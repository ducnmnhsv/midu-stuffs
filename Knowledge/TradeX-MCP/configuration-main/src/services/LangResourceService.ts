import { Service } from "typedi";
import axios, { AxiosResponse } from "axios";
import { LangResourceFileRepository } from "../repositories/LangResourceFileRepository";
import { LangResourceResponse } from "../models/response/LangResourceResponse";
import { Errors, Utils } from "tradex-common";
import ILangResourceRequest from "../models/request/ILangResourceRequest";
import {
  LangResourceFileResponse,
  parse as parseFile,
} from "../models/response/LangResourceFileResponse";
import LangResourceFile from "../models/db/LangResourceFile";
import LangResourceVersion from "../models/db/LangResourceVersion";

@Service()
export default class LangResourceService {
  public async getAllResources(
    request: ILangResourceRequest,
  ): Promise<LangResourceResponse[]> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.msNames, "msNames")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();
    const langResourceFiles: LangResourceFile[] =
      await LangResourceFileRepository.findByMsName(request.msNames);
    if (langResourceFiles == null) {
      throw new Errors.ObjectNotFoundError();
    }
    const langResourceResponses: LangResourceResponse[] = [];
    let langResourceResponse: LangResourceResponse = null;

    langResourceFiles.forEach((langResourceFile: LangResourceFile) => {
      if (
        langResourceResponse == null ||
        langResourceResponse.msName !==
          langResourceFile.langNamespace.langResource.msName ||
        langResourceResponse.lang !== langResourceFile.lang
      ) {
        if (langResourceResponse != null) {
          langResourceResponses.push(langResourceResponse);
        }

        langResourceResponse = new LangResourceResponse();
        langResourceResponse.msName =
          langResourceFile.langNamespace.langResource.msName;
        langResourceResponse.lang = langResourceFile.lang;
        langResourceResponse.latestVersion =
          langResourceFile.langNamespace.langResource.langResourceVersions.find(
            (value: LangResourceVersion) =>
              value.lang === langResourceResponse.lang,
          ).version;
      }

      langResourceResponse.files.push(parseFile(langResourceFile));
    });

    if (langResourceResponse != null) {
      langResourceResponses.push(langResourceResponse);
    }

    return langResourceResponses;
  }

  public async getAllResourcesForInternal(
    request: ILangResourceRequest,
  ): Promise<LangResourceResponse[]> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.msNames, "msNames")
      .setRequire()
      .throwValid(invalidParams);
    invalidParams.throwErr();

    const langResourceFiles: LangResourceFile[] =
      await LangResourceFileRepository.findByMsName(request.msNames);
    if (langResourceFiles == null) {
      throw new Errors.ObjectNotFoundError();
    }
    let langResourceResponses: LangResourceResponse[] = [];
    let langResourceResponse: LangResourceResponse = null;

    langResourceFiles.forEach((langResourceFile: LangResourceFile) => {
      if (
        langResourceResponse == null ||
        langResourceResponse.msName !==
          langResourceFile.langNamespace.langResource.msName ||
        langResourceResponse.lang !== langResourceFile.lang
      ) {
        if (langResourceResponse != null) {
          langResourceResponses.push(langResourceResponse);
        }

        langResourceResponse = new LangResourceResponse();
        langResourceResponse.msName =
          langResourceFile.langNamespace.langResource.msName;
        langResourceResponse.lang = langResourceFile.lang;
        langResourceResponse.latestVersion =
          langResourceFile.langNamespace.langResource.langResourceVersions.find(
            (value: LangResourceVersion) =>
              value.lang === langResourceResponse.lang,
          ).version;
      }

      langResourceResponse.files.push(parseFile(langResourceFile));
    });

    if (langResourceResponse != null) {
      langResourceResponses.push(langResourceResponse);
    }

    const promises: Promise<LangResourceResponse>[] = langResourceResponses.map(
      (response: LangResourceResponse) => this.fetchLangResource(response),
    );
    langResourceResponses = await Promise.all(promises);
    return langResourceResponses;
  }

  private async fetchLangResource(
    langResource: LangResourceResponse,
  ): Promise<LangResourceResponse> {
    const promises = langResource.files.map((file: LangResourceFileResponse) =>
      this.fetchFileContent(file),
    );

    const langResourceFileResponses: LangResourceFileResponse[] =
      await Promise.all(promises);
    langResource.files = langResourceFileResponses;
    return langResource;
  }

  private async fetchFileContent(
    file: LangResourceFileResponse,
  ): Promise<LangResourceFileResponse> {
    const content: AxiosResponse = await axios.get(file.url);
    file.content = content.data;
    return file;
  }
}
