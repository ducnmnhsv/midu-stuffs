export default interface IRefreshTokenReq {
  grant_type: string;
  client_id: string;
  client_secret: string;
  refresh_token: string;
}
