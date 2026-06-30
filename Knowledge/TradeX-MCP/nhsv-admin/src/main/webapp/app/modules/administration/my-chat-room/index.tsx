import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import MyChatRoom from './my-chat-room';
import MyChatRoomLink from './my-chat-room-detail';
import MyChatRoomUpdate from './my-chat-room-update';
import MyChatRoomDeleteDialog from './my-chat-room-delete-dialog';
import PageNotFound from 'app/shared/error/page-not-found';
import MyChatRoomCreate from './my-chat-room-create';

const MyChatRoomRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<MyChatRoom />} />
    <Route path="new" element={<MyChatRoomCreate />} />
    <Route path=":id">
      <Route index element={<MyChatRoomLink />} />
      <Route path="edit" element={<MyChatRoomUpdate />} />
      <Route path="delete" element={<MyChatRoomDeleteDialog />} />
    </Route>
    <Route path="*" element={<PageNotFound />} />

  </ErrorBoundaryRoutes>
);

export default MyChatRoomRoutes;
