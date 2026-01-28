import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
  ManyToOne,
  JoinColumn,
} from "typeorm";
import Menu from "./Menu";
import AdminRole from "./AdminRole";
import MenuRole from "./MenuRole";

@Entity("t_menu_group")
export default class MenuGroup {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "group_name" })
  public groupName: string;

  @Column()
  public description: string;

  @OneToMany((objType: any) => Menu, (menu: Menu) => menu.menuGroup)
  public menus: Menu[];

  @OneToMany(
    (objType: any) => MenuRole,
    (menuRole: MenuRole) => menuRole.menuGroup,
  )
  public menuRoles: MenuRole[];

  @ManyToOne(
    (objType: any) => AdminRole,
    (adminRole: AdminRole) => adminRole.menuGroups,
  )
  @JoinColumn({
    name: "admin_role_id",
    referencedColumnName: "id",
  })
  public adminRole: AdminRole;
}
