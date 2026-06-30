import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopyMarketLeaderDetails from './copy-market-leader-details';
import CopyMarketLeaderDetailsDetail from './copy-market-leader-details-detail';
import CopyMarketLeaderDetailsUpdate from './copy-market-leader-details-update';
import CopyMarketLeaderDetailsDeleteDialog from './copy-market-leader-details-delete-dialog';

const CopyMarketLeaderDetailsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyMarketLeaderDetails />} />
    <Route path="new" element={<CopyMarketLeaderDetailsUpdate />} />
    <Route path=":id">
      <Route index element={<CopyMarketLeaderDetailsDetail />} />
      <Route path="edit" element={<CopyMarketLeaderDetailsUpdate />} />
      <Route path="delete" element={<CopyMarketLeaderDetailsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopyMarketLeaderDetailsRoutes;
