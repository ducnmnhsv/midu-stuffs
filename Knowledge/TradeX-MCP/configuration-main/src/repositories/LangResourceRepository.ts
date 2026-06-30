import LangResource from "../models/db/LangResource";
import { AppDataSource } from "../AppDataSource";

export const LangResourceRepository = AppDataSource.getRepository(
  LangResource,
).extend({
  getAllResources(): Promise<LangResource[]> {
    return this.find({
      relations: ["langNamespaces"],
    });
  },
});
