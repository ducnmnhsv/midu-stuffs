import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToMany,
  UpdateDateColumn,
  CreateDateColumn,
} from "typeorm";
import LoginMethod, { parseToLoginMethodResponse } from "./LoginMethod";
import { Utils } from "tradex-common";
import { IClientResponse } from "../response/IClientResponse";

@Entity("t_client")
export default class Client {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "user_id" })
  public userId: number;

  @Column({ name: "client_id" })
  public clientId: string;

  @Column({ name: "client_secret" })
  public clientSecret: string;

  @Column()
  public description: string;

  @Column()
  public status: number;

  @Column({ name: "open_api_server" })
  public openApiServer: string;

  @Column({ name: "open_api_url" })
  public openApiUrl: string;

  @Column({ name: "app_version" })
  public appVersion: string;

  @Column({ name: "created_by" })
  public createdBy: string;

  @Column({ name: "updated_by" })
  public updatedBy: string;

  @CreateDateColumn({ name: "created_at" })
  public createdAt: Date;

  @UpdateDateColumn({ name: "updated_at" })
  public updatedAt: Date;

  @Column()
  public domain: string;

  @ManyToMany(
    (objType: any) => LoginMethod,
    (loginMethod: LoginMethod) => loginMethod.clients,
    { cascade: ["insert", "update"] },
  )
  public loginMethods: LoginMethod[];
}

export function parseToClientResponse(client: Client): IClientResponse {
  if (client != null) {
    const response: IClientResponse = {
      id: client.id,
      userId: client.userId,
      clientId: client.clientId,
      clientSecret: client.clientSecret,
      description: client.description,
      status: client.status,
      appVersion: client.appVersion,
      createdBy: client.createdBy,
      updatedBy: client.updatedBy,
      createdAt: Utils.formatDateToDisplay(
        client.createdAt,
        Utils.DATETIME_DISPLAY_FORMAT,
      ),
      updatedAt: Utils.formatDateToDisplay(
        client.updatedAt,
        Utils.DATETIME_DISPLAY_FORMAT,
      ),
      domain: client.domain,
      loginMethods: [],
    };

    if (client.loginMethods != null) {
      response.loginMethods = client.loginMethods.map(
        parseToLoginMethodResponse,
      );
    }
    return response;
  }
  return null;
}
