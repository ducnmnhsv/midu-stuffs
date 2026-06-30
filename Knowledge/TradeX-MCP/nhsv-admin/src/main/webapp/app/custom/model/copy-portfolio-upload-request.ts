import { ICopyPortfolioDetail } from 'app/custom/model/copy-portfolio-detail.model';

export interface ICopyPortfolioUploadRequest {
  mlUserId?: number;
  items?: ICopyPortfolioDetail[];
}

export const defaultValue: Readonly<ICopyPortfolioUploadRequest> = {};
