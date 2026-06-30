import LangKey from "../models/db/LangKey";
import config from "../config";
import { AppDataSource } from "../AppDataSource";

export const LangKeyRepository = AppDataSource.getRepository(LangKey).extend({
  findAllKeysByNamespace(
    namespaceId: number,
    lastKey: string,
    fetchCount: number,
    keyword: string,
  ): Promise<LangKey[]> {
    let whereStr = " t1.namespaceId = :namespaceId ";
    const whereConditions: any = {};
    whereConditions.namespaceId = namespaceId;

    if (lastKey != null) {
      whereStr += "AND t1.key > :lastKey ";
      whereConditions.lastKey = lastKey;
    }

    if (keyword != null && keyword.length > 0) {
      whereStr +=
        "AND (t1.key LIKE :keyword OR t1.id IN (SELECT key_id FROM t_lang_translate lt WHERE lt.value LIKE :keyword) )";
      whereConditions.keyword = `%${keyword}%`;
    }

    return this.createQueryBuilder("t1")
      .leftJoinAndSelect("t1.langTranslates", "t2")
      .take(fetchCount == null ? config.defaultFetchCount : fetchCount)
      .where(whereStr, whereConditions)
      .orderBy({
        "t1.key": "ASC",
      })
      .getMany();
  },

  getAllKeys(namespaceId: number, lang: string): Promise<LangKey[]> {
    return this.createQueryBuilder("t1")
      .innerJoinAndSelect("t1.langTranslates", "t2")
      .where(" t1.namespaceId = :namespaceId AND t2.lang = :lang", {
        namespaceId: namespaceId,
        lang: lang,
      })
      .orderBy({
        "t1.key": "ASC",
      })
      .getMany();
  },

  findById(id: number): Promise<LangKey> {
    return this.findOne({
      where: { id: id },
      relations: ["langTranslates"],
    });
  },

  findByKeyAndNamespace(key: string, namespaceId: number): Promise<LangKey> {
    return this.findOne({
      where: {
        key: key,
        namespaceId: namespaceId,
      },
    });
  },
});
