/* tslint:disable */
import {Models} from "tradex-common";

function service(service: string, uri: string): Models.AAA.IForwardService {
  return {
    service,
    uri,
    forwardType: Models.AAA.ForwardType.SERVICE,
  };
}

function connection(type: Models.AAA.ForwardDataType, uri_mapping: Map<string, string>): Models.AAA.IForwardConnection {
  return {
    type,
    uri_mapping,
    forwardType: Models.AAA.ForwardType.CONNECTION,
  };
}

export const config = {
  service,
  connection,
};

