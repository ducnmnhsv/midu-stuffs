import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IBroker } from 'app/shared/model/broker.model';
import { getEntity, updateEntity, createEntity, reset } from './broker.reducer';

export const BrokerUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const brokerEntity = useAppSelector(state => state.broker.entity);
  const loading = useAppSelector(state => state.broker.loading);
  const updating = useAppSelector(state => state.broker.updating);
  const updateSuccess = useAppSelector(state => state.broker.updateSuccess);

  const handleClose = () => {
    navigate('/broker');
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
    values.deactivatedAt = convertDateTimeToServer(values.deactivatedAt);

    const entity = {
      ...brokerEntity,
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
          deactivatedAt: displayDefaultDateTime(),
        }
      : {
          ...brokerEntity,
          createdAt: convertDateTimeFromServer(brokerEntity.createdAt),
          updatedAt: convertDateTimeFromServer(brokerEntity.updatedAt),
          deactivatedAt: convertDateTimeFromServer(brokerEntity.deactivatedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.broker.home.createOrEditLabel" data-cy="BrokerCreateUpdateHeading">
            Create or edit a Broker
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="broker-id" label="Id" validate={{ required: true }} /> : null}
              <ValidatedField label="Username" id="broker-username" name="username" data-cy="username" type="text" />
              <ValidatedField label="Fullname" id="broker-fullname" name="fullname" data-cy="fullname" type="text" />
              <ValidatedField label="Status" id="broker-status" name="status" data-cy="status" check type="checkbox" />
              <ValidatedField label="Total Chat Room" id="broker-totalChatRoom" name="totalChatRoom" data-cy="totalChatRoom" type="text" />
              <ValidatedField label="Current Rank" id="broker-currentRank" name="currentRank" data-cy="currentRank" type="text" />
              <ValidatedField label="Is Dynamic" id="broker-isDynamic" name="isDynamic" data-cy="isDynamic" check type="checkbox" />
              <ValidatedField label="Email" id="broker-email" name="email" data-cy="email" type="text" validate={{}} />
              <ValidatedField
                label="Created At"
                id="broker-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Updated At"
                id="broker-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Deactivated At"
                id="broker-deactivatedAt"
                name="deactivatedAt"
                data-cy="deactivatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Deactivated By" id="broker-deactivatedBy" name="deactivatedBy" data-cy="deactivatedBy" type="text" />
              <ValidatedField label="Invited By" id="broker-invitedBy" name="invitedBy" data-cy="invitedBy" type="text" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/broker" replace color="info">
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

export default BrokerUpdate;
