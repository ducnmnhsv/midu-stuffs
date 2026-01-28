import * as nanomatch from "nanomatch";
import {scopeService} from "../services/ScopeService";
import {Kafka, Logger, Models} from "tradex-common";
import Scope from "../models/db/Scope";
import {ITransaction} from "../models/db/Transaction";
import forwardMessageWithScope from "../services/ForwardMessageService";

const logger = Logger;

function handleMessage(msg: Kafka.IMessage): Promise<any> | boolean {
  // check token
  logger.info("handle forward msg {}", msg);
  const headers: Models.IHeaders = msg.data.headers;
  const token: Models.IToken = headers ? headers.token : null;
  const transaction: ITransaction = !token ? null : {
    user_id: token.userId,
    scope_id: null,
    service_user_id: token.serviceUserId,
    client_id: token.clientId,
    refresh_token_id: token.refreshTokenId,
    transaction_id: msg.transactionId as string,
    data: JSON.stringify(msg),
    to_topic: null,
    to_uri: null,
  };
  const sgIds: number[] = (token && token.scopeGroupIds) ? token.scopeGroupIds : [];
  const scopes: Scope[] = scopeService.getScopesByScopeGroups(sgIds);
  for (let i: number = 0; i < scopes.length; i++) {
    const scope: Scope = scopes[i];
    const res: string[] = nanomatch(msg.uri, scope.uriPattern.get());
    if (res && res.length > 0) {
      logger.info("match scope", scope.getScopeResponse());
      return forwardMessageWithScope(msg, scope, headers, transaction);
    }
  }
  return false;
}

export {
  handleMessage,
};
