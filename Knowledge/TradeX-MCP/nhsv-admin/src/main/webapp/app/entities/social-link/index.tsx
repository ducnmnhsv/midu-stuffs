import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import SocialLink from './social-link';
import SocialLinkDetail from './social-link-detail';
import SocialLinkUpdate from './social-link-update';
import SocialLinkDeleteDialog from './social-link-delete-dialog';

const SocialLinkRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<SocialLink />} />
    <Route path="new" element={<SocialLinkUpdate />} />
    <Route path=":id">
      <Route index element={<SocialLinkDetail />} />
      <Route path="edit" element={<SocialLinkUpdate />} />
      <Route path="delete" element={<SocialLinkDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SocialLinkRoutes;
