import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ChatRoom from './chat-room';
import CreatedChatRoom from './created-chat-room';
import SocialLink from './social-link';
import InviteUser from './invite-user';
import Broker from './broker';
import RecentViewChatRoom from './recent-view-chat-room';
import CopySubscriber from './copy-subscriber';
import CopySubscriberHistory from './copy-subscriber-history';
import CopyPortfolio from './copy-portfolio';
import CopyPortfolioHistory from './copy-portfolio-history';
import CopyPortfolioDetails from './copy-portfolio-details';
import CopyPortfolioDetailHistory from './copy-portfolio-detail-history';
import CopyMarketLeaderDetails from './copy-market-leader-details';
import CopyTradingOrder from './copy-trading-order';
import MarketHistoryJobResultUpdate from './market-history-job-result';
import MarketHistoryJobResult from './market-history-job-result';

import CustomMarketHistoryJobResultUpdate from './../custom/entities/market-history-job-result';
import CustomMarketHistoryJobResult from './..//custom/entities/market-history-job-result';

import CopyTradingRegister from './copy-trading-register';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="chat-room/*" element={<ChatRoom />} />
        <Route path="created-chat-room/*" element={<CreatedChatRoom />} />
        <Route path="social-link/*" element={<SocialLink />} />
        <Route path="invite-user/*" element={<InviteUser />} />
        <Route path="broker/*" element={<Broker />} />
        <Route path="recent-view-chat-room/*" element={<RecentViewChatRoom />} />
        <Route path="copy-subscriber/*" element={<CopySubscriber />} />
        <Route path="copy-subscriber-history/*" element={<CopySubscriberHistory />} />
        <Route path="copy-portfolio/*" element={<CopyPortfolio />} />
        <Route path="copy-portfolio-history/*" element={<CopyPortfolioHistory />} />
        <Route path="copy-portfolio-details/*" element={<CopyPortfolioDetails />} />
        <Route path="copy-portfolio-detail-history/*" element={<CopyPortfolioDetailHistory />} />
        <Route path="copy-market-leader-details/*" element={<CopyMarketLeaderDetails />} />
        <Route path="copy-trading-order/*" element={<CopyTradingOrder />} />
        <Route path="market-history-job-result/*" element={<MarketHistoryJobResultUpdate />} />
        <Route path="market-history-job-result/*" element={<MarketHistoryJobResult />} />
        <Route path="copy-trading-register/*" element={<CopyTradingRegister />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
        <Route path="latest-job-result/*" element={<CustomMarketHistoryJobResultUpdate />} />
        <Route path="latest-job-result/*" element={<CustomMarketHistoryJobResult />} />
      </ErrorBoundaryRoutes>
    </div>
  );
};
