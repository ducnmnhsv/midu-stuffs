import { Service } from "typedi";
import { AWS, Errors, Models } from "tradex-common";
import { Client as MinioClient } from "minio";
import config, { STORAGE_TYPES } from "../../config";
import { TempCredentialResponse } from "../../models/response/TempCredentialResponse";
import IAWSGetSignedDataRequest from "../../models/request/IAWSGetSignedDataRequest";
import { AWS_GET_SIGNED_DATA_FAILED } from "../../constants/errors";

@Service()
export default class AdminAmazonWebService {
  public async getSignedDataToUploadInternal(
    request: IAWSGetSignedDataRequest,
  ): Promise<any> {
    if (config.storageService === STORAGE_TYPES.S3) {
      const signedData: any = await AWS.generateSignedDataForUpload(
        request.key,
        config.aws.s3[request.serviceName],
      );
      if (signedData == null) {
        throw new Errors.GeneralError(AWS_GET_SIGNED_DATA_FAILED, null);
      }
      return signedData;
    } else {
      const client = new MinioClient(config.minio.internal);
      const conf = config.minio.buckets[request.serviceName];
      if (!(await client.bucketExists(conf.bucket))) {
        await client.makeBucket(conf.bucket, config.minio.region);
        let policies = config.minio.policies[conf.acl];
        policies = policies.split("xxBucketNamexx").join(conf.bucket);
        await client.setBucketPolicy(conf.bucket, policies);
      }
      return client.presignedPutObject(conf.bucket, request.key, conf.expires);
    }
  }

  public async getPublicTempCredential(
    request: Models.IDataRequest,
  ): Promise<TempCredentialResponse> {
    const params: any = config.assumeRole.public;
    params.RoleSessionName = `tradex-${request.headers.token.userId}`;
    params.ExternalId = `tradex-${request.headers.token.userId}`;
    const value: any = await AWS.getTempCredentials(params);
    const tempCredentialResponse = new TempCredentialResponse();
    tempCredentialResponse.accessKeyId = value.AccessKeyId;
    tempCredentialResponse.secretAccessKey = value.SecretAccessKey;
    tempCredentialResponse.sessionToken = value.SessionToken;
    return tempCredentialResponse;
  }
}
