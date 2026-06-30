import { Service } from "typedi";
import { MenuRepository } from "../../repositories/MenuRepository";
import Menu from "../../models/db/Menu";
import IMenuQueryRequest from "../../models/request/admin/IMenuQueryRequest";
import { parse } from "../../models/response/admin/MenuResponse";
import {
  MenuGroupResponse,
  parse as parseGroup,
} from "../../models/response/admin/MenuGroupResponse";

@Service()
export default class MenuService {
  public async findMenuByRoleIds(
    request: IMenuQueryRequest,
  ): Promise<MenuGroupResponse[]> {
    const menus: Menu[] = await MenuRepository.findByRoleIds(
      request.menuRoleIds,
    );
    const menuGroupResponses = [];
    let menuGroupResponse: MenuGroupResponse = null;
    menus.forEach((menu: Menu) => {
      if (
        menuGroupResponse == null ||
        menuGroupResponse.id !== menu.menuGroup.id
      ) {
        if (menuGroupResponse != null) {
          menuGroupResponses.push(menuGroupResponse);
        }
        menuGroupResponse = parseGroup(menu.menuGroup);
      }

      menuGroupResponse.menus.push(parse(menu));
    });

    if (menuGroupResponse != null) {
      menuGroupResponses.push(menuGroupResponse);
    }

    return menuGroupResponses;
  }
}
