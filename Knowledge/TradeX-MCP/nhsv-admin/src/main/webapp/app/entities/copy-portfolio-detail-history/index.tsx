import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopyPortfolioDetailHistory from './copy-portfolio-detail-history';
import CopyPortfolioDetailHistoryDetail from './copy-portfolio-detail-history-detail';
import CopyPortfolioDetailHistoryUpdate from './copy-portfolio-detail-history-update';
import CopyPortfolioDetailHistoryDeleteDialog from './copy-portfolio-detail-history-delete-dialog';

const CopyPortfolioDetailHistoryRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyPortfolioDetailHistory />} />
    <Route path="new" element={<CopyPortfolioDetailHistoryUpdate />} />
    <Route path=":id">
      <Route index element={<CopyPortfolioDetailHistoryDetail />} />
      <Route path="edit" element={<CopyPortfolioDetailHistoryUpdate />} />
      <Route path="delete" element={<CopyPortfolioDetailHistoryDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopyPortfolioDetailHistoryRoutes;
