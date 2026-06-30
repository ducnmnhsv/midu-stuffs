import dayjs from 'dayjs';
import { ICopyPortfolioHistory } from 'app/shared/model/copy-portfolio-history.model';

export interface ICopyPortfolioDetailHistory {
  id?: number;
  symbol?: string;
  weight?: number;
  createdAt?: string;
  copyPortfolioHistoryId?: ICopyPortfolioHistory | null;
}

export const defaultValue: Readonly<ICopyPortfolioDetailHistory> = {};
