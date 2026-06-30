import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopyTradingOrder from './copy-trading-order';
import CopyTradingOrderDetail from './copy-trading-order-detail';
import CopyTradingOrderUpdate from './copy-trading-order-update';
import CopyTradingOrderDeleteDialog from './copy-trading-order-delete-dialog';

const CopyTradingOrderRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyTradingOrder />} />
    <Route path="new" element={<CopyTradingOrderUpdate />} />
    <Route path=":id">
      <Route index element={<CopyTradingOrderDetail />} />
      <Route path="edit" element={<CopyTradingOrderUpdate />} />
      <Route path="delete" element={<CopyTradingOrderDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopyTradingOrderRoutes;
