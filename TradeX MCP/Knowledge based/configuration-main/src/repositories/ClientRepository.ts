import Client from "../models/db/Client";
import config from "../config";
import { AppDataSource } from "../AppDataSource";
import IClientRequest from "../models/request/admin/IClientRequest";
import { ClientStatusEnum } from "../constants/ClientStatusEnum";
import { ISystemQuery } from "../models/request/admin/ISystemQueryRequest";
import LoginMethod from "../models/db/LoginMethod";

export const ClientRepository = AppDataSource.getRepository(Client).extend({
  async findByClientRequest(request: IClientRequest): Promise<Client[]> {
    let whereStr = " client.domain = :domain AND client.status = :status ";
    const whereConditions: any = {
      domain: request.domain,
      status: ClientStatusEnum.ENABLED,
    };

    if (request.lastSequence != null) {
      whereStr += " AND client.id > :lastSequence";
      whereConditions.lastSequence = request.lastSequence;
    }

    const response = this.createQueryBuilder("client")
      .take(
        request.fetchCount == null
          ? config.defaultFetchCount
          : request.fetchCount,
      )
      .where(whereStr, whereConditions)
      .orderBy({
        "client.id": "ASC",
      });
    if (request.isFullData === true) {
      return response
        .leftJoinAndSelect("client.loginMethods", "loginMethod")
        .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
        .getMany();
    } else {
      return response.getMany();
    }
  },

  async queryClientForSystemRequest(request: ISystemQuery): Promise<Client[]> {
    let queryBuilder = this.createQueryBuilder("client")
      .leftJoinAndSelect("client.loginMethods", "loginMethod")
      .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
      .where("client.domain = :domain", { domain: request.domain });
    if (request.lastQueriedTime != null) {
      queryBuilder.andWhere(
        "client.updated_at > :lastQueriedTime or loginMethod.updated_at > :lastQueriedTime",
        { lastQueriedTime: request.lastQueriedTime },
      );
    }
    queryBuilder = queryBuilder.orderBy({
      "client.id": "ASC",
    });
    return queryBuilder.printSql().getMany();
  },

  async findById(id: number): Promise<Client> {
    return this.createQueryBuilder("client")
      .leftJoinAndSelect("client.loginMethods", "loginMethod")
      .leftJoinAndSelect("loginMethod.scopeGroups", "scopeGroup")
      .where("client.id = :id AND client.status = :status", {
        id: id,
        status: ClientStatusEnum.ENABLED,
      })
      .getOne();
  },

  async findByClientId(clientId: string): Promise<Client> {
    return this.createQueryBuilder("client")
      .where("client.clientId = :id", {
        id: clientId,
      })
      .getOne();
  },

  async queryClientIdMap(): Promise<Client[]> {
    const data = await this.createQueryBuilder("client")
      .leftJoinAndSelect("client.loginMethods", "loginMethod")
      .getMany();
    data.map((obj: Client) => {
      obj.loginMethods = Object.assign(
        obj.loginMethods.map((value: LoginMethod) => value.id),
      );
    });
    return data;
  },
});
