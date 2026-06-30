import OpenApi from "../models/db/OpenApi";
import { DEFAULT_PAGE_SIZE } from "../constants";
import { AppDataSource } from "../AppDataSource";

export const OpenApiRepository = AppDataSource.getRepository(OpenApi).extend({
  findBy(
    filter: any,
    fetchCount: number = DEFAULT_PAGE_SIZE,
    sort: any = { id: "ASC" },
  ): Promise<OpenApi[]> {
    return this.find({
      order: sort,
      where: filter,
      take: fetchCount,
    });
  },
});
