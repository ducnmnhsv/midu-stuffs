import { OpenAPIV3 } from 'openapi-types';
import { Models } from 'tradex-common';

export interface ScopeData {
  t?: number; // msTime
  scopes: Scope[];
  scopeDict: Map<number, Scope>;
  scopeGroups: ScopeGroup[];
  scopeGroupMap: Map<number, ScopeGroup>;
  publicScopes: number[];
  scopeApis: any[];
  scopeApiMap: Map<number, OpenAPIV3.OperationObject>;
  unmatchedOpenApiList: any[];
}

export interface Matcher {
  remainingPathname: string;
  paramValues: string[];
  paramNames: string[];
}

export interface OpenApiMap {
  [k: string]: OpenAPIV3.OperationObject;
}

export interface Scope extends Models.AAA.IScope {
  id: number;
  isPublic: boolean;
  scopeGroupIds: number[];
  processedPattern: string;
}

export interface ScopeGroup {
  id: number;
  scopeGroupName: string;
  scopes: number[];
}
