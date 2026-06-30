import { createEntitySlice, EntityState } from 'app/shared/reducers/reducer.utils';
import {
  getAccountInfo,
  getCopyPortfolioDetailHistory,
  getCopyPortfolioHistory,
  getOrderHistoryOfSub,
  getPortfolioDetail,
  getSubscriberOfMLAccount,
  postUploadCurrentPortfolio,
} from '../api/portfolio-api';
import { isPending } from '@reduxjs/toolkit';

const initialState: EntityState<any> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: {
    accountInfo: {},
    copyPortfolioDetails: [],
    copyPortfolioHistory: [],
    copyPortfolioDetailHistory: [],
    mlSubscribers: [],
    mlCopyTradingOrders: [],
    uploadPortfolioStatus: {} as any,
    loadingSubscribers: false,
    loadingSubHistoryOrders: false,
    portfolioDetailTotalItems: 0,
    historyPortfolioTotalItems: 0,
    historyPortfolioDetailTotalItems: 0,
    subscriberTotalItems: 0,
    historyOrderTotalItems: 0,
  },
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

export const PortfolioSlice = createEntitySlice({
  name: 'portfolio',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getAccountInfo.fulfilled, (state, action) => {
        state.loading = false;
        state.entity.accountInfo = action.payload.data;
      })
      .addCase(getPortfolioDetail.fulfilled, (state, action) => {
        state.loading = false;
        state.entity.copyPortfolioDetails = action.payload.data;
        state.entity.portfolioDetailTotalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addCase(getSubscriberOfMLAccount.fulfilled, (state, action) => {
        state.entity.loadingSubscribers = false;
        state.entity.mlSubscribers = action.payload.data;
        state.entity.subscriberTotalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addCase(getCopyPortfolioHistory.fulfilled, (state, action) => {
        state.loading = false;
        state.entity.copyPortfolioHistory = action.payload.data;
        state.entity.historyPortfolioTotalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addCase(getCopyPortfolioDetailHistory.fulfilled, (state, action) => {
        state.loading = false;
        state.entity.copyPortfolioDetailHistory = action.payload.data;
        state.entity.historyPortfolioDetailTotalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addCase(getOrderHistoryOfSub.fulfilled, (state, action) => {
        state.entity.loadingSubHistoryOrders = false;
        state.entity.mlCopyTradingOrders = action.payload.data;
        state.entity.historyOrderTotalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addCase(postUploadCurrentPortfolio.fulfilled, (state, action) => {
        state.loading = false;
        state.entity.uploadPortfolioStatus = action.payload.data;
      })
      .addMatcher(isPending(getCopyPortfolioHistory, getCopyPortfolioDetailHistory), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(getPortfolioDetail), state => {
        state.entity.loadi = false;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(getSubscriberOfMLAccount), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.entity.loadingSubscribers = true;
      })
      .addMatcher(isPending(getOrderHistoryOfSub), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.entity.loadingSubHistoryOrders = true;
      })
      .addMatcher(isPending(getAccountInfo), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.entity.accountInfo = null;
      })
      .addMatcher(isPending(postUploadCurrentPortfolio), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      });
  },
});

export default PortfolioSlice.reducer;
