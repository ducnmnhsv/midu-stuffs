import InterestInfo from "../models/db/InterestInfo";
import { AppDataSource } from "../AppDataSource";

export const InterestInfoRepository = AppDataSource.getRepository(
  InterestInfo,
).extend({});
