import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './created-chat-room.reducer';

export const CreatedChatRoomDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const createdChatRoomEntity = useAppSelector(state => state.createdChatRoom.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="createdChatRoomDetailsHeading">Created Chat Room</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{createdChatRoomEntity.id}</dd>
          <dt>
            <span id="groupName">Group Name</span>
          </dt>
          <dd>{createdChatRoomEntity.groupName}</dd>
          <dt>
            <span id="groupOwner">Group Owner</span>
          </dt>
          <dd>{createdChatRoomEntity.groupOwner}</dd>
          <dt>
            <span id="introduction">Introduction</span>
          </dt>
          <dd>{createdChatRoomEntity.introduction}</dd>
          <dt>
            <span id="photo">Photo</span>
          </dt>
          <dd>{createdChatRoomEntity.photo}</dd>
          <dt>
            <span id="brokerName">Broker Name</span>
          </dt>
          <dd>{createdChatRoomEntity.brokerName}</dd>
          <dt>
            <span id="brokerContact">Broker Contact</span>
          </dt>
          <dd>{createdChatRoomEntity.brokerContact}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{createdChatRoomEntity.status}</dd>
          <dt>
            <span id="createdBy">Created By</span>
          </dt>
          <dd>{createdChatRoomEntity.createdBy}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {createdChatRoomEntity.createdAt ? (
              <TextFormat value={createdChatRoomEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {createdChatRoomEntity.updatedAt ? (
              <TextFormat value={createdChatRoomEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="approvedAt">Approved At</span>
          </dt>
          <dd>
            {createdChatRoomEntity.approvedAt ? (
              <TextFormat value={createdChatRoomEntity.approvedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="approvedBy">Approved By</span>
          </dt>
          <dd>{createdChatRoomEntity.approvedBy}</dd>
          <dt>
            <span id="rejectReason">Reject Reason</span>
          </dt>
          <dd>{createdChatRoomEntity.rejectReason}</dd>
          <dt>
            <span id="totalView">Total View</span>
          </dt>
          <dd>{createdChatRoomEntity.totalView}</dd>
        </dl>
        <Button tag={Link} to="/created-chat-room" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/created-chat-room/${createdChatRoomEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CreatedChatRoomDetail;
