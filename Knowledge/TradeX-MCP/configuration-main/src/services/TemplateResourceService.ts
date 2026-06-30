import { Service } from "typedi";
import { TemplateResourceRepository } from "../repositories/TemplateResourceRepository";
import TemplateResource from "../models/db/TemplateResource";
import {
  parse,
  TemplateResourceResponse,
} from "../models/response/TemplateResourceResponse";
import { Errors, Utils } from "tradex-common";
import ITemplateResourceRequest from "../models/request/ITemplateResourceRequest";

@Service()
export default class TemplateResourceService {
  public async getAllResources(
    request: ITemplateResourceRequest,
  ): Promise<TemplateResourceResponse[]> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.msNames, "msNames")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
    const templateResources: TemplateResource[] =
      await TemplateResourceRepository.findByMsName(request.msNames);
    if (templateResources == null) {
      throw new Errors.ObjectNotFoundError();
    }

    return templateResources.map(parse);
  }
}
