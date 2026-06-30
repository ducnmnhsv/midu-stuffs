export interface IStockBalanceResponse {
  bankCode?: string;
  bankName?: string;
  bankAccount?: string;
}

export interface IRegisterBankAccountResponse {
  success: boolean;
}

export interface IDeleteBankAccountResponse {
  success: boolean;
}
