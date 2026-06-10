import {Kafka, Logger as logger, Models, Utils} from "tradex-common";
import Scope from "../models/db/Scope";
import TRANSFORM from "../utils/uriTransforms";
import {createTransaction} from "../dao/TransactionDao";
import ServiceDownError from "../errors/ServiceDownError";
import {ITransaction} from "../models/db/Transaction";

export default function forwardMessageWithScope(
  msg: Kafka.IMessage,
  scope: Scope,
  headers: Models.IHeaders,
  transaction: ITransaction,
): Promise<any> | boolean {
  if (transaction) {
    transaction.scope_id = scope.id.get();
  }
  if (scope.hasForwardData()) {
    const scopeResponse: Models.AAA.IScope = scope.getScopeResponse();
    const forwardResult: Models.IForwardUriResult = Utils.getForwardUri(msg.uri, scopeResponse, headers.token, TRANSFORM);
    logger.info("forward result", forwardResult);
    if (!forwardResult.topic) {
      return false;
    }
    if (forwardResult.topic === "ERROR") {
      if (transaction) {
        transaction.to_topic = forwardResult.topic;
        transaction.to_uri = forwardResult.uri;
        createTransaction(transaction).then().catch();
      }
      return Promise.reject(new ServiceDownError());
    } else {
      if (transaction) {
        transaction.to_topic = forwardResult.topic;
        transaction.to_uri = forwardResult.uri;
        createTransaction(transaction).then().catch();
      }
      if (forwardResult.conId) {
        msg.data.conId = forwardResult.conId;
      }
      Kafka.getInstance().sendForwardMessage(msg, forwardResult.topic, forwardResult.uri);
      return true;
    }
  } else {
    logger.warn("scope does not have forward setting", scope.id.get());
    return false;
  }
}
