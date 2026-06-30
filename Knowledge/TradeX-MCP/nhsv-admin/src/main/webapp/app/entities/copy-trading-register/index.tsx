import React from 'react';
import { Route } from 'react-router-dom';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import CopyTradingRegister from './copy-trading-register';
import CopyTradingRegisterDetail from './copy-trading-register-detail';
import CopyTradingRegisterUpdate from './copy-trading-register-update';
import CopyTradingRegisterDeleteDialog from './copy-trading-register-delete-dialog';
const CopyTradingRegisterRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyTradingRegister />} />
    <Route path="new" element={<CopyTradingRegisterUpdate />} />
    <Route path=":id">
      <Route index element={<CopyTradingRegisterDetail />} />
      <Route path="edit" element={<CopyTradingRegisterUpdate />} />
      <Route path="delete" element={<CopyTradingRegisterDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);
export default CopyTradingRegisterRoutes;
