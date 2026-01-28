import TemplateResource from "../models/db/TemplateResource";
import { AppDataSource } from "../AppDataSource";
import { In } from "typeorm";

export const TemplateResourceRepository = AppDataSource.getRepository(
  TemplateResource,
).extend({
  findByMsName(msNames: string[]): Promise<TemplateResource[]> {
    return this.find({
      msName: In(msNames),
      isLatest: true,
    });
  },
});
