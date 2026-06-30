import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopySubscriber from './copy-subscriber';
import CopySubscriberDetail from './copy-subscriber-detail';
import CopySubscriberUpdate from './copy-subscriber-update';
import CopySubscriberDeleteDialog from './copy-subscriber-delete-dialog';

const CopySubscriberRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopySubscriber />} />
    <Route path="new" element={<CopySubscriberUpdate />} />
    <Route path=":id">
      <Route index element={<CopySubscriberDetail />} />
      <Route path="edit" element={<CopySubscriberUpdate />} />
      <Route path="delete" element={<CopySubscriberDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopySubscriberRoutes;
