export interface IRightHistoryResponse {
  stockCode?: string;
  sequenceNumber?: number;
  baseDate?: string;
  status?: string;
  baseRate?: number;
  dividendRate?: number;
  ownQty?: number;
  beginDate?: string;
  endDate?: string;
  issuePrice?: number;
  availableQty?: number;
  requestQty?: number;
  requestAmount?: number;
  effectiveDate?: string;
  isEffective?: string;
}
