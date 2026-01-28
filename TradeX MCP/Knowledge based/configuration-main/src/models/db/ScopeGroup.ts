import {
  Column,
  CreateDateColumn,
  Entity,
  JoinTable,
  ManyToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from "typeorm";
import LoginMethod from "./LoginMethod";
import Scope from "./Scope";
import { TradexModelsConfiguration } from "tradex-models-ts";

@Entity("t_scope_group")
export default class ScopeGroup {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "scope_group_name" })
  public name: string;

  @Column({ name: "created_by" })
  public createdBy: string;

  @Column({ name: "updated_by" })
  public updatedBy: string;

  @CreateDateColumn({ name: "created_at" })
  public createdAt: Date;

  @UpdateDateColumn({ name: "updated_at" })
  public updatedAt: Date;

  @ManyToMany(
    (objType: any) => LoginMethod,
    (loginMethod: LoginMethod) => loginMethod.scopeGroups,
  )
  @JoinTable({
    name: "t_login_method_scope_group_map",
    joinColumn: {
      name: "scope_group_id",
      referencedColumnName: "id",
    },
    inverseJoinColumn: {
      name: "login_method_id",
      referencedColumnName: "id",
    },
  })
  public loginMethods: LoginMethod[];

  @ManyToMany((objType: any) => Scope, (scope: Scope) => scope.scopeGroups, {
    cascade: true,
  })
  @JoinTable({
    name: "t_scope_scope_group_map",
    joinColumn: {
      name: "scope_group_id",
    },
    inverseJoinColumn: {
      name: "scope_id",
    },
  })
  public scopes: Scope[];
}

export function parseScopeGroup(
  scopeGroup: ScopeGroup,
): TradexModelsConfiguration.ScopeGroupResponse {
  if (scopeGroup != null) {
    const response: TradexModelsConfiguration.ScopeGroupResponse = {
      id: scopeGroup.id,
      scopeGroupName: scopeGroup.name,
    };

    if (scopeGroup.scopes != null) {
      response.scopeIds = scopeGroup.scopes.map((scope: Scope) => scope.id);
    }
    return response;
  }
  return null;
}
