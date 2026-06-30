import LangResourceFile from "../models/db/LangResourceFile";
import { AppDataSource } from "../AppDataSource";

export const LangResourceFileRepository = AppDataSource.getRepository(
  LangResourceFile,
).extend({
  findByMsName(msNames: string[]): Promise<LangResourceFile[]> {
    return this.createQueryBuilder("t1")
      .innerJoinAndSelect("t1.langNamespace", "t2")
      .innerJoinAndSelect("t2.langResource", "t3")
      .innerJoinAndSelect("t3.langResourceVersions", "t4")
      .where("t3.msName IN (:msNames)", { msNames: msNames })
      .orderBy({
        "t3.msName": "ASC",
        "t1.lang": "ASC",
      })
      .getMany();
  },
});
