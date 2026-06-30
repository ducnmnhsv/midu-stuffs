import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IChatRoom } from 'app/shared/model/chat-room.model';
import { StatusEnum } from 'app/shared/model/enumerations/status-enum.model';
import { ActionEnum } from 'app/shared/model/enumerations/action-enum.model';
import { getEntity, updateEntity, createEntity, reset } from './chat-room.reducer';

export const ChatRoomUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const chatRoomEntity = useAppSelector(state => state.chatRoom.entity);
  const loading = useAppSelector(state => state.chatRoom.loading);
  const updating = useAppSelector(state => state.chatRoom.updating);
  const updateSuccess = useAppSelector(state => state.chatRoom.updateSuccess);
  const statusEnumValues = Object.keys(StatusEnum);
  const actionEnumValues = Object.keys(ActionEnum);

  const handleClose = () => {
    navigate('/chat-room' + location.search);
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
    values.approvedAt = convertDateTimeToServer(values.approvedAt);
    values.rejectedAt = convertDateTimeToServer(values.rejectedAt);

    const entity = {
      ...chatRoomEntity,
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
          approvedAt: displayDefaultDateTime(),
          rejectedAt: displayDefaultDateTime(),
        }
      : {
          status: 'PENDING',
          action: 'CREATE',
          ...chatRoomEntity,
          createdAt: convertDateTimeFromServer(chatRoomEntity.createdAt),
          updatedAt: convertDateTimeFromServer(chatRoomEntity.updatedAt),
          approvedAt: convertDateTimeFromServer(chatRoomEntity.approvedAt),
          rejectedAt: convertDateTimeFromServer(chatRoomEntity.rejectedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.chatRoom.home.createOrEditLabel" data-cy="ChatRoomCreateUpdateHeading">
            Create or edit a Chat Room
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="chat-room-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="Group Name" id="chat-room-groupName" name="groupName" data-cy="groupName" type="text" />
              <ValidatedField label="Group Owner" id="chat-room-groupOwner" name="groupOwner" data-cy="groupOwner" type="text" />
              <ValidatedField label="Introduction" id="chat-room-introduction" name="introduction" data-cy="introduction" type="text" />
              <ValidatedField label="Photo" id="chat-room-photo" name="photo" data-cy="photo" type="text" />
              <ValidatedField label="Broker Name" id="chat-room-brokerName" name="brokerName" data-cy="brokerName" type="text" />
              <ValidatedField
                label="Broker Contact"
                id="chat-room-brokerContact"
                name="brokerContact"
                data-cy="brokerContact"
                type="text"
              />
              <ValidatedField label="Status" id="chat-room-status" name="status" data-cy="status" type="select">
                {statusEnumValues.map(statusEnum => (
                  <option value={statusEnum} key={statusEnum}>
                    {statusEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Created By" id="chat-room-createdBy" name="createdBy" data-cy="createdBy" type="text" />
              <ValidatedField
                label="Created At"
                id="chat-room-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Updated At"
                id="chat-room-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Approved At"
                id="chat-room-approvedAt"
                name="approvedAt"
                data-cy="approvedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Rejected At"
                id="chat-room-rejectedAt"
                name="rejectedAt"
                data-cy="rejectedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Reject Reason" id="chat-room-rejectReason" name="rejectReason" data-cy="rejectReason" type="text" />
              <ValidatedField label="Approved By" id="chat-room-approvedBy" name="approvedBy" data-cy="approvedBy" type="text" />
              <ValidatedField label="Rejected By" id="chat-room-rejectedBy" name="rejectedBy" data-cy="rejectedBy" type="text" />
              <ValidatedField label="Action" id="chat-room-action" name="action" data-cy="action" type="select">
                {actionEnumValues.map(actionEnum => (
                  <option value={actionEnum} key={actionEnum}>
                    {actionEnum}
                  </option>
                ))}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/chat-room" replace color="info">
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

export default ChatRoomUpdate;
