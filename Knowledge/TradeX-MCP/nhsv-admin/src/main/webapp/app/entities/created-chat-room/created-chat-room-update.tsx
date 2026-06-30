import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ICreatedChatRoom } from 'app/shared/model/created-chat-room.model';
import { StatusEnum } from 'app/shared/model/enumerations/status-enum.model';
import { getEntity, updateEntity, createEntity, reset } from './created-chat-room.reducer';

export const CreatedChatRoomUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const createdChatRoomEntity = useAppSelector(state => state.createdChatRoom.entity);
  const loading = useAppSelector(state => state.createdChatRoom.loading);
  const updating = useAppSelector(state => state.createdChatRoom.updating);
  const updateSuccess = useAppSelector(state => state.createdChatRoom.updateSuccess);
  const statusEnumValues = Object.keys(StatusEnum);

  const handleClose = () => {
    navigate('/created-chat-room' + location.search);
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

    const entity = {
      ...createdChatRoomEntity,
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
        }
      : {
          status: 'PENDING',
          ...createdChatRoomEntity,
          createdAt: convertDateTimeFromServer(createdChatRoomEntity.createdAt),
          updatedAt: convertDateTimeFromServer(createdChatRoomEntity.updatedAt),
          approvedAt: convertDateTimeFromServer(createdChatRoomEntity.approvedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.createdChatRoom.home.createOrEditLabel" data-cy="CreatedChatRoomCreateUpdateHeading">
            Create or edit a Created Chat Room
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
                <ValidatedField name="id" required readOnly id="created-chat-room-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField label="Group Name" id="created-chat-room-groupName" name="groupName" data-cy="groupName" type="text" />
              <ValidatedField label="Group Owner" id="created-chat-room-groupOwner" name="groupOwner" data-cy="groupOwner" type="text" />
              <ValidatedField
                label="Introduction"
                id="created-chat-room-introduction"
                name="introduction"
                data-cy="introduction"
                type="text"
              />
              <ValidatedField label="Photo" id="created-chat-room-photo" name="photo" data-cy="photo" type="text" />
              <ValidatedField label="Broker Name" id="created-chat-room-brokerName" name="brokerName" data-cy="brokerName" type="text" />
              <ValidatedField
                label="Broker Contact"
                id="created-chat-room-brokerContact"
                name="brokerContact"
                data-cy="brokerContact"
                type="text"
              />
              <ValidatedField label="Status" id="created-chat-room-status" name="status" data-cy="status" type="select">
                {statusEnumValues.map(statusEnum => (
                  <option value={statusEnum} key={statusEnum}>
                    {statusEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Created By" id="created-chat-room-createdBy" name="createdBy" data-cy="createdBy" type="text" />
              <ValidatedField
                label="Created At"
                id="created-chat-room-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Updated At"
                id="created-chat-room-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Approved At"
                id="created-chat-room-approvedAt"
                name="approvedAt"
                data-cy="approvedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Approved By" id="created-chat-room-approvedBy" name="approvedBy" data-cy="approvedBy" type="text" />
              <ValidatedField
                label="Reject Reason"
                id="created-chat-room-rejectReason"
                name="rejectReason"
                data-cy="rejectReason"
                type="text"
              />
              <ValidatedField label="Total View" id="created-chat-room-totalView" name="totalView" data-cy="totalView" type="text" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/created-chat-room" replace color="info">
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

export default CreatedChatRoomUpdate;
