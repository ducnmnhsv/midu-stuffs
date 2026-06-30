import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopyPortfolio from './copy-portfolio';
import CopyPortfolioDetail from './copy-portfolio-detail';
import CopyPortfolioUpdate from './copy-portfolio-update';
import CopyPortfolioDeleteDialog from './copy-portfolio-delete-dialog';

const CopyPortfolioRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyPortfolio />} />
    <Route path="new" element={<CopyPortfolioUpdate />} />
    <Route path=":id">
      <Route index element={<CopyPortfolioDetail />} />
      <Route path="edit" element={<CopyPortfolioUpdate />} />
      <Route path="delete" element={<CopyPortfolioDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopyPortfolioRoutes;
