import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IRecentViewChatRoom } from 'app/shared/model/recent-view-chat-room.model';
import { getEntities } from './recent-view-chat-room.reducer';

export const RecentViewChatRoom = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const recentViewChatRoomList = useAppSelector(state => state.recentViewChatRoom.entities);
  const loading = useAppSelector(state => state.recentViewChatRoom.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="recent-view-chat-room-heading" data-cy="RecentViewChatRoomHeading">
        Recent View Chat Rooms
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link
            to="/recent-view-chat-room/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Recent View Chat Room
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {recentViewChatRoomList && recentViewChatRoomList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>User Id</th>
                <th>Chat Room Id</th>
                <th>Created At</th>
                <th>Updated At</th>
                <th>Deleted At</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {recentViewChatRoomList.map((recentViewChatRoom, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/recent-view-chat-room/${recentViewChatRoom.id}`} color="link" size="sm">
                      {recentViewChatRoom.id}
                    </Button>
                  </td>
                  <td>{recentViewChatRoom.userId}</td>
                  <td>{recentViewChatRoom.chatRoomId}</td>
                  <td>
                    {recentViewChatRoom.createdAt ? (
                      <TextFormat type="date" value={recentViewChatRoom.createdAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {recentViewChatRoom.updatedAt ? (
                      <TextFormat type="date" value={recentViewChatRoom.updatedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {recentViewChatRoom.deletedAt ? (
                      <TextFormat type="date" value={recentViewChatRoom.deletedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/recent-view-chat-room/${recentViewChatRoom.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/recent-view-chat-room/${recentViewChatRoom.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/recent-view-chat-room/${recentViewChatRoom.id}/delete`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Recent View Chat Rooms found</div>
        )}
      </div>
    </div>
  );
};

export default RecentViewChatRoom;
