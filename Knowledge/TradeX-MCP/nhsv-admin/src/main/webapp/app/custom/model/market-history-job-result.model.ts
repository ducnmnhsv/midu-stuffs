import { IMarketHistoryJobResult } from "app/shared/model/market-history-job-result.model";

export interface IMarketHistoryJobResultStockEvent extends IMarketHistoryJobResult{
    eventName?: string | null;
    eventType?: string | null;
}

export const defaultValue: Readonly<IMarketHistoryJobResultStockEvent> = {
    isSuccess: false,
};

export enum StockEventType {
    CASHDIV = 'Cash dividend',
    BONUS_SHARES = 'Bonus shares',
    STOCKDIV = 'Stock dividend',
    ISSUE = 'Additional shares'
}