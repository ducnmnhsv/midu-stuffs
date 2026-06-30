import React from 'react';
import { Route } from 'react-router-dom';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import UserManagement from './user-management';
import UserManagementDetail from './user-management-detail';
import UserManagementUpdate from './user-management-update';
import UserManagementDeactivateDialog from './user-management-deactivate-dialog';

const UserManagementRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<UserManagement />} />
    <Route path=":login">
      <Route index element={<UserManagementDetail />} />
      <Route path="edit" element={<UserManagementUpdate />} />
      <Route path="deactivate" element={<UserManagementDeactivateDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default UserManagementRoutes;
