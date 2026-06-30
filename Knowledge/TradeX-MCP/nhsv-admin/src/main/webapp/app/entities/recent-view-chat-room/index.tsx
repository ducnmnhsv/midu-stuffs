import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import RecentViewChatRoom from './recent-view-chat-room';
import RecentViewChatRoomDetail from './recent-view-chat-room-detail';
import RecentViewChatRoomUpdate from './recent-view-chat-room-update';
import RecentViewChatRoomDeleteDialog from './recent-view-chat-room-delete-dialog';

const RecentViewChatRoomRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<RecentViewChatRoom />} />
    <Route path="new" element={<RecentViewChatRoomUpdate />} />
    <Route path=":id">
      <Route index element={<RecentViewChatRoomDetail />} />
      <Route path="edit" element={<RecentViewChatRoomUpdate />} />
      <Route path="delete" element={<RecentViewChatRoomDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default RecentViewChatRoomRoutes;
