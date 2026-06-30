import {
  OpenApiDeleteResponse,
  OpenApiFileResponse,
  OpenApiListResponse,
  OpenApiUpdateResponse,
} from "tradex-models-configuration";
import OpenApi from "../models/db/OpenApi";
// tslint:disable-next-line:no-implicit-dependencies
import { OpenAPIV3 } from "openapi-types";

const toOpenApiUpdateResponse = (): OpenApiUpdateResponse => {
  return {};
};

const toOpenApiListResponse = (openApi: OpenApi): OpenApiListResponse => {
  return {
    id: openApi.id,
    uriPattern: openApi.uriPattern,
    operationId: openApi.operationId,
    summary: openApi.summary,
    parameters: openApi.parameters,
    requestBody: openApi.requestBody,
    responses: openApi.responses,
    security: openApi.security,
    tags: openApi.tags,
  };
};

const toOpenApi = (
  id: number,
  uriPattern: string,
  data: OpenAPIV3.OperationObject,
): OpenApi => {
  const openApi = new OpenApi();
  openApi.id = id;
  openApi.summary = data.summary;
  openApi.tags = data.tags;
  openApi.operationId = data.operationId;
  openApi.uriPattern = uriPattern;
  openApi.parameters = data.parameters;
  openApi.requestBody = data.requestBody != null ? data.requestBody : null;
  openApi.responses = data.responses;
  openApi.security = data.security;
  return openApi;
};

const toOpenApiDeleteResponse = (): OpenApiDeleteResponse => {
  return {};
};

const toOperationObject = (request: OpenApi): OpenAPIV3.OperationObject => {
  return {
    tags: request.tags,
    summary: request.summary,
    description: request.description,
    operationId: request.operationId,
    parameters: request.parameters,
    requestBody: request.requestBody,
    responses: request.responses,
    security: request.security,
  };
};

const toOpenApiFileResponse = (url: string): OpenApiFileResponse => {
  return {
    url: url,
  };
};

export {
  toOpenApiUpdateResponse,
  toOpenApiListResponse,
  toOpenApiDeleteResponse,
  toOpenApi,
  toOperationObject,
  toOpenApiFileResponse,
};
