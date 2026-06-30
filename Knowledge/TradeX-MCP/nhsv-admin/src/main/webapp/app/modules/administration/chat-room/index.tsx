import React from 'react';
import { Route } from 'react-router-dom';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import ChatRoom from './chat-room';
import ChatRoomDetail from './chat-room-detail';
import ChatRoomApproveDialog from './chat-room-approve-dialog';
import ChatRoomRejectDialog from './chat-room-reject-dialog';
import PageNotFound from 'app/shared/error/page-not-found';

const ChatRoomRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ChatRoom />} />
    <Route path=":id">
      <Route index element={<ChatRoomDetail />} />
      <Route path="approve" element={<ChatRoomApproveDialog />} />
      <Route path="reject" element={<ChatRoomRejectDialog />} />
    </Route>
    <Route path="*" element={<PageNotFound />} />

  </ErrorBoundaryRoutes>
);

export default ChatRoomRoutes;
