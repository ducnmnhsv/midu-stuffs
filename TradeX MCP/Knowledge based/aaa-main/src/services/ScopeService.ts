import {Logger, Models, Utils} from "tradex-common";
import {connectAndDo, Connection, getConnectAndQuery} from "../db/async";
import Scope from "../models/db/Scope";
import {Query} from "../models/db/BaseModel";
import ScopeScopeGroupMap from "../models/db/ScopeScopeGroupMap";
import LoginMethodScopeGroupMap from "../models/db/LoginMethodScopeGroupMap";
import {FieldInfo} from "mysql";
import ScopeGroup from "../models/db/ScopeGroup";
import conf from "../conf";
import LoginMethodStepScopeGroupMap from "../models/db/LoginMethodStepScopeGroupMap";
import LoginMethodStep from "../models/db/LoginMethodStep";

interface ISpecialScope {
  verifyOtp: ScopeGroup | null;
  unAuthenticated: ScopeGroup | null;
}

const specialScopes: ISpecialScope = {
  verifyOtp: null,
  unAuthenticated: null,
};

class ScopeService {
  private scopeMap: Map<number, Scope> = null;
  private scopeGroupMap: Map<number, Scope[]> = null;

  public async init(maxRetryTime: number, finish?: () => void): Promise<any> {
    await Utils.asyncWithRetry(() => this.load(), maxRetryTime);
    finish();
  }

  public async load(): Promise<any> {
    await this.loadAllScopes();
    await this.loadAllScopeGroups();
    const data: Map<string, ScopeGroup> = await this.loadSpecialScopeGroups([conf.scopes.verifyOtpScope
      , conf.scopes.unAuthenticated]);
    specialScopes.verifyOtp = data.get(conf.scopes.verifyOtpScope);
    specialScopes.unAuthenticated = data.get(conf.scopes.unAuthenticated);
    Logger.info("finish loading scope");
  }

  public async findScopeGroupsAsync(loginMethodId: number, connection?: Connection): Promise<LoginMethodScopeGroupMap[]> {
    return connectAndDo((con: Connection) => this.realFindScopeGroupsAsync(loginMethodId, con), connection);
  }

  public async findScopeGroupStepsAsync(loginMethodId: number, step: number, connection?: Connection): Promise<LoginMethodStepScopeGroupMap[]> {
    return connectAndDo((con: Connection) => this.realFindScopeGroupStepsAsync(loginMethodId, step, con), connection);
  }

  public getScopesByScopeGroups(scopeGroupIds: number[]): Scope[] {
    this.scopeGroupMap.forEach((value: Scope[], key: number) => Logger.warn(key, value.map((v: Scope) => v.id.get()).join(",")));
    Logger.info(scopeGroupIds);
    const sgIds: number[] = scopeGroupIds ? scopeGroupIds : [];
    if (specialScopes.unAuthenticated) {
      sgIds.push(specialScopes.unAuthenticated.id.get());
    }
    const results: Scope[] = [];
    Logger.info(sgIds, this.scopeGroupMap.keys());
    sgIds.forEach((sgId: number) => {
      const scopes: Scope[] = this.scopeGroupMap.get(sgId);
      if (scopes != null && scopes.length > 0) {
        results.push(...scopes);
      }
    });
    return results;
  }

  private async loadSpecialScopeGroups(scopeGroupNames: string[]): Promise<Map<string, ScopeGroup>> {
    const queryObj: Query<ScopeGroup> = new Query<ScopeGroup>(
      new ScopeGroup()).where((sg: ScopeGroup) => sg.name, "in (?)");
    Logger.info("loading sepcial scope groups", scopeGroupNames);
    const pair: Models.Pair<any, FieldInfo[]> = await getConnectAndQuery(queryObj.select(), [scopeGroupNames]);
    const map: Map<string, ScopeGroup> = new Map<string, ScopeGroup>();
    for (let i: number = 0; i < pair.left.length; i++) {
      const sc: ScopeGroup = new ScopeGroup(pair.left[i]);
      map.set(sc.name.get(), sc);
    }
    Logger.info("****loaded sepcial scope group", map.size);
    return map;
  }

