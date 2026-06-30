import React from 'react';
import { Route } from 'react-router-dom';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import InviteUser from './invite-user';
import InviteUserDetail from './invite-user-detail';
import UserManagementInvite from './invite-user-create';
import PageNotFound from 'app/shared/error/page-not-found';

const InviteUserRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<InviteUser />} />
    <Route path="new" element={<UserManagementInvite />} />
    <Route path=":id">
      <Route index element={<InviteUserDetail />} />
    </Route>
    <Route path="*" element={<PageNotFound />} />
  </ErrorBoundaryRoutes>
);

export default InviteUserRoutes;
