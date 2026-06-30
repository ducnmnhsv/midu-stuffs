import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IRecentViewChatRoom } from 'app/shared/model/recent-view-chat-room.model';
import { getEntity, updateEntity, createEntity, reset } from './recent-view-chat-room.reducer';

export const RecentViewChatRoomUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const recentViewChatRoomEntity = useAppSelector(state => state.recentViewChatRoom.entity);
  const loading = useAppSelector(state => state.recentViewChatRoom.loading);
  const updating = useAppSelector(state => state.recentViewChatRoom.updating);
  const updateSuccess = useAppSelector(state => state.recentViewChatRoom.updateSuccess);

  const handleClose = () => {
    navigate('/recent-view-chat-room');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);
    values.deletedAt = convertDateTimeToServer(values.deletedAt);

    const entity = {
      ...recentViewChatRoomEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          createdAt: displayDefaultDateTime(),
          updatedAt: displayDefaultDateTime(),
          deletedAt: displayDefaultDateTime(),
        }
      : {
          ...recentViewChatRoomEntity,
          createdAt: convertDateTimeFromServer(recentViewChatRoomEntity.createdAt),
          updatedAt: convertDateTimeFromServer(recentViewChatRoomEntity.updatedAt),
          deletedAt: convertDateTimeFromServer(recentViewChatRoomEntity.deletedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.recentViewChatRoom.home.createOrEditLabel" data-cy="RecentViewChatRoomCreateUpdateHeading">
            Create or edit a Recent View Chat Room
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="recent-view-chat-room-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField label="User Id" id="recent-view-chat-room-userId" name="userId" data-cy="userId" type="text" />
              <ValidatedField
                label="Chat Room Id"
                id="recent-view-chat-room-chatRoomId"
                name="chatRoomId"
                data-cy="chatRoomId"
                type="text"
              />
              <ValidatedField
                label="Created At"
                id="recent-view-chat-room-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Updated At"
                id="recent-view-chat-room-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Deleted At"
                id="recent-view-chat-room-deletedAt"
                name="deletedAt"
                data-cy="deletedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/recent-view-chat-room" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default RecentViewChatRoomUpdate;
