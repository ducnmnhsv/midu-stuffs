import { TradexNotification } from 'tradex-common';

export default class OrderStatusDerivative implements TradexNotification.ITemplateData {
    eventType?: string;
    seqNo?: string;
    eventTime?: string;
    accountNumber?: string;
    orderNumber?: string;
    symbolCode?: string;
    sellBuyType?: string;
    operation?: string;
    orderType?: string;
    orderPrice?: string|number;
    orderQuantity?: string|number;
    orderStatus?: string;
    matchedQuantity?: string|number;
    remainingQuantity?: string|number;
    matchedPrice?: string|number;
    originalOrderNumber?: string;

    getTemplate(): string {
      return 'dt';
    }
}
