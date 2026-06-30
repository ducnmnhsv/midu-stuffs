import dayjs from 'dayjs';
import { ICopyPortfolio } from 'app/shared/model/copy-portfolio.model';

export interface ICopyPortfolioDetails {
  id?: number;
  symbol?: string;
  weight?: number;
  createdAt?: string;
  copyPortfolioId?: ICopyPortfolio | null;
}

export const defaultValue: Readonly<ICopyPortfolioDetails> = {};