  private async loadAllScopeGroups(): Promise<any> {
    Logger.info("loading scope group");
    const queryObj: Query<ScopeScopeGroupMap> = new Query<ScopeScopeGroupMap>(
      new ScopeScopeGroupMap()).order((ssg: ScopeScopeGroupMap) => ssg.groupId);
    const pair: Models.Pair<any, FieldInfo[]> = await getConnectAndQuery(queryObj.select(), []);
    let currentSGId: number = -99999;
    let currentSG: Scope[] = [];
    const scopeGroupMap: Map<number, Scope[]> = new Map();
    pair.left.forEach((item: any) => {
      const ssg: ScopeScopeGroupMap = new ScopeScopeGroupMap(item);
      if (currentSGId < 0 || currentSGId !== ssg.groupId.get()) {
        currentSGId = ssg.groupId.get();
        currentSG = [];
        scopeGroupMap.set(currentSGId, currentSG);
      }
      currentSG.push(this.scopeMap.get(ssg.scopeId.get()));
    });
    this.scopeGroupMap = scopeGroupMap;
    this.scopeGroupMap.forEach((value: Scope[], key: number) => Logger.warn(key, value.length));
    Logger.info("****loaded scope group", this.scopeGroupMap.size);
  }

  private async loadAllScopes(): Promise<any> {
    const scopeMap: Map<number, Scope> = new Map();
    Logger.info("loading scope");
    const queryObj: Query<Scope> = new Query<Scope>(new Scope({}));
    const pair: Models.Pair<any, FieldInfo[]> = await getConnectAndQuery(queryObj.select(), []);
    pair.left.forEach((sc: any) => {
      const scope: Scope = new Scope(sc);
      if (scope.hasForwardData()) {
        scope.getForwardData();
      }
      scopeMap.set(scope.id.get(), scope);
    });
    this.scopeMap = scopeMap;
    Logger.info("****loaded scope", this.scopeMap.size);
  }

  private async realFindScopeGroupsAsync(loginMethodId: number, connection: Connection): Promise<LoginMethodScopeGroupMap[]> {
    const queryObs: Query<LoginMethodScopeGroupMap> = new Query<LoginMethodScopeGroupMap>(
      new LoginMethodScopeGroupMap({})).where(
      (m: LoginMethodScopeGroupMap) => m.loginMethodId, "=?");
    const pair: Models.Pair<any, FieldInfo[]> = await connection.query(queryObs.select(), [loginMethodId]);
    return pair.left.map((lgsc: any) => new LoginMethodScopeGroupMap(lgsc));
  }

  private async realFindScopeGroupStepsAsync(loginMethodId: number, step: number, connection: Connection): Promise<LoginMethodStepScopeGroupMap[]> {
    const queryObs: Query<LoginMethodStepScopeGroupMap> = new Query<LoginMethodStepScopeGroupMap>(
      new LoginMethodStepScopeGroupMap({}));
    const join = queryObs.join(
      new LoginMethodStep({}),
      (s: LoginMethodStepScopeGroupMap) => s.stepId,
      (lm: LoginMethodStep) => lm.id
    );
    queryObs.whereAndCondition(join.fieldCondition((lm: LoginMethodStep) => lm.loginMethodId, "= ?")).
        addCondition(join.fieldCondition((lm: LoginMethodStep) => lm.step, "= ?"));
    const pair: Models.Pair<any, FieldInfo[]> = await connection.query(queryObs.select(), [loginMethodId, step]);
    return pair.left.map((lgsc: any) => new LoginMethodStepScopeGroupMap(lgsc));
  }
}

const scopeService: ScopeService = new ScopeService();

export {
  ScopeService,
  scopeService,
  specialScopes,
};
