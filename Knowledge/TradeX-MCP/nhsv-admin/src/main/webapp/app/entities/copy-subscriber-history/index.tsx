import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CopySubscriberHistory from './copy-subscriber-history';
import CopySubscriberHistoryDetail from './copy-subscriber-history-detail';
import CopySubscriberHistoryUpdate from './copy-subscriber-history-update';
import CopySubscriberHistoryDeleteDialog from './copy-subscriber-history-delete-dialog';

const CopySubscriberHistoryRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CopySubscriberHistory />} />
    <Route path="new" element={<CopySubscriberHistoryUpdate />} />
    <Route path=":id">
      <Route index element={<CopySubscriberHistoryDetail />} />
      <Route path="edit" element={<CopySubscriberHistoryUpdate />} />
      <Route path="delete" element={<CopySubscriberHistoryDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CopySubscriberHistoryRoutes;
