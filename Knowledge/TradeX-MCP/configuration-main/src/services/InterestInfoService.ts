import { Service } from "typedi";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { InterestInfoRepository } from "../repositories/InterestInfoRepository";
import InterestInfo, { parseInterestInfos } from "../models/db/InterestInfo";

@Service()
export default class InterestInfoService {
  public async findAllInterestInfo(): Promise<TradexModelsConfiguration.QueryInterestInfoResponse> {
    const interestInfoList: InterestInfo[] =
      await InterestInfoRepository.find();
    return parseInterestInfos(interestInfoList);
  }
}
