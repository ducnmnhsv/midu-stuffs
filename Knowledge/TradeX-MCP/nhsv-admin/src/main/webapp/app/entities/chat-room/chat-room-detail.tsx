import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './chat-room.reducer';

export const ChatRoomDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const chatRoomEntity = useAppSelector(state => state.chatRoom.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="chatRoomDetailsHeading">Chat Room</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{chatRoomEntity.id}</dd>
          <dt>
            <span id="groupName">Group Name</span>
          </dt>
          <dd>{chatRoomEntity.groupName}</dd>
          <dt>
            <span id="groupOwner">Group Owner</span>
          </dt>
          <dd>{chatRoomEntity.groupOwner}</dd>
          <dt>
            <span id="introduction">Introduction</span>
          </dt>
          <dd>{chatRoomEntity.introduction}</dd>
          <dt>
            <span id="photo">Photo</span>
          </dt>
          <dd>{chatRoomEntity.photo}</dd>
          <dt>
            <span id="brokerName">Broker Name</span>
          </dt>
          <dd>{chatRoomEntity.brokerName}</dd>
          <dt>
            <span id="brokerContact">Broker Contact</span>
          </dt>
          <dd>{chatRoomEntity.brokerContact}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{chatRoomEntity.status}</dd>
          <dt>
            <span id="createdBy">Created By</span>
          </dt>
          <dd>{chatRoomEntity.createdBy}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>{chatRoomEntity.createdAt ? <TextFormat value={chatRoomEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>{chatRoomEntity.updatedAt ? <TextFormat value={chatRoomEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="approvedAt">Approved At</span>
          </dt>
          <dd>
            {chatRoomEntity.approvedAt ? <TextFormat value={chatRoomEntity.approvedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="rejectedAt">Rejected At</span>
          </dt>
          <dd>
            {chatRoomEntity.rejectedAt ? <TextFormat value={chatRoomEntity.rejectedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="rejectReason">Reject Reason</span>
          </dt>
          <dd>{chatRoomEntity.rejectReason}</dd>
          <dt>
            <span id="approvedBy">Approved By</span>
          </dt>
          <dd>{chatRoomEntity.approvedBy}</dd>
          <dt>
            <span id="rejectedBy">Rejected By</span>
          </dt>
          <dd>{chatRoomEntity.rejectedBy}</dd>
          <dt>
            <span id="action">Action</span>
          </dt>
          <dd>{chatRoomEntity.action}</dd>
        </dl>
        <Button tag={Link} to="/chat-room" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/chat-room/${chatRoomEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default ChatRoomDetail;
