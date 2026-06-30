import { AppDataSource } from "../AppDataSource";
import Faq from "../models/db/Faq";

export const FaqRepository = AppDataSource.getRepository(Faq).extend({
  findById(id: number): Promise<Faq> {
    return this.findOne({ id: id });
  },
});
