import { AppDataSource } from "../AppDataSource";
import LangNamespace from "../models/db/LangNamespace";

export const LangNamespaceRepository = AppDataSource.getRepository(
  LangNamespace,
).extend({
  findById(namespaceId: number): Promise<LangNamespace> {
    return this.findOne({
      relations: [
        "langResource",
        "langResourceFiles",
        "langResource.langResourceVersions",
      ],
      where: {
        id: namespaceId,
      },
    });
  },
});
