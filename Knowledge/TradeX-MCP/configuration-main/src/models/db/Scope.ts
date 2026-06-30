import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToMany,
  CreateDateColumn,
  UpdateDateColumn,
} from "typeorm";
import { Models, Utils } from "tradex-common";
import {
  TradexModelsConfiguration,
  TradexModelsCommon,
} from "tradex-models-ts";
import ScopeGroup from "./ScopeGroup";

@Entity("t_scope")
export default class Scope {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public name: string;

  @Column({ name: "uri_pattern" })
  public uriPattern: string;

  @Column({ name: "created_by" })
  public createdBy: string;

  @Column({ name: "updated_by" })
  public updatedBy: string;

  @CreateDateColumn({ name: "created_at" })
  public createdAt: Date;

  @UpdateDateColumn({ name: "updated_at" })
  public updatedAt: Date;

  @Column("enum", { enum: Models.AAA.ForwardType, name: "forward_type" })
  public forwardType: Models.AAA.ForwardType;

  @Column({ type: "json", name: "forward_data" })
  public forwardData: TradexModelsCommon.ForwardData;

  @ManyToMany(
    (objType: any) => ScopeGroup,
    (scopeGroup: ScopeGroup) => scopeGroup.scopes,
    { cascade: ["insert", "update"] },
  )
  public scopeGroups: ScopeGroup[];
}

export function parseToScope(
  scope: Scope,
): TradexModelsConfiguration.ScopeResponse | undefined {
  if (scope != null) {
    const response: TradexModelsConfiguration.ScopeResponse = {
      id: +scope.id,
      name: scope.name,
      uriPattern: scope.uriPattern,
      createdBy: scope.createdBy,
      updatedBy: scope.updatedBy,
      forwardData: scope.forwardData,
      forwardType: scope.forwardType,
      createdAt: Utils.formatDateToDisplay(
        scope.createdAt,
        Utils.DATETIME_DISPLAY_FORMAT,
      ),
      updatedAt: Utils.formatDateToDisplay(
        scope.updatedAt,
        Utils.DATETIME_DISPLAY_FORMAT,
      ),
    };
    if (scope.scopeGroups != null) {
      response.scopeGroupIds = scope.scopeGroups.map(
        (scopeGroup: ScopeGroup) => scopeGroup.id,
      );
    }
    return response;
  }

  return undefined;
}
