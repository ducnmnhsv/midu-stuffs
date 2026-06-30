import { Service } from "typedi";
import { AWS, Errors } from "tradex-common";
import config, { STORAGE_TYPES } from "../config";
import IAWSGetSignedDataRequest from "../models/request/IAWSGetSignedDataRequest";
import {
  REQUIRE_AUTHENTICATE,
  AWS_GET_SIGNED_DATA_FAILED,
  STORAGE_SERVICE_ACTION_NOT_SUPPORTED,
} from "../constants/errors";
import { Client as MinioClient } from "minio";

@Service()
export default class AmazonWebService {
  public async getPresignedURL(
    request: IAWSGetSignedDataRequest,
  ): Promise<any> {
    if (config.storageService === STORAGE_TYPES.S3) {
      if (request.action && request.action !== "upload") {
        throw new Errors.GeneralError(
          STORAGE_SERVICE_ACTION_NOT_SUPPORTED,
          null,
        );
      }
      return this.getPresignedURLToUploadAWSPublic(request);
    } else {
      if (request.action === undefined || request.action === "upload") {
        return this.getPresignedURLToUploadMino(request);
      } else {
        return this.getPresignedURLToDownloadMino(request);
      }
    }
  }

  private async getPresignedURLToUploadAWSPublic(
    request: IAWSGetSignedDataRequest,
  ): Promise<any> {
    const signedData: any = await AWS.generateSignedDataForUpload(
      request.key,
      config.aws.s3.public,
    );
    if (signedData == null) {
      throw new Errors.GeneralError(AWS_GET_SIGNED_DATA_FAILED, null);
    }
    return signedData;
  }

  private async getPresignedURLToUploadMino(
    request: IAWSGetSignedDataRequest,
  ): Promise<any> {
    const client = new MinioClient(config.minio.external);
    let conf = config.minio.buckets[request.serviceName];
    if (!conf) {
      conf = config.minio.buckets.public;
    }
    if (
      conf.requireAuthenticate &&
      (request.headers == null || request.headers.token == null)
    ) {
      throw new Errors.GeneralError(REQUIRE_AUTHENTICATE);
    }
    if (!(await client.bucketExists(conf.bucket))) {
      await client.makeBucket(conf.bucket, config.minio.region);
      let policies = config.minio.policies[conf.acl];
      policies = policies.split("xxBucketNamexx").join(conf.bucket);
      await client.setBucketPolicy(conf.bucket, policies);
    }
    return client.presignedPutObject(conf.bucket, request.key, conf.expires);
  }

  private async getPresignedURLToDownloadMino(
    request: IAWSGetSignedDataRequest,
  ): Promise<any> {
    const client = new MinioClient(config.minio.external);
    let conf = config.minio.buckets[request.serviceName];
    if (!conf) {
      conf = config.minio.buckets.public;
    }
    if (
      conf.requireAuthenticate &&
      (request.headers == null || request.headers.token == null)
    ) {
      throw new Errors.GeneralError(REQUIRE_AUTHENTICATE);
    }
    return client.presignedGetObject(conf.bucket, request.key, conf.expires);
  }
}
