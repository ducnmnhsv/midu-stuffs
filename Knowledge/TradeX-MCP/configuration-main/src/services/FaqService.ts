import { Service } from "typedi";
import { FaqGroupRepository } from "../repositories/FaqGroupRepository";
import { FaqRepository } from "../repositories/FaqRepository";
import FaqGroup from "../models/db/FaqGroup";
import Faq from "../models/db/Faq";
import { FaqGroupResponse, parse } from "../models/response/FaqGroupResponse";
import { Errors, Utils } from "tradex-common";
import { EntityManager } from "typeorm";
import FaqReview from "../models/db/FaqReview";
import IFaqQueryRequest from "../models/request/IFaqQueryRequest";
import IFaqReviewRequest from "../models/request/IFaqReviewRequest";
import { FAQ_ALREADY_REVIEWED } from "../constants/errors";
import { AppDataSource } from "../AppDataSource";

@Service()
export default class FaqService {
  public async getFaqsOfService(
    request: IFaqQueryRequest,
  ): Promise<FaqGroupResponse[]> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.msName, "msName")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
    const lang = request.headers["accept-language"];

    const faqGroups: FaqGroup[] = await FaqGroupRepository.findByMsNameAndLang(
      request.msName,
      lang,
    );
    if (faqGroups == null) {
      throw new Errors.ObjectNotFoundError();
    }

    return faqGroups.map(parse);
  }

  public async reviewFaq(request: IFaqReviewRequest): Promise<any> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.faqId, "faqId")
      .setRequire()
      .throwValid(invalidParams);
    Utils.validate(request.isUseful, "isUseful")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();

    const faq: Faq = await FaqRepository.findById(request.faqId);
    if (faq == null) {
      throw new Errors.ObjectNotFoundError();
    }
    try {
      return AppDataSource.transaction(
        async (transactionalEntityManager: EntityManager) => {
          const faqReview = new FaqReview();
          faqReview.userId = request.headers.token.userId;
          faqReview.isUseful = request.isUseful;
          faqReview.faqId = request.faqId;

          await transactionalEntityManager.save(faqReview);
        },
      );
    } catch (err) {
      if (err.code === "ER_DUP_ENTRY") {
        throw new Errors.GeneralError(FAQ_ALREADY_REVIEWED, null);
      } else {
        throw err;
      }
    }
  }
}
