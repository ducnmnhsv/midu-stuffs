import dayjs from 'dayjs';
export interface ICopyTradingRegister {
  id?: number;
  accountNumber?: string | null;
  subAccount?: string | null;
  customerName?: string | null;
  status?: boolean | null;
  createAt?: string | null;
  updatedAt?: string | null;
}
export const defaultValue: Readonly<ICopyTradingRegister> = {
  status: false,
};
