import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  JoinColumn,
  ManyToOne,
  OneToMany,
  ManyToMany,
  JoinTable,
} from "typeorm";
import MenuGroup from "./MenuGroup";
import MenuRole from "./MenuRole";

@Entity("t_menu")
export default class Menu {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public title: string;

  @Column()
  public order: number;

  @Column()
  public icon: string;

  @Column({ name: "is_leaf" })
  public isLeaf: boolean;

  @Column()
  public href: string;

  @Column({ name: "screen_code" })
  public screenCode: string;

  @Column({ name: "menu_group_id" })
  public groupId: number;

  @Column({ name: "parent_id" })
  public parentId: number;

  @ManyToOne(
    (objType: any) => MenuGroup,
    (menuGroup: MenuGroup) => menuGroup.menus,
  )
  @JoinColumn({
    name: "menu_group_id",
    referencedColumnName: "id",
  })
  public menuGroup: MenuGroup;

  @ManyToOne((objType: any) => Menu, (menu: Menu) => menu.children)
  @JoinColumn({
    name: "parent_id",
    referencedColumnName: "id",
  })
  public parent: Menu;

  @OneToMany((objType: any) => Menu, (menu: Menu) => menu.parent)
  public children: Menu[];

  @ManyToMany(
    (objType: any) => MenuRole,
    (menuRole: MenuRole) => menuRole.menus,
  )
  @JoinTable({
    name: "t_menu_role_menu_map",
    joinColumn: {
      name: "menu_id",
    },
    inverseJoinColumn: {
      name: "menu_role_id",
    },
  })
  public menuRoles: MenuRole[];
}
