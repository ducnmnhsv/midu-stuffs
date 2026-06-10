import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteContractStatusResponse extends ILotteCommonResponse {
  contractStatus: string;
  error_code: string;
  error_desc: string;
  success: boolean;
  total_record: string;
  data_list: Array<{ has_cntr_yn: string }>;
}
