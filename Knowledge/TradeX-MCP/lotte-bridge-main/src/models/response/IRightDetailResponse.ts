export interface IRightDetailResponse {
  amount?: number;
  endDate?: string;
  quantity?: string;
  rightType?: number;
  startDate?: string;
  issuePrice?: number;
  tradeNumber?: string;
  availableAmount?: number;
  standardQuantity?: number;
  availableQuantity?: number;
  requestedQuantity?: number;
  processStatusCode?: number;
  processStatusName?: string;
  approveWaitingQuantity?: number;
  bankCancelWaitingQuantity?: number;
  bankApproveWaitingQuantity?: number;
}
