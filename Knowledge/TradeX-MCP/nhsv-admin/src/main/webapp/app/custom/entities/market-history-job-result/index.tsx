import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import MarketHistoryJobResult from './market-history-job-result';
import MarketHistoryJobResultDetail from './market-history-job-result-detail';
import MarketHistoryJobResultUpdate from './market-history-job-result-update';
import MarketHistoryJobResultDeleteDialog from './market-history-job-result-delete-dialog';

const MarketHistoryJobResultRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MarketHistoryJobResult />} />
    <Route path="new" element={<MarketHistoryJobResultUpdate />} />
    <Route path=":id">
      <Route index element={<MarketHistoryJobResultDetail />} />
      <Route path="edit" element={<MarketHistoryJobResultUpdate />} />
      <Route path="delete" element={<MarketHistoryJobResultDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default MarketHistoryJobResultRoutes;
