import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import MarketLeaderManagementComponent from './market-leader-management';
import CurrentPortfolio from 'app/custom/entities/portfolio/market-leader-summary-info/component/current-portfolio/current-portfolio';
import Portfolio from '../portfolio';
import AccountInfo from 'app/custom/entities/portfolio/market-leader-summary-info/component/account-info/account-info';

const UserManagementRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MarketLeaderManagementComponent />} />

    <Route path="/" element={<Portfolio />}>
      <Route path={'/account-info'} element={<AccountInfo />} />
      <Route path={'/current-portfolio'} element={<CurrentPortfolio />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default UserManagementRoutes;
