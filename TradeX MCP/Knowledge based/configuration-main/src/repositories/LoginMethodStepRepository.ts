import LoginMethodStep from "../models/db/LoginMethodStep";
import { AppDataSource } from "../AppDataSource";

export const LoginMethodStepRepository = AppDataSource.getRepository(
  LoginMethodStep,
).extend({
  async queryStepsIn(loginMethodIds: number[]): Promise<LoginMethodStep[]> {
    let queryBuilder = this.createQueryBuilder("s")
      .leftJoinAndSelect("s.scopeGroups", "scopeGroup")
      .where("s.loginMethodId in (:ids)", { ids: loginMethodIds });
    queryBuilder = queryBuilder.orderBy({
      "s.loginMethodId": "ASC",
      "s.step": "ASC",
    });
    return queryBuilder.printSql().getMany();
  },
});
