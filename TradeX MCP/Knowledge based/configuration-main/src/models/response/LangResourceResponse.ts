import { LangResourceFileResponse } from "./LangResourceFileResponse";

class LangResourceResponse {
  public msName: string;
  public latestVersion: string;
  public lang: string;
  public files: LangResourceFileResponse[] = [];
}

export { LangResourceResponse };
