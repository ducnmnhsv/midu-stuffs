import { Service as TypeService } from "typedi";
import { Models } from "tradex-common";
import { ServiceRepository } from "../repositories/ServiceRepository";
import { parse, ServiceResponse } from "../models/response/ServiceResponse";
import Service from "../models/db/Service";

/* eslint-disable @typescript-eslint/no-unused-vars */
@TypeService()
export default class CommonService {
  public async getAllServices(
    request: Models.IDataRequest,
  ): Promise<ServiceResponse[]> {
    /* eslint-enable @typescript-eslint/no-unused-vars */
    const response: Service[] = await ServiceRepository.find();
    return response.map(parse);
  }
}
