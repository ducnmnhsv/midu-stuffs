import { TradexNotification } from 'tradex-common';
import { v4 as uuid } from 'uuid';
import { IOrderStatusDerivativeRequest } from '../model/request/IOrderStatusDerivativeRequest';
import { getSendNotification } from './SendNotificationService';
import { stringToNumberDefault } from '../util/NumberUtils';
import { getOrderOperation, getSellBuyType, getOrderType, getOrderStatus } from '../constrains/Enum';
import OrderStatusDerivative from '../model/ws/OrderStatusDerivative';

export function sendDataOrderStatusDerivative(request: IOrderStatusDerivativeRequest): void {
  const conf: TradexNotification.SocketClusterConfiguration = new TradexNotification.SocketClusterConfiguration();
  conf.channel = `order.status.${request.acnt_no.toUpperCase()}`;

  // Mapping data from lotte to tradex format
  const orderStatusDerivative: OrderStatusDerivative = new OrderStatusDerivative();
  orderStatusDerivative.eventType = 'ORDER_STATUS';
  orderStatusDerivative.seqNo = request.event_seqno;
  orderStatusDerivative.eventTime = request.date + ' ' + request.evt_time;
  orderStatusDerivative.accountNumber = request.acnt_no;
  orderStatusDerivative.orderNumber = request.evt_ordNo;
  orderStatusDerivative.symbolCode = request.evt_code;
  orderStatusDerivative.sellBuyType = getSellBuyType(request.evt_side, request.evt_side);
  orderStatusDerivative.operation = getOrderOperation(request.evt_action, request.evt_action);
  orderStatusDerivative.orderType = getOrderType(request.evt_ordType, request.evt_ordType);
  orderStatusDerivative.orderPrice = stringToNumberDefault(request.evt_price, request.evt_price);
  orderStatusDerivative.orderQuantity = stringToNumberDefault(request.evt_qty, request.evt_qty);
  orderStatusDerivative.orderStatus = getOrderStatus(request.evt_status, request.evt_status);
  orderStatusDerivative.matchedQuantity = stringToNumberDefault(request.evt_matchQty, request.evt_matchQty);
  orderStatusDerivative.remainingQuantity = stringToNumberDefault(request.evt_remQty, request.evt_remQty);
  orderStatusDerivative.matchedPrice = request.evt_matchPrice;
  if (request.evt_ordType.toString() === '9' && request.evt_status.toString() === '5' && stringToNumberDefault(request.evt_matchPrice, request.evt_matchPrice) === 0) {
    orderStatusDerivative.matchedPrice = orderStatusDerivative.orderPrice.toString();
  }
  orderStatusDerivative.originalOrderNumber = request.evt_orgOrdNo;

  getSendNotification().sendSocketCluser(uuid(), conf, orderStatusDerivative);
}
