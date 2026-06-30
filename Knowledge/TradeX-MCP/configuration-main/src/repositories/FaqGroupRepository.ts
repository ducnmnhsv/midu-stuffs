import FaqGroup from "../models/db/FaqGroup";
import { AppDataSource } from "../AppDataSource";

export const FaqGroupRepository = AppDataSource.getRepository(FaqGroup).extend({
  findByMsNameAndLang(msName: string, lang: string): Promise<FaqGroup[]> {
    return this.find({
      relations: ["faqs"],
      where: {
        msName: msName,
        lang: lang,
      },
    });
  },
});
