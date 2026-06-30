import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import { Route } from 'react-router-dom';
import React from 'react';
import Portfolio from './portfolio';
import PrivateRoute from 'app/shared/auth/private-route';
import { AUTHORITIES } from 'app/config/constants';
import MarketLeaderManagementComponent from 'app/custom/entities/portfolio/market-leader-management/market-leader-management';
import MarketLeaderInfoRouter
  from "app/custom/entities/portfolio/market-leader-summary-info/market-leader-summary-router";

const PortfolioRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route path="/" element={<Portfolio />}>
      <Route path={'/market-leaders'} element={<MarketLeaderManagementComponent />} />
      <Route
        path="summary-info/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN, AUTHORITIES.MARKET_LEADER]}>
            <MarketLeaderInfoRouter />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PortfolioRoutes;
