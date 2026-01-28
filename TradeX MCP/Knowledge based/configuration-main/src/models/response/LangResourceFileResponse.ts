import LangResourceFile from "../db/LangResourceFile";

const parse = (
  langResourceFile: LangResourceFile,
): LangResourceFileResponse => {
  const response: LangResourceFileResponse = new LangResourceFileResponse();
  if (langResourceFile != null) {
    response.namespace = langResourceFile.langNamespace.namespace;
    response.url = langResourceFile.url;
  }

  return response;
};

class LangResourceFileResponse {
  public namespace: string;
  public url: string;
  public content: any;
}

export { parse, LangResourceFileResponse };
