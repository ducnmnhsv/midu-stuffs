import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CreatedChatRoom from './created-chat-room';
import CreatedChatRoomDetail from './created-chat-room-detail';
import CreatedChatRoomUpdate from './created-chat-room-update';
import CreatedChatRoomDeleteDialog from './created-chat-room-delete-dialog';

const CreatedChatRoomRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CreatedChatRoom />} />
    <Route path="new" element={<CreatedChatRoomUpdate />} />
    <Route path=":id">
      <Route index element={<CreatedChatRoomDetail />} />
      <Route path="edit" element={<CreatedChatRoomUpdate />} />
      <Route path="delete" element={<CreatedChatRoomDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CreatedChatRoomRoutes;
