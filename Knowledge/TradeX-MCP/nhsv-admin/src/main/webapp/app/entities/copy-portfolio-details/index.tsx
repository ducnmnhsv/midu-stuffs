import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopyPortfolioDetails from './copy-portfolio-details';
import CopyPortfolioDetailsDetail from './copy-portfolio-details-detail';
import CopyPortfolioDetailsUpdate from './copy-portfolio-details-update';
import CopyPortfolioDetailsDeleteDialog from './copy-portfolio-details-delete-dialog';

const CopyPortfolioDetailsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopyPortfolioDetails />} />
    <Route path="new" element={<CopyPortfolioDetailsUpdate />} />
    <Route path=":id">
      <Route index element={<CopyPortfolioDetailsDetail />} />
      <Route path="edit" element={<CopyPortfolioDetailsUpdate />} />
      <Route path="delete" element={<CopyPortfolioDetailsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopyPortfolioDetailsRoutes;
