import { Service } from 'typedi';
import { Matcher, Scope, ScopeData, ScopeGroup } from '../models/IScope';
import { OpenAPIV3 } from 'openapi-types';
import { Kafka, Logger } from 'tradex-common';
import config from '../config';
import { DEFAULT_PAGE_SIZE } from '../constants';
import { v4 as uuid } from 'uuid';
import { fsReadFile, fsStat } from '../promisify';
import * as fs from 'fs';

const SCOPE_KEY = 'qazxs&&wedc';

/* eslint-disable no-unused-vars */
type GetSchemaFunc = (scopeId: number) => OpenAPIV3.OperationObject | undefined;
type FindScopeWithUriFunc = (
  uriParts: string[],
  scopeDict: any,
  paramNames: string[],
  paramValues: string[],
  isPublic: boolean,
  scopeGroupIds?: number[] | undefined | null,
  index?: number | undefined,
) => Scope | undefined;
type FindScopeFunc = (uri: string, isPublic: boolean, scopeGroupIds?: number[]) => [Scope?, Matcher?];
type QueryScopeGroupsFunc = (lastSequence: number, scopeData: ScopeData) => Promise<void>;
/* eslint-enable no-unused-vars */

@Service()
export default class ScopeService {
  public scopeData!: ScopeData;
  private scopeDict = null;

  public scopeDataFileDir = config.fileDir.scope;
  public openApiFileDir = config.fileDir.openApi;

  private createNewScopeData(): ScopeData {
    return {
      t: new Date().getTime(),
      scopes: [], //Scope[]
      scopeDict: new Map(), //id - scope map
      scopeGroups: [], //ScopeGroup[]
      scopeGroupMap: new Map(), //{ [k: number]: ScopeGroup }
      publicScopes: [],
      scopeApis: [],
      scopeApiMap: new Map(), //{ [k: number]: OpenAPIV3.OperationObject }
      unmatchedOpenApiList: [],
    };
  }

  public init = async () => {
    this.scopeData = this.createNewScopeData();
    let isExist = true;
    try {
      await fsStat(this.scopeDataFileDir);
    } catch (err) {
      isExist = false;
    }
    if (isExist) {
      Logger.warn('Scope data file exists => Start server');
      const buf: Buffer = await fsReadFile(this.scopeDataFileDir);
      this.scopeData = JSON.parse(buf.toString('utf8'));
      this.scopeData.scopeDict = new Map();
      this.scopeData.scopeGroupMap = new Map();
      this.scopeData.scopeApiMap = new Map();
      if (this.scopeData.scopes != null) this.scopeData.scopes.forEach((it) => this.scopeData.scopeDict.set(it.id, it));
      if (this.scopeData.scopeGroups != null) this.scopeData.scopeGroups.forEach((it) => this.scopeData.scopeGroupMap.set(it.id, it));
      if (this.scopeData.scopeApis != null)
        this.scopeData.scopeApis.forEach((openApi) =>
          this.scopeData.scopeApiMap.set(openApi.id, {
            summary: openApi.summary,
            parameters: openApi.parameters,
            requestBody: openApi.requestBody,
            responses: openApi.responses,
            security: openApi.security,
            tags: openApi.tags,
          }),
        );
      this.scopeDict = this.createSortDict(this.scopeData.scopes);
      this.updateFromConfServiceRetry()
        .then(() => {
          this.findUnmatchedApi().then().catch(Logger.error);
        })
        .catch(Logger.error);
    } else {
      Logger.error(`Scope data file not exists, send request to configuration service`);
      // return;
      await this.updateFromConfServiceRetry();
      await this.findUnmatchedApi();
    }
    setInterval(this.updateScopeInterval, 600000);
  };

  private updateScopeInterval = async () => {
    try {
      await this.updateFromConfService();
      await this.findUnmatchedApi();
    } catch (e) {
      Logger.error('fail to update scope list', e);
    }
  };

  public updateFromConfServiceRetry: () => Promise<boolean> = async () => {
    let exit = false;
    let time = 0;
    while (!exit) {
      try {
        await this.updateFromConfService();
        exit = true;
      } catch (e) {
        if (time < 100) {
          Logger.error('fail to load from conf. will retry', time, e);
          time++;
        } else {
          Logger.error('fail to load from conf', time);
        }
      }
    }
    return exit;
  };

