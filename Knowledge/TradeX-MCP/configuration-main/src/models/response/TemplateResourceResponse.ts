import TemplateResource from "../db/TemplateResource";

const parse = (
  templateResource: TemplateResource,
): TemplateResourceResponse => {
  const response: TemplateResourceResponse = new TemplateResourceResponse();
  if (templateResource != null) {
    response.name = templateResource.name;
    response.msName = templateResource.msName;
    response.lang = templateResource.lang;
    response.url = templateResource.url;
  }

  return response;
};

class TemplateResourceResponse {
  public name: string;
  public msName: string;
  public lang: string;
  public url: string;
}

export { parse, TemplateResourceResponse };
