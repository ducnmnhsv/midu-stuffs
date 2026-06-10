export interface ILotteChangePasswordRequest {
  hts_user_id: string;
  old_pass: string;
  new_pass: string;
  hts_id: string;
  sec_pass: string;
}
