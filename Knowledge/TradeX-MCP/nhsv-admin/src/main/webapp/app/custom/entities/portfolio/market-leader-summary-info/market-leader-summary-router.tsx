import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import { Route } from 'react-router-dom';
import React from 'react';
import CurrentPortfolio from 'app/custom/entities/portfolio/market-leader-summary-info/component/current-portfolio/current-portfolio';
import AccountInfo from 'app/custom/entities/portfolio/market-leader-summary-info/component/account-info/account-info';
import HistoryPortfolioComponent from 'app/custom/entities/portfolio/market-leader-summary-info/component/history/history-portfolio';
import HistoryPortfolioDetailComponent from 'app/custom/entities/portfolio/market-leader-summary-info/component/history/history-portfolio-detail';
import SubscriberInfo from 'app/custom/entities/portfolio/market-leader-summary-info/component/subscriber-info/subscriber-info';
import MarketLeaderSummaryInfo from 'app/custom/entities/portfolio/market-leader-summary-info/market-leader-summary-info';

const MarketLeaderInfoRouter = () => (
  <ErrorBoundaryRoutes>
    <Route path="/" element={<MarketLeaderSummaryInfo />}>
      <Route path={'/:id/account-info'} element={<AccountInfo />} />
      <Route path={'/:id/current-portfolio'} element={<CurrentPortfolio />} />
      <Route path={'/:id/subscribers'} element={<SubscriberInfo />} />
      <Route path={'/:id/current-portfolio'} element={<CurrentPortfolio />} />
      <Route path={'/:id/history-portfolio'} element={<HistoryPortfolioComponent />} />
      <Route path={'/:id/history-portfolio/detail'} element={<HistoryPortfolioDetailComponent />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MarketLeaderInfoRouter;
