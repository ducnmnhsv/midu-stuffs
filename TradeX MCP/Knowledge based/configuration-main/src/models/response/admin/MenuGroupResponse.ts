import { MenuResponse, parse as parseMenu } from "./MenuResponse";
import MenuGroup from "../../db/MenuGroup";

const parse = (
  menuGroup: MenuGroup,
  loadAllMenus: boolean = false,
): MenuGroupResponse => {
  const response: MenuGroupResponse = new MenuGroupResponse();
  if (menuGroup != null) {
    response.id = menuGroup.id;
    response.groupName = menuGroup.groupName;
    if (loadAllMenus) {
      response.menus = menuGroup.menus.map(parseMenu);
    } else {
      response.menus = [];
    }
  }

  return response;
};

class MenuGroupResponse {
  public id: number;
  public groupName: string;
  public menus: MenuResponse[];
}

export { MenuGroupResponse, parse };
