export interface IUpdateAppVersionRequest {
  clientIds?: any;
  paaveClientId: any;
  appVersion: IAppVersion;
}

export interface IAppVersion {
  android: string;
  ios: string;
  listAndroid: any;
  listIos: any;
}
