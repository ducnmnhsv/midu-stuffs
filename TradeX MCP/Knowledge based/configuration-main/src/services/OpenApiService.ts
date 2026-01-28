import { Service } from "typedi";
import OpenApi from "../models/db/OpenApi";
import Scope from "../models/db/Scope";
import { OpenApiRepository } from "../repositories/OpenApiRepository";
import { ScopeRepository } from "../repositories/ScopeRepository";
import { EntityManager, In, MoreThan } from "typeorm";
import * as SwaggerParser from "swagger-parser";
// tslint:disable-next-line:no-implicit-dependencies
import { OpenAPIV3 } from "openapi-types";
import { DEFAULT_LAST_SEQUENCE, DEFAULT_PAGE_SIZE } from "../constants";
import { Errors, Logger } from "tradex-common";
import { INVALID_PARAMETER } from "../constants/errors";
import * as Ajv from "ajv";
import {
  OpenApiDeleteRequest,
  OpenApiDeleteResponse,
  OpenApiListRequest,
  OpenApiListResponse,
  OpenApiUpdateRequest,
  OpenApiUpdateResponse,
  OpenApiFileRequest,
  OpenApiFileResponse,
} from "tradex-models-configuration";
import {
  openApiDeleteRequestValidator,
  openApiListRequestValidator,
  openApiUpdateRequestValidator,
  openApiFileRequestValidator,
} from "tradex-models-configuration-validator";
import {
  toOpenApi,
  toOpenApiDeleteResponse,
  toOpenApiFileResponse,
  toOpenApiListResponse,
  toOpenApiUpdateResponse,
  toOperationObject,
} from "../utils/ResponseUtils";
import axios, { AxiosResponse, AxiosRequestConfig } from "axios";
import { ClientRepository } from "../repositories/ClientRepository";
import Client from "../models/db/Client";
import config, { STORAGE_TYPES } from "../config";
import * as AWS from "aws-sdk";
import { Client as MinioClient } from "minio";
import { AppDataSource } from "../AppDataSource";

@Service()
export default class OpenApiService {
  /**
   * update openApi from url
   * @param request
   */
  public async updateOpenApi(
    request: OpenApiUpdateRequest,
  ): Promise<OpenApiUpdateResponse> {
    const validator: Ajv.ValidateFunction = openApiUpdateRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    let countMatched = 0;

    const scopeList: Scope[] = await ScopeRepository.find({});
    const nameScopeMap = new Map<string, Scope>();
    const uriPatternScopeMap = new Map<string, Scope>();
    for (const scope of scopeList) {
      nameScopeMap.set(scope.name, scope);
      uriPatternScopeMap.set(scope.uriPattern, scope);
    }

    for (const url of request.urlList) {
      //download
      const options: AxiosRequestConfig = {
        method: "get",
        url,
        responseType: "text",
      };
      let fetchedData: any = "";
      //if success, return object, if fail, return string - error message
      try {
        // tslint:disable-next-line
        const response: AxiosResponse = await axios(options);
        if (response.status >= 200 && response.status < 300) {
          fetchedData = JSON.parse(response.data);
        } else {
          throw new Error(response.statusText);
        }
      } catch (error) {
        Logger.error(`fail to download from url ${url}, error`, error);
        continue;
      }

      const matchedList: OpenApi[] = [];
      //parse
      const openApi: OpenAPIV3.Document = (await SwaggerParser.validate(
        fetchedData,
      )) as OpenAPIV3.Document;
      //iterate through uri

      for (const uri of Object.keys(openApi.paths)) {
        const pathItemObject = openApi.paths[uri];
        for (const method of Object.keys(pathItemObject)) {
          const operationObject = pathItemObject[method];
          const uriPattern = `${method}:${uri}`;
          if (nameScopeMap.has(operationObject.operationId)) {
            const scope = nameScopeMap.get(operationObject.operationId);
            const openApi: OpenApi = toOpenApi(
              +scope.id,
              uriPattern,
              operationObject,
            );
            matchedList.push(openApi);
            //if open api dont have operationId, find scope by uriPattern instead
          } else {
            if (uriPatternScopeMap.has(uriPattern)) {
              const scope = uriPatternScopeMap.get(uriPattern);
              const openApi: OpenApi = toOpenApi(
                +scope.id,
                uriPattern,
                operationObject,
              );
              openApi.operationId = scope.name;
              matchedList.push(openApi);
            }
          }
        }
      }

      await AppDataSource.transaction(
        async (txEntityManager: EntityManager) => {
          await txEntityManager.save(matchedList);
        },
      );
      countMatched += matchedList.length;
    }
    Logger.info(`total matched open api: ${countMatched}`);
    if (countMatched > 0) {
      await this.createAndUploadOpenApiFile();
      await this.createFinalSwagger();
    }
    return toOpenApiUpdateResponse();
  }

  public async queryOpenApiList(
    request: OpenApiListRequest,
  ): Promise<OpenApiListResponse[]> {
    const validator: Ajv.ValidateFunction = openApiListRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }

    const fetchCount =
      request.fetchCount != null ? request.fetchCount : DEFAULT_PAGE_SIZE;
    const lastSequence =
      request.lastSequence != null
        ? request.lastSequence
        : DEFAULT_LAST_SEQUENCE;