  public updateFromConfService = async () => {
    const scopeData: ScopeData = this.createNewScopeData();
    // this.scopeData.t = new Date().getTime();
    // this.confServiceNewScopeList = [];
    // this.scopeData =
    await this.queryOpenApiLoop(0, scopeData);
    await this.queryScopeGroupsLoop(0, scopeData);
    await this.queryScopesLoop(0, scopeData);
    // this.scopeData.scopes = this.confServiceNewScopeList;
    this.markPublicScope(scopeData);
    this.scopeData = scopeData;
    await this.saveScopeFile();
    this.scopeDict = this.createSortDict(this.scopeData.scopes);
  };

  public queryOpenApiLoop = async (lastSequence: number, scopeData: ScopeData) => {
    const openApiListRequest: any = {
      fetchCount: DEFAULT_PAGE_SIZE,
      lastSequence: lastSequence,
    };
    const message: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      uuid(),
      config.topic.configuration,
      config.uri.queryOpenApiList,
      openApiListRequest,
      20000,
    );
    const response: any[] = Kafka.getResponse(message);
    for (const openApi of response) {
      scopeData.scopeApis.push(openApi);
      scopeData.scopeApiMap.set(openApi.id, {
        summary: openApi.summary,
        parameters: openApi.parameters,
        requestBody: openApi.requestBody,
        responses: openApi.responses,
        security: openApi.security,
        tags: openApi.tags,
      });
    }

