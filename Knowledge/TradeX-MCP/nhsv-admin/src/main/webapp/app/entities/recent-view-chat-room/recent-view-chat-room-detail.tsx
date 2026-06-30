import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './recent-view-chat-room.reducer';

export const RecentViewChatRoomDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const recentViewChatRoomEntity = useAppSelector(state => state.recentViewChatRoom.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="recentViewChatRoomDetailsHeading">Recent View Chat Room</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{recentViewChatRoomEntity.id}</dd>
          <dt>
            <span id="userId">User Id</span>
          </dt>
          <dd>{recentViewChatRoomEntity.userId}</dd>
          <dt>
            <span id="chatRoomId">Chat Room Id</span>
          </dt>
          <dd>{recentViewChatRoomEntity.chatRoomId}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {recentViewChatRoomEntity.createdAt ? (
              <TextFormat value={recentViewChatRoomEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {recentViewChatRoomEntity.updatedAt ? (
              <TextFormat value={recentViewChatRoomEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="deletedAt">Deleted At</span>
          </dt>
          <dd>
            {recentViewChatRoomEntity.deletedAt ? (
              <TextFormat value={recentViewChatRoomEntity.deletedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
        </dl>
        <Button tag={Link} to="/recent-view-chat-room" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/recent-view-chat-room/${recentViewChatRoomEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default RecentViewChatRoomDetail;
