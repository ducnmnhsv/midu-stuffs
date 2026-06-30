export interface IChangeBrokerHistoryItemResponse {
  sequence: number;
  previousBrokerId: string;
  previousBrokerName?: string;
  newBrokerId: string;
  newBrokerName?: string;
  reason: string;
  status: string;
  requestedDate: string;
  updatedDate?: string;
  nextKey?: string;
}

