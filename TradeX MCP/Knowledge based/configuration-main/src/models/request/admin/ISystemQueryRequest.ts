export interface ISystemQueryUpdateTimeRequest {
  lastQueriedTime?: string;
}

export default interface ISystemQueryRequest
  extends ISystemQueryUpdateTimeRequest {
  domain: string;
}

export interface ISystemQueryUpdateTime {
  lastQueriedTime?: Date;
}

export interface ISystemQuery extends ISystemQueryUpdateTime {
  domain: string;
}
