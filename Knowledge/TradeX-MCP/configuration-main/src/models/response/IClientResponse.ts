import { ILoginMethodResponse } from "./ILoginMethodResponse";

/**
 * request for query list of holiday
 */
export interface IClientResponse {
  /**
   * id
   */
  id: number;
  /**
   * user id that client belong to
   */
  userId: number;
  /**
   * clientId
   */
  clientId: string;
  /**
   * clientSecret
   */
  clientSecret: string;
  /**
   * description of client for which purpose of this client
   */
  description: string;
  /**
   * status of client. active or not. unsed yet
   */
  status: number;
  /**
   * domain of client
   */
  domain: string;
  /**
   * login methods available for client
   */
  appVersion?: string;

  loginMethods?: ILoginMethodResponse[];
  [k: string]: any;
}
