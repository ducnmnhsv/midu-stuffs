import React from 'react';

import { Route } from 'react-router-dom';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import UserManagement from './user-management';
import ChatRoom from 'app/modules/administration/chat-room';
import InviteUser from 'app/modules/administration/invite-user';
import { AUTHORITIES } from 'app/config/constants';
import PrivateRoute from 'app/shared/auth/private-route';
import PageNotFound from 'app/shared/error/page-not-found';
import MyChatRoom from 'app/modules/administration/my-chat-room';
import PortfolioRoutes from 'app/custom/entities/portfolio/router';
import MarketHistoryJobResultRoutes from "app/custom/entities/market-history-job-result";

const AdministrationRoutes = () => (
  <div>
    <ErrorBoundaryRoutes>
      <Route
        path="user-management/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]}>
            <UserManagement />
          </PrivateRoute>
        }
      />
      <Route
        path="user-management/**"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]}>
            <UserManagement />
          </PrivateRoute>
        }
      />
      <Route
        path="my-chat-room/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.BROKER]}>
            <MyChatRoom />
          </PrivateRoute>
        }
      />
      <Route
        path="my-chat-room/**"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.BROKER]}>
            <MyChatRoom />
          </PrivateRoute>
        }
      />
      <Route
        path="chat-room/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]}>
            <ChatRoom />
          </PrivateRoute>
        }
      />
      <Route
        path="chat-room/**"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]}>
            <ChatRoom />
          </PrivateRoute>
        }
      />
      <Route
        path="invite-user/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]}>
            <InviteUser />
          </PrivateRoute>
        }
      />
      <Route
        path="portfolio-management/**"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN, AUTHORITIES.MARKET_LEADER]}>
            <PortfolioRoutes />
          </PrivateRoute>
        }
      />
      <Route
        path="portfolio-management/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN, AUTHORITIES.MARKET_LEADER]}>
            <PortfolioRoutes />
          </PrivateRoute>
        }
      />
      <Route
        path="latest-job-result/*"
        element={
          <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]}>
            <MarketHistoryJobResultRoutes />
          </PrivateRoute>
        }
      />
      <Route path="*" element={<PageNotFound />} />
    </ErrorBoundaryRoutes>
  </div>
);

export default AdministrationRoutes;
