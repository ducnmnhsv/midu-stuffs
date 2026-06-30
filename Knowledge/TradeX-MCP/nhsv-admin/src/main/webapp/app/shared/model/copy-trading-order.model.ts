import dayjs from 'dayjs';
import { SellBuyTypeEnum } from 'app/shared/model/enumerations/sell-buy-type-enum.model';
import { ExchangeTypeEnum } from 'app/shared/model/enumerations/exchange-type-enum.model';
import { OrderTypeEnum } from 'app/shared/model/enumerations/order-type-enum.model';

export interface ICopyTradingOrder {
  id?: number;
  jobId?: string;
  symbol?: string;
  fee?: number | null;
  tax?: number | null;
  orderNumber?: string | null;
  sellBuyType?: SellBuyTypeEnum | null;
  exchangeType?: ExchangeTypeEnum | null;
  orderType?: OrderTypeEnum | null;
  orderQuantity?: number | null;
  orderPrice?: number | null;
  apiParam?: string | null;
  apiStatusCode?: string | null;
  apiErrorMessage?: string | null;
  createdAt?: string;
  updatedAt?: string | null;
  copySubscriberId?: number;
  copyPortfolioId?: number;
}

export const defaultValue: Readonly<ICopyTradingOrder> = {};
