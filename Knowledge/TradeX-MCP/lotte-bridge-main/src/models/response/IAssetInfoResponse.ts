import { ILotteAssetInfoData } from './lotte/ILotteAssetInfoResponse';

export interface IAssetInfoResponse {
  cmr?: number;
  rights?: number;
  dividend?: number;
  interest?: number;
  netAsset?: number;
  totalCash?: number;
  marginLoan?: number;
  totalAsset?: number;
  reuseAmount?: number;
  marginRights?: number;
  availableCash?: number;
  depositoryFee?: number;
  minBuyingPower?: number;
  virtualDeposit?: number;
  totalLoanAmount?: number;
  totalLoanBuying?: number;
  unavailableCash?: number;
  cashAmountForMMR?: number;
  evaluationAmount?: number;
  evaluationRights?: number;
  tlTaOfMarginList?: number;
  stockAmountForMMR?: number;
  totalLoanMortgage?: number;
  pendingStockAmount?: number;
  tlTaOfTotalAccount?: number;
  usedVirtualDeposit?: number;
  withdrawableAmount?: number;
  lackingMarginAmount?: number;
  unmatchBuyingAmount?: number;
  availableStockAmount?: number;
  stockEvaluationAmount?: number;
  lackingLoanAmountForT1?: number;
  nonSettledBuyingAmount?: number;
  unavailableStockAmount?: number;
  stockAmountCanUseMargin?: number;
  totalLoanExpectedAmount?: number;
  buyingStockWaitingAmount?: number;
  marginPendingStockAmount?: number;
  totalLackingSettledAmount?: number;
  marginAvailableStockAmount?: number;
  lackingVirtualDepositAmount?: number;
  evaluationPendingStockAmount?: number;
  marginUnavailableStockAmount?: number;
  evaluationAvailableStockAmount?: number;
  evaluationUnavailableStockAmount?: number;
  marginStockAmount?: number;
  cwWaitingAmount?: number;
  externalSourceLoan?: number;
  realLoan?: number;
  maxAdvanceWithdrawalLimit?: number;
  marginUnmatchStockAmount?: number;
  marginAwaitingStockValue?: number;
  evaluationUnmatchStockValue?: number;
  evaluationAwaitingStockValue?: number;
  cmrb?: number;
  unusedVirtualDeposit?: number;
  requiredDepositToInitialMargin?: number;
}

export function toAssetInfoResponse(lotteResDataList: ILotteAssetInfoData): IAssetInfoResponse {
  const response: IAssetInfoResponse = {
    cmr: Number(lotteResDataList.mrgn_now_mntn_rt.replace('%', '').trim()),
    rights: Number(lotteResDataList.rgt_dpo),
    dividend: Number(lotteResDataList.rgt_cash),
    interest: Number(lotteResDataList.loan_int),
    netAsset: Number(lotteResDataList.not_total_asset),
    totalCash: Number(lotteResDataList.cash_total),
    marginLoan: Number(lotteResDataList.mgn_loan_amt),
    totalAsset: Number(lotteResDataList.total_asset),
    reuseAmount: Number(lotteResDataList.notyet_pia_loan_amt),
    availableCash: Number(lotteResDataList.cash_not_hold),
    depositoryFee: Number(lotteResDataList.depo_amt),
    virtualDeposit: Number(lotteResDataList.gst_dpo),
    totalLoanAmount: Number(lotteResDataList.loan_total),
    unavailableCash: Number(lotteResDataList.cash_hold),
    cashAmountForMMR: Number(lotteResDataList.mrgn_shrt_amt),
    evaluationAmount: Number(lotteResDataList.wait_qty_for_buy_2),
    marginRights: Number(lotteResDataList.rgt_sbstamt),
    tlTaOfMarginList: Number(lotteResDataList.mrgn_mntn_rt.replace('%', '').trim()),
    totalLoanMortgage: Number(lotteResDataList.col_loan_amt),
    pendingStockAmount: Number(lotteResDataList.delay_rate),
    tlTaOfTotalAccount: Number(lotteResDataList.mrgn_mntn_rt_ta.replace('%', '').trim()),
    usedVirtualDeposit: Number(lotteResDataList.use_vd),
    withdrawableAmount: Number(lotteResDataList.psbamt),
    lackingMarginAmount: Number(lotteResDataList.loan_amt_t1),
    unmatchBuyingAmount: Number(lotteResDataList.not_yet_math_amout),
    availableStockAmount: Number(lotteResDataList.able_qty_value),
    stockEvaluationAmount: Number(lotteResDataList.stk_amt_total),
    lackingLoanAmountForT1: Number(lotteResDataList.loan_sum_amt_t1),
    nonSettledBuyingAmount: Number(lotteResDataList.all_prf_net),
    unavailableStockAmount: Number(lotteResDataList.lim_qty_value),
    stockAmountCanUseMargin: Number(lotteResDataList.stk_margin_rto_total),
    buyingStockWaitingAmount: Number(lotteResDataList.buy_wait_amt),
    totalLackingSettledAmount: Number(lotteResDataList.lack_amt_settl),
    lackingVirtualDepositAmount: Number(lotteResDataList.vd_amt_t1),
    marginPendingStockAmount: Number(lotteResDataList.delay_amt_margin_rto_value),
    marginStockAmount: Number(lotteResDataList.able_amt_margin_rto_value),
    marginUnavailableStockAmount: Number(lotteResDataList.waiting_amt_margin_rto_value),
    marginAvailableStockAmount: Number(lotteResDataList.wait_qty_for_buy_2),
    cwWaitingAmount: Number(lotteResDataList.waiting_dpo_cw),
    externalSourceLoan: Number(lotteResDataList.mgn_loan_amt_src),
    realLoan: Number(lotteResDataList.lnd_real),
    maxAdvanceWithdrawalLimit: Number(lotteResDataList.mn_can_withdraw),
    marginUnmatchStockAmount: Number(lotteResDataList.acnt_nonmth_sbst_amt),
    marginAwaitingStockValue: Number(lotteResDataList.cdr_sbst_acnt),
    evaluationAvailableStockAmount: Number(lotteResDataList.able_amt_sbst_rto_value),
    evaluationPendingStockAmount: Number(lotteResDataList.delay_amt_sbst_rto_value),
    evaluationUnavailableStockAmount: Number(lotteResDataList.waiting_amt_sbst_rto_value),
    evaluationRights: Number(lotteResDataList.rgt_sbst_value),
    evaluationUnmatchStockValue: Number(lotteResDataList.nonmth_sbst_value),
    evaluationAwaitingStockValue: Number(lotteResDataList.cdr_bp_acnt),
    cmrb: Number(lotteResDataList.mrgn_now_mntn_bp_rt.replace('%', '').trim()),
    unusedVirtualDeposit: Number(lotteResDataList.unused_grt_mn),
    requiredDepositToInitialMargin: Number(lotteResDataList.payable_amount),
  };
  return response;
}
