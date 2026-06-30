import Menu from "../../db/Menu";

const parse = (menu: Menu): MenuResponse => {
  const response: MenuResponse = new MenuResponse();
  if (menu != null) {
    response.id = menu.id;
    response.title = menu.title;
    response.order = menu.order;
    response.parent = menu.parentId;
    response.isLeaf = menu.isLeaf;
    response.href = menu.href;
    response.icon = menu.icon;
    response.screenCode = menu.screenCode;
  }

  return response;
};

class MenuResponse {
  public id: number;
  public title: string;
  public order: number;
  public icon: string;
  public parent: number;
  public isLeaf: boolean;
  public href: string;
  public screenCode: string;
}

export { parse, MenuResponse };
