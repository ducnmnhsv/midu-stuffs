import { Entity, PrimaryGeneratedColumn, Column, OneToMany } from "typeorm";
import MenuGroup from "./MenuGroup";

@Entity("t_admin_role")
export default class AdminRole {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public role: string;

  @Column()
  public description: string;

  @OneToMany(
    (objType: any) => MenuGroup,
    (menuGroup: MenuGroup) => menuGroup.adminRole,
  )
  public menuGroups: MenuGroup[];
}
