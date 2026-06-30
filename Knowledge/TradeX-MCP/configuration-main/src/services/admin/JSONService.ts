import { Service } from "typedi";
import { EntityManager } from "typeorm";
import * as AWS from "aws-sdk";
import config, { STORAGE_TYPES } from "../../config";
import { ClientRepository } from "../../repositories/ClientRepository";
import { LoginMethodRepository } from "../../repositories/LoginMethodRepository";
import { ScopeGroupRepository } from "../../repositories/ScopeGroupRepository";
import { ScopeRepository } from "../../repositories/ScopeRepository";
import { IImportResponse } from "../../models/response/IImportResponse";
import { Client as MinioClient } from "minio";
import axios, { AxiosResponse } from "axios";
import { AppDataSource } from "../../AppDataSource";

@Service()
export default class JSONService {
  public async exportJsonMap(): Promise<any> {
    const clientMap = await ClientRepository.queryClientIdMap();
    const loginMap = await LoginMethodRepository.queryLoginMethodMap();
    const scopeGroupMap = await ScopeGroupRepository.queryScopeGrounpMap();
    const scopeMap = await ScopeRepository.queryScopeMap();
    if (config.storageService === STORAGE_TYPES.S3) {
      const s3 = new AWS.S3({
        accessKeyId: config.aws.accessKeyId,
        secretAccessKey: config.aws.secretAccessKey,
        region: config.aws.s3.public.region,
      });
      const request: AWS.S3.PutObjectRequest = {
        Bucket: config.aws.s3.public.bucket,
        Key: config.json,
        Body: JSON.stringify({
          clientMap,
          loginMap,
          scopeGroupMap,
          scopeMap,
        }),
        ACL: config.aws.s3.public.acl,
      };

      return new Promise((resolve: any, reject: any) => {
        s3.upload(request, (error: any, data: any) => {
          if (error) {
            return reject(error);
          }
          return resolve(data);
        });
      });
    } else {
      const client = new MinioClient(config.minio.internal);
      const conf = config.minio.buckets.public;
      if (!(await client.bucketExists(conf.bucket))) {
        await client.makeBucket(conf.bucket, config.minio.region);
        let policies = config.minio.policies[conf.acl];
        policies = policies.split("xxBucketNamexx").join(conf.bucket);
        await client.setBucketPolicy(conf.bucket, policies);
      }
      return client.putObject(
        conf.bucket,
        config.json,
        JSON.stringify({
          clientMap,
          loginMap,
          scopeGroupMap,
          scopeMap,
        }),
      );
    }
  }

  public async importJsonMap(): Promise<IImportResponse> {
    const response: AxiosResponse = await axios.get(config.dbExportUrl);
    const res: IImportResponse = {
      status: response.status,
      statusText: response.statusText,
      importStatus: "",
    };
    if (response.status !== 200) {
      res.importStatus = "FALSE";
      res.statusText = `${config.dbExportUrl} - ${response.statusText}`;
      return res;
    }
    const value = response.data;
    const clientMap = {};
    const loginMap = {};
    const scopeGroup = {};
    await Promise.all([
      value.clientMap.forEach(
        (value: any) => (clientMap[value.clientId] = value),
      ),
      value.loginMap.forEach((value: any) => (loginMap[value.id] = value)),
      value.scopeGroupMap.forEach(
        (value: any) => (scopeGroup[value.name] = value),
      ),
    ]);
    await Promise.all([
      value.loginMap.forEach((value: any) => {
        value.clients = value.clients.map((obj: any) => clientMap[obj]);
      }),
      value.scopeGroupMap.forEach((value: any) => {
        value.loginMethods = value.loginMethods.map(
          (obj: any) => loginMap[obj],
        );
      }),
      value.scopeMap.forEach((value: any) => {
        value.scopeGroups = value.scopeGroups.map(
          (obj: any) => scopeGroup[obj],
        );
      }),
    ]);
    try {
      await AppDataSource.transaction(async (entityManager: EntityManager) => {
        await entityManager.getRepository("t_client").save(value.clientMap);
        await entityManager
          .getRepository("t_login_method")
          .save(value.loginMap);
        await entityManager
          .getRepository("t_scope_group")
          .save(value.scopeGroupMap);
        await entityManager.getRepository("t_scope").save(value.scopeMap);
      });
    } catch (e) {
      res.importStatus = "FALSE";
      res.errMsg = e;
      return res;
    }
    res.importStatus = "SUCCESS";
    return res;
  }
}
