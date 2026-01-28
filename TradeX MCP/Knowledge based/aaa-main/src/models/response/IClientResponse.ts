interface ILoginMethodStepResponse {
  id?: number;
  loginMethodId?: number;
  step?: number;
  name?: string;
  description?: string;
  scopeGroupIds?: number[];
}

interface ILoginMethodResponse {
  id?: number;
  serviceCode?: string;
  grantType?: string;
  msName?: string;
  msUri?: string;
  isDefault?: boolean;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
  scopeGroupIds?: number[];
  accessTokenTtl?: number,
  refreshTokenTtl?: number,
  refreshTokenLongTtl?: number,
  multiFactorTtl?: number,
  steps?: ILoginMethodStepResponse[];
  extraData?: string;
}

interface IClientResponse {
  id?: number;
  userId?: number;
  clientId?: string;
  clientSecret?: string;
  description?: string;
  status?: number;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
  domain?: string;
  appVersion?: string;
  loginMethods?: ILoginMethodResponse[];
}

interface IClientForUpdateResponse {
  clients?: IClientResponse[];
  lastQueriedTime?: string;
}
