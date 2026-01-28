import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToMany,
  JoinTable,
  CreateDateColumn,
  UpdateDateColumn,
  OneToMany,
} from "typeorm";
import Client from "./Client";
import ScopeGroup from "./ScopeGroup";
import { Utils } from "tradex-common";
import { TradexModelsConfiguration } from "tradex-models-ts";
import LoginMethodStep from "./LoginMethodStep";

@Entity("t_login_method")
export default class LoginMethod {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "service_code" })
  public serviceCode: string;

  @Column({ name: "grant_type" })
  public grantType: string;

  @Column({ name: "ms_name" })
  public msName: string;

  @Column({ name: "is_default" })
  public isDefault: boolean;

  @Column({ name: "created_by" })
  public createdBy: string;

  @Column({ name: "updated_by" })
  public updatedBy: string;

  @CreateDateColumn({ name: "created_at" })
  public createdAt: Date;

  @UpdateDateColumn({ name: "updated_at" })
  public updatedAt: Date;

  @Column({ name: "ms_uri" })
  public msUri: string;

  @Column({ name: "access_token_ttl" })
  public accessTokenTtl: number;

  @Column({ name: "refresh_token_ttl" })
  public refreshTokenTtl: number;

  @Column({ name: "refresh_token_long_ttl" })
  public refreshTokenLongTtl: number;

  @Column({ name: "multi_factor_ttl" })
  public multiFactorTtl: number;

  @Column({ name: "public_scopes" })
  public publicScopes: string;

  @Column({ name: "extra_data" })
  public extraData: string;

  @ManyToMany((objType: any) => Client, (client: Client) => client.loginMethods)
  @JoinTable({
    name: "t_client_login_method_map",
    joinColumn: {
      name: "login_method_id",
    },
    inverseJoinColumn: {
      name: "client_id",
    },
  })
  public clients: Client[];

  @ManyToMany(
    (objType: any) => ScopeGroup,
    (scopeGroup: ScopeGroup) => scopeGroup.loginMethods,
    { cascade: ["insert", "update"] },
  )
  public scopeGroups: ScopeGroup[];

  @OneToMany(
    (objType: any) => LoginMethodStep,
    (menuGroup: LoginMethodStep) => menuGroup.loginMethod,
  )
  public steps: LoginMethodStep[];
}

export function parseToLoginMethodResponse(
  loginMethod: LoginMethod,
): TradexModelsConfiguration.LoginMethodResponse {
  if (loginMethod != null) {
    const response: TradexModelsConfiguration.LoginMethodResponse = {
      id: loginMethod.id,
      serviceCode: loginMethod.serviceCode,
      grantType: loginMethod.grantType,
      msName: loginMethod.msName,
      msUri: loginMethod.msUri,
      isDefault: loginMethod.isDefault,
      createdBy: loginMethod.createdBy,
      updatedBy: loginMethod.updatedBy,
      createdAt: Utils.formatDateToDisplay(
        loginMethod.createdAt,
        Utils.DATETIME_DISPLAY_FORMAT,
      ),
      updatedAt: Utils.formatDateToDisplay(
        loginMethod.updatedAt,
        Utils.DATETIME_DISPLAY_FORMAT,
      ),
      accessTokenTtl: loginMethod.accessTokenTtl,
      refreshTokenTtl: loginMethod.refreshTokenTtl,
      refreshTokenLongTtl: loginMethod.refreshTokenLongTtl,
      multiFactorTtl: loginMethod.multiFactorTtl,
      publicScopes: loginMethod.publicScopes,
      extraData: loginMethod.extraData,
    };

    if (loginMethod.scopeGroups != null) {
      response.scopeGroupIds = loginMethod.scopeGroups.map(
        (scopeGroup: ScopeGroup) => scopeGroup.id,
      );
    }
    return response;
  }
  return null;
}
