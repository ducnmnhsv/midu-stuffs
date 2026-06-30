import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import InviteUser from './invite-user';
import InviteUserDetail from './invite-user-detail';
import InviteUserUpdate from './invite-user-update';
import InviteUserDeleteDialog from './invite-user-delete-dialog';

const InviteUserRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<InviteUser />} />
    <Route path="new" element={<InviteUserUpdate />} />
    <Route path=":id">
      <Route index element={<InviteUserDetail />} />
      <Route path="edit" element={<InviteUserUpdate />} />
      <Route path="delete" element={<InviteUserDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default InviteUserRoutes;
