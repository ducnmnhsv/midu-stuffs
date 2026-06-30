import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopyPortfolioHistory from './copy-portfolio-history';
import CopyPortfolioHistoryDetail from './copy-portfolio-history-detail';
import CopyPortfolioHistoryUpdate from './copy-portfolio-history-update';
import CopyPortfolioHistoryDeleteDialog from './copy-portfolio-history-delete-dialog';

const CopyPortfolioHistoryRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyPortfolioHistory />} />
    <Route path="new" element={<CopyPortfolioHistoryUpdate />} />
    <Route path=":id">
      <Route index element={<CopyPortfolioHistoryDetail />} />
      <Route path="edit" element={<CopyPortfolioHistoryUpdate />} />
      <Route path="delete" element={<CopyPortfolioHistoryDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopyPortfolioHistoryRoutes;
