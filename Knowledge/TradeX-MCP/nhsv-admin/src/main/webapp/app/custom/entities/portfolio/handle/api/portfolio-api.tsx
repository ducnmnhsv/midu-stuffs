import { createAsyncThunk } from '@reduxjs/toolkit';
import axios, { AxiosResponse } from 'axios';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IAccount } from 'app/custom/model/account.model';
import { ICopyPortfolioDetail } from 'app/custom/model/copy-portfolio-detail.model';
import { ICopySubscriber } from 'app/shared/model/copy-subscriber.model';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { ICopyPortfolio } from 'app/shared/model/copy-portfolio.model';
import { toast } from "react-toastify";

const GET_ML_ACCOUNT_INFO = 'api/copy-trading/ml-account';
const GET_PORTFOLIO_DETAIL_BY_ML_ID = 'api/copy-trading/portfolio-detail';
const GET_HISTORY_PORTFOLIO = 'api/copy-trading/history-portfolio';
const PUT_PORTFOLIO_INTRODUCTION = 'api/account/introduction';
const GET_SUBSCRIBERS_BY_ML_ID = 'api/copy-trading/ml-subscribers';
const GET_ORDER_HISTORY_BY_ML_ID = 'api/copy-trading/ml-copy-trading-orders';
const POST_UPLOAD_CURRENT_PORTFOLIO = 'api/copy-trading/portfolio/upload';

export const getAccountInfo = createAsyncThunk(
  'portfolio/account-info',
  async (mlUserId: number) => {
    try {
      const url = `${GET_ML_ACCOUNT_INFO}${mlUserId ? `?mlUserId=${mlUserId}` : ''}`;
      return axios.get<IAccount>(url);
    } catch (error) {
      return emptyResponse({});
    }
  },
  { serializeError: serializeAxiosError }
);

export const getPortfolioDetail = createAsyncThunk(
  'portfolio/current-portfolio-detail',
  async (paramString: string) => {
    try {
      const requestUrl = `${GET_PORTFOLIO_DETAIL_BY_ML_ID}${paramString}`;
      return axios.get<ICopyPortfolioDetail[]>(requestUrl);
    } catch (error) {
      return emptyResponse([]);
    }
  },
  { serializeError: serializeAxiosError }
);

export const getCopyPortfolioHistory = createAsyncThunk(
  'portfolio/history',
  async ({ query, page, size, sort }: IQueryParams) => {
    const requestUrl = `${GET_HISTORY_PORTFOLIO}${query ? `?${query}&` : '?'}${
      sort ? `page=${page}&size=${size}&sort=${sort}&` : '?'
    }cacheBuster=${new Date().getTime()}`;
    return axios.get<ICopyPortfolioDetail[]>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const getCopyPortfolioDetailHistory = createAsyncThunk(
  'portfolio/history-detail',
  async ({ query, page, size, sort }: IQueryParams) => {
    const requestUrl = `${GET_HISTORY_PORTFOLIO}/detail${query ? `?${query}&` : '?'}${
      sort ? `page=${page}&size=${size}&sort=${sort}&` : '?'
    }cacheBuster=${new Date().getTime()}`;
    return axios.get<ICopyPortfolioDetail[]>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const putIntroduction = createAsyncThunk(
  'account/introduction',
  async (account: IAccount) => {
    const requestUrl = `${PUT_PORTFOLIO_INTRODUCTION}`;

    return axios.put<IAccount>(requestUrl, cleanEntity(account));
  },
  { serializeError: serializeAxiosError }
);

export const getSubscriberOfMLAccount = createAsyncThunk(
  'portfolio/ml-subscribers',
  async (paramString: string | null) => {
    try {
      const requestUrl = `${GET_SUBSCRIBERS_BY_ML_ID}${paramString}`;
      return axios.get<ICopySubscriber[]>(requestUrl);
    } catch (error) {
      return emptyResponse([]);
    }
  },
  { serializeError: serializeAxiosError }
);

export const getOrderHistoryOfSub = createAsyncThunk(
  'portfolio/ml-copy-order-history',
  async (paramString: string | null) => {
    try {
      const requestUrl = `${GET_ORDER_HISTORY_BY_ML_ID}${paramString}`;
      return axios.get<ICopySubscriber[]>(requestUrl);
    } catch (error) {
      return emptyResponse([]);
    }
  },
  { serializeError: serializeAxiosError }
);

export const postUploadCurrentPortfolio = createAsyncThunk(
  'copyPortfolio/upload_portfolio',
  async (request: any, thunkAPI) => {
    try {
      const result = await axios.post<ICopyPortfolio>(POST_UPLOAD_CURRENT_PORTFOLIO, cleanEntity(request.request));
      request.onReloadCurrentPortfolioList();
      request.onCloseModal();
      return result;
    } catch (error) {
      toast.error(error?.response?.data?.detail, {
        position: "bottom-right",
        autoClose: 10000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
      });
      return emptyResponse([]);
    }
  },
  { serializeError: serializeAxiosError }
);

const emptyResponse = (data: any) =>
  Promise.resolve<AxiosResponse>({
    data: data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: {},
  });
