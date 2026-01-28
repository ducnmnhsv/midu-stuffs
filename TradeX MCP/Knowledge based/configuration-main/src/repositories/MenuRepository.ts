import Menu from "../models/db/Menu";
import { AppDataSource } from "../AppDataSource";

export const MenuRepository = AppDataSource.getRepository(Menu).extend({
  findByRoleIds(menuRoleIds: number[]): Promise<Menu[]> {
    return this.createQueryBuilder("t1")
      .innerJoinAndSelect("t1.menuRoles", "t2")
      .innerJoinAndSelect("t1.menuGroup", "t3")
      .where("t2.id IN (:roleIds)", {
        roleIds: menuRoleIds,
      })
      .orderBy({
        "t3.id": "ASC",
        "t1.order": "ASC",
      })
      .getMany();
  },
});