    if (response.length === DEFAULT_PAGE_SIZE) {
      await this.queryOpenApiLoop(response[response.length - 1].id, scopeData);
    }
  };

  public queryScopeGroupsLoop: QueryScopeGroupsFunc = async (lastSequence: number, scopeData: ScopeData) => {
    const scopeGroupMsg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      '',
      config.topic.configuration,
      config.uri.scopeGroup,
      {
        fetchCount: DEFAULT_PAGE_SIZE,
        lastSequence: lastSequence,
      },
      20000,
    );
    const data: ScopeGroup[] = Kafka.getResponse(scopeGroupMsg);
    for (const scopeGroup of data) {
      scopeGroup.scopes = [];
      scopeData.scopeGroupMap.set(scopeGroup.id, scopeGroup);
      scopeData.scopeGroups.push(scopeGroup);
    }
    if (data.length >= DEFAULT_PAGE_SIZE) {
      await this.queryScopeGroupsLoop(+data[data.length - 1].id, scopeData);
    }
  };

  public queryScopesLoop = async (lastSequence: number, scopeData: ScopeData) => {
    const scopeResponseMessage: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      '',
      config.topic.configuration,
      config.uri.getAllScopes,
      {
        fetchCount: DEFAULT_PAGE_SIZE,
        lastSequence: lastSequence,
      },
      20000,
    );
    const scopeList: Scope[] = Kafka.getResponse(scopeResponseMessage);
    Logger.info('loading scopes', scopeList.length);
    for (const scope of scopeList) {
      this.processUri(scope);
      scopeData.scopes.push(scope);
      scopeData.scopeDict.set(scope.id, scope);

      if (scope.scopeGroupIds != null && scope.scopeGroupIds.length > 0) {
        scope.scopeGroupIds.forEach((scopeGroupId: number) => {
          const scs: ScopeGroup | undefined = scopeData.scopeGroupMap.get(scopeGroupId);
          if (scs == null) {
            scopeData.scopeGroupMap.set(scopeGroupId, {
              id: scopeGroupId,
              scopeGroupName: 'UNKNOWN',
              scopes: [scope.id],
            });
          } else {
            scs.scopes.push(scope.id);
          }
        });
      }
    }
    if (scopeList.length >= DEFAULT_PAGE_SIZE) {
      await this.queryScopesLoop(scopeList[scopeList.length - 1].id, scopeData);
    }
  };

  public markPublicScope = (scopeData: ScopeData) => {
    const publicScopes: Scope[] = [];
    const publicScopeGroups: string[] = config.scopes.publicScopeGroups;
    publicScopeGroups.forEach((scopeGroupName: string) => {
      const scopeGroup: ScopeGroup | undefined = scopeData.scopeGroups.find((scopeGroup: ScopeGroup) => {
        return scopeGroup.scopeGroupName === scopeGroupName;
      });
      if (scopeGroup != null) {
        scopeGroup.scopes.forEach((id: number) => {
          const scope: Scope | undefined = scopeData.scopeDict.get(id);
          if (scope != null) {
            scope.isPublic = true;
            publicScopes.push(scope);
          }
        });
      }
    });

    scopeData.publicScopes = publicScopes.map((scope: Scope) => scope.id);
  };

  public findScope: FindScopeFunc = (uri: string, isPublic: boolean, scopeGroupIds: number[] = []) => {
    if (this.scopeDict == null) {
      throw new Error('GATEWAY_STARTING');
    }
    const parts = uri.split('/').filter((item) => item !== '');
    const paramNames: string[] = [];
    const paramValues: string[] = [];
    const scope = this.findScopeUriWithIndex(parts, this.scopeDict, paramNames, paramValues, isPublic, scopeGroupIds, 0);
    const matcher: Matcher = {
      remainingPathname: '',
      paramNames: paramNames,
      paramValues: paramValues,
    };
    return [scope, matcher];
  };

  private findScopeUriWithIndex: FindScopeWithUriFunc = (
    uriParts: string[],
    scopeDict: any,
    paramNames: string[],
    paramValues: string[],
    isPublic: boolean,
    scopeGroupIds: number[] | undefined | null = undefined,
    index: number | undefined = 0,
  ) => {
    if (index === uriParts.length) {
      const array = scopeDict[SCOPE_KEY];
      if (array != null) {
        for (const element of array) {
          const scope = element;
          if ((scope.isPublic !== true) === (isPublic !== true)) {
            if (isPublic || scopeGroupIds == null || scopeGroupIds.find((id) => scope.scopeGroupIds.indexOf(id) > -1) != null) {
              return scope.scope;
            }
          }
        }
      }
      return undefined;
    }

    const part = uriParts[index];
    let insideDict = scopeDict[part];
    if (insideDict != null) {
      const result = this.findScopeUriWithIndex(uriParts, insideDict, paramNames, paramValues, isPublic, scopeGroupIds, index + 1);
      if (result != null) {
        return result;
      }
    }
    const pathParams = Object.keys(scopeDict).filter((key) => key.startsWith(':'));
    for (const element of pathParams) {
      const paramName = element;
      insideDict = scopeDict[paramName];
      const scopeId = this.findScopeUriWithIndex(uriParts, insideDict, paramNames, paramValues, isPublic, scopeGroupIds, index + 1);
      if (scopeId != null) {
        paramNames.push(paramName.substring(1));
        paramValues.push(part);
        return scopeId;
      }
    }
    return undefined;
  };

  public createSortDict = (scopes: Scope[]) => {
    const sortDict: any = {};
    for (const scope of scopes) {
      const scopeInDict = this.scopeData.scopeDict.get(scope.id);
      if (scopeInDict != null) {
        scope.isPublic = scopeInDict.isPublic;
      }
      const uri = scope.processedPattern;
      const partList: string[] = uri.split('/').filter((key) => key !== '');
      let currentPosition = sortDict;
      for (let i = 0; i < partList.length; i++) {
        const part = partList[i];
        if (currentPosition[part] == null) {
          currentPosition[part] = {};
        }
        currentPosition = currentPosition[part];
      }
      let array = currentPosition[SCOPE_KEY];
      if (array == null) {
        array = [];
        currentPosition[SCOPE_KEY] = array;
      }
      const data = {
        ...scope,
        scope,
      };
      if (typeof data.id === 'string') {
        data.id = parseInt(data.id); //tslint:disable-line
      }

      if (data.scopeGroupIds != null) {
        for (let i = 0; i < data.scopeGroupIds.length; i++) {
          const value = data.scopeGroupIds[i];
          if (typeof value === 'string') {
            data.scopeGroupIds[i] = parseInt(value); //tslint:disable-line
          }
        }
      }
      array.push(data);

      currentPosition = sortDict;
    }
    return sortDict;
  };

  public processUri = (scope: Scope) => {
    const uri = `/${scope.uriPattern.replace(':', '')}`;
    const parts: string[] = uri.split('/');
    parts.forEach((part: string) => {
      let p = part;
      if (part.startsWith('{') && part.endsWith('}')) {
        p = `:${part.substr(1, part.length - 2)}`;
      }
      if (scope.processedPattern == null) {
        scope.processedPattern = p;
      } else {
        scope.processedPattern += `/${p}`;
      }
    });
  };

  public saveScopeFile = async () => {
    const content: string = JSON.stringify(this.scopeData, null, 2);
    fs.writeFileSync(this.scopeDataFileDir, content);
    Logger.info(`created scope data file at ${this.scopeDataFileDir} `);
  };

  public async findUnmatchedApi() {
    Logger.info(`total scope: ${this.scopeData.scopes.length}`);
    let matchedCount = 0;
    for (const [value, data] of Object.entries(this.scopeData.scopeDict)) {
      if (this.scopeData.scopeApiMap.get(+value) == null) {
        matchedCount++;
        Logger.warn(`scope does not have open api: ${data.uriPattern}`);
      }
    }
    Logger.warn(`total unmatched scope: ${matchedCount}`);
  }

  public getSchema: GetSchemaFunc = (scopeId: number) => {
    return this.scopeData.scopeApiMap!.get(scopeId);
  };
}
