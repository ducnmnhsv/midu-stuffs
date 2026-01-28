import LoginMethod from "../models/db/LoginMethod";
import config from "../config";
import ILoginMethodRequest, {
  ILoginMethodIdRequest,
} from "../models/request/admin/ILoginMethodRequest";
import ISystemQueryRequest from "../models/request/admin/ISystemQueryRequest";
import ScopeGroup from "../models/db/ScopeGroup";
import Client from "../models/db/Client";
import { AppDataSource } from "../AppDataSource";

export const LoginMethodRepository = AppDataSource.getRepository(
  LoginMethod,
).extend({
  async findByLoginMethodRequest(
    request: ILoginMethodRequest,
  ): Promise<LoginMethod[]> {
    let whereStr = " 1 = 1 ";
    const whereCondition: any = {};

    if (request.lastSequence != null) {
      whereStr += " AND loginMethod.id > :lastSequence";
      whereCondition.lastSequence = request.lastSequence;
    }

    return this.createQueryBuilder("loginMethod")
      .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
      .take(
        request.fetchCount == null
          ? config.defaultFetchCount
          : request.fetchCount,
      )
      .where(whereStr, whereCondition)
      .orderBy({
        "loginMethod.id": "ASC",
      })
      .getMany();
  },

  async queryLoginMethodForUpdateRequest(
    request: ISystemQueryRequest,
  ): Promise<LoginMethod[]> {
    const whereStr = "loginMethod.updated_at > :lastQueriedTime";
    const whereCondition = {
      lastQueriedTime:
        request.lastQueriedTime != null ? request.lastQueriedTime : "",
    };

    return this.createQueryBuilder("loginMethod")
      .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
      .where(whereStr, whereCondition)
      .orderBy({
        "loginMethod.id": "ASC",
      })
      .getMany();
  },

  async findByLoginMethodId(
    request: ILoginMethodIdRequest,
  ): Promise<LoginMethod> {
    return this.createQueryBuilder("loginMethod")
      .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
      .where("loginMethod.id = :id", { id: request.id })
      .getOne();
  },

  async queryLoginMethodMap(): Promise<LoginMethod[]> {
    const data = await this.createQueryBuilder("loginMethod")
      .leftJoinAndSelect("loginMethod.clients", "client")
      .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
      .getMany();
    data.map((obj: LoginMethod) => {
      obj.scopeGroups = Object.assign(
        obj.scopeGroups.map((value: ScopeGroup) => value.name),
      );
    });
    data.map((obj: LoginMethod) => {
      obj.clients = Object.assign(
        obj.clients.map((value: Client) => value.clientId),
      );
    });
    return data;
  },
});
