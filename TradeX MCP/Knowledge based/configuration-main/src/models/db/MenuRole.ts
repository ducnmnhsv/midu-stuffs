import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  ManyToMany,
} from "typeorm";
import MenuGroup from "./MenuGroup";
import Menu from "./Menu";

@Entity("t_menu_role")
export default class MenuRole {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public code: string;

  @Column()
  public name: string;

  @Column()
  public description: string;

  @ManyToOne(
    (objType: any) => MenuGroup,
    (menuGroup: MenuGroup) => menuGroup.menuRoles,
  )
  @JoinColumn({
    name: "menu_group_id",
    referencedColumnName: "id",
  })
  public menuGroup: MenuGroup;

  @ManyToMany((objType: any) => Menu, (menu: Menu) => menu.menuRoles)
  public menus: Menu[];
}
