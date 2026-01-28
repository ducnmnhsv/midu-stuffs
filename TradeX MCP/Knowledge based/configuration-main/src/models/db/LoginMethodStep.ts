import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToMany,
  ManyToOne,
  JoinColumn,
  JoinTable,
} from "typeorm";
import ScopeGroup from "./ScopeGroup";
import LoginMethod from "./LoginMethod";

@Entity("t_login_method_step")
export default class LoginMethodStep {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "login_method_id" })
  public loginMethodId: number;

  @Column({ name: "step" })
  public step: number;

  @Column({ name: "name" })
  public name: string;

  @Column({ name: "desc" })
  public description: string;

  @ManyToOne(
    (objType: any) => LoginMethod,
    (loginMethod: LoginMethod) => loginMethod.steps,
  )
  @JoinColumn({
    name: "login_method_id",
    referencedColumnName: "id",
  })
  public loginMethod: LoginMethod;

  @ManyToMany(() => ScopeGroup, { cascade: ["insert", "update"] })
  @JoinTable({
    name: "t_login_method_step_scope_group_map",
    joinColumn: {
      name: "step_id",
      referencedColumnName: "id",
    },
    inverseJoinColumn: {
      name: "scope_group_id",
      referencedColumnName: "id",
    },
  })
  public scopeGroups: ScopeGroup[];
}