    const openApiList = await OpenApiRepository.findBy(
      {
        id: MoreThan(lastSequence),
      },
      fetchCount,
      { id: "ASC" },
    );
    return openApiList.map(toOpenApiListResponse);
  }

  public async deleteOpenApiList(
    request: OpenApiDeleteRequest,
  ): Promise<OpenApiDeleteResponse> {
    const validator: Ajv.ValidateFunction = openApiDeleteRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    await AppDataSource.transaction(async (txEntityManager: EntityManager) => {
      await txEntityManager.delete(OpenApi, {
        id: In(request.scopeIdList),
      });
    });
    return toOpenApiDeleteResponse();
  }

  public async createAndUploadOpenApiFile() {
    const finalClientList = [];
    const clientList: Client[] = await ClientRepository.find({});

    for (const client of clientList) {
      let scopeList: Scope[] = await ScopeRepository.queryAllScopeByClient(
        client.id,
      );
      const publicScope = await ScopeRepository.queryAllScopeByScoupGroup(
        config.publicScopeGroup,
      );
      scopeList = scopeList.concat(publicScope);
      const scopeIdList = scopeList.map((scope: Scope) => +scope.id);
      const openApiList: OpenApi[] = await OpenApiRepository.find({
        where: {
          id: In(scopeIdList),
        },
        order: {
          id: "ASC",
        },
      });
      const swaggerFile = await this.createOpenApiFile(
        client.openApiServer,
        openApiList,
      );
      client.openApiUrl = await this.uploadFile(
        swaggerFile,
        `${client.clientId}-open-api.json`,
      );
      finalClientList.push(client);
    }

    await AppDataSource.transaction(async (txEntityManager: EntityManager) => {
      await txEntityManager.save(Client, finalClientList);
    });
    return;
  }

  public async createFinalSwagger() {
    const openApiList = await OpenApiRepository.find({});
    const swagger = this.createOpenApiFile(
      "http://localhost:3000",
      openApiList,
    );
    const url = await this.uploadFile(swagger, "finalSwagger.json");
    Logger.info(`final swagger url: ${url}`);
  }

  public async uploadFile(data: object, name: string): Promise<string> {
    if (config.storageService === STORAGE_TYPES.S3) {
      return this.uploadS3(data, name);
    } else {
      return this.uploadMinio(data, name);
    }
  }

  public createOpenApiFile(openApiServer: string, openApiList: OpenApi[]): any {
    const swagger: any = config.swagger.header;
    swagger.servers[0].url = openApiServer;
    const paths: any = {};
    const tags: any[] = [];
    const uniqueTag: string[] = [];

    openApiList.forEach((openApi: OpenApi) => {
      const operationObject = toOperationObject(openApi);

      const uriPattern = openApi.uriPattern;
      const marker = uriPattern.indexOf("/api/");

      const uri = uriPattern.substring(marker, uriPattern.length);
      const method = uriPattern.substring(0, marker - 1);
      //check null
      if (paths[uri] == null) {
        paths[uri] = {};
      }
      //delete null field
      for (const field of Object.keys(operationObject)) {
        const value = operationObject[field];
        if (value == null) {
          delete (operationObject as any)[field];
        }
      }
      paths[uri][method] = operationObject;
      if (!uniqueTag.includes(operationObject.tags[0])) {
        const tag = {
          name: operationObject.tags[0],
        };
        uniqueTag.push(operationObject.tags[0]);
        tags.push(tag);
      }
    });

    swagger.paths = paths;
    swagger.tags = tags;
    return swagger;
  }

  public async uploadS3(data: object, name: string): Promise<string> {
    Logger.info("start uploadS3");
    const s3 = new AWS.S3({
      accessKeyId: config.s3.accessKey,
      secretAccessKey: config.s3.privateKey,
      region: config.s3.region,
    });

    const request = {
      Bucket: config.s3.bucketName,
      Key: name,
      Body: JSON.stringify(data),
      ACL: "public-read",
    };

    return new Promise((resolve: any, reject: any) => {
      s3.upload(request, (error: any, data: any) => {
        if (error) {
          return reject(error);
        }
        Logger.info(`finished uploadS3, url: ${data.Location}`);
        return resolve(data.Location);
      });
    });
  }

  public async uploadMinio(data: object, name: string): Promise<string> {
    Logger.info("start uploadMinio");
    const minioClient = new MinioClient(config.minio.internal);
    const conf = config.minio.buckets.public;
    const metaData = {
      "Content-Type": "application/json",
    };
    if (!(await minioClient.bucketExists(conf.bucket))) {
      await minioClient.makeBucket(conf.bucket, config.minio.region);
      let policies = config.minio.policies[conf.acl];
      policies = policies.split("xxBucketNamexx").join(conf.bucket);
      await minioClient.setBucketPolicy(conf.bucket, policies);
    }

    await minioClient.putObject(
      conf.bucket,
      name,
      JSON.stringify(data),
      metaData,
    );
    const uploadUrl = config.minio.urlRewriteTo
      .concat("/")
      .concat(conf.bucket)
      .concat("/")
      .concat(name);

    Logger.info(`finished upload to Minio ${uploadUrl}`);
    return uploadUrl;
  }

  public async queryOpenApiFileByClient(
    request: OpenApiFileRequest,
  ): Promise<OpenApiFileResponse> {
    const validator: Ajv.ValidateFunction = openApiFileRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const client: Client = await ClientRepository.findOneBy({
      clientId: request.clientId,
    });
    return toOpenApiFileResponse(client.openApiUrl);
  }
}
