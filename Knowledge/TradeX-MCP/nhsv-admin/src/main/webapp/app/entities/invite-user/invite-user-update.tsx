import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IInviteUser } from 'app/shared/model/invite-user.model';
import { InviteStatusEnum } from 'app/shared/model/enumerations/invite-status-enum.model';
import { getEntity, updateEntity, createEntity, reset } from './invite-user.reducer';

export const InviteUserUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const inviteUserEntity = useAppSelector(state => state.inviteUser.entity);
  const loading = useAppSelector(state => state.inviteUser.loading);
  const updating = useAppSelector(state => state.inviteUser.updating);
  const updateSuccess = useAppSelector(state => state.inviteUser.updateSuccess);
  const inviteStatusEnumValues = Object.keys(InviteStatusEnum);

  const handleClose = () => {
    navigate('/invite-user' + location.search);
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
    values.activationDate = convertDateTimeToServer(values.activationDate);

    const entity = {
      ...inviteUserEntity,
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
          activationDate: displayDefaultDateTime(),
        }
      : {
          status: 'PENDING',
          ...inviteUserEntity,
          createdAt: convertDateTimeFromServer(inviteUserEntity.createdAt),
          updatedAt: convertDateTimeFromServer(inviteUserEntity.updatedAt),
          activationDate: convertDateTimeFromServer(inviteUserEntity.activationDate),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.inviteUser.home.createOrEditLabel" data-cy="InviteUserCreateUpdateHeading">
            Create or edit a Invite User
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="invite-user-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="Login" id="invite-user-login" name="login" data-cy="login" type="text" />
              <ValidatedField label="Email" id="invite-user-email" name="email" data-cy="email" type="text" />
              <ValidatedField label="Status" id="invite-user-status" name="status" data-cy="status" type="select">
                {inviteStatusEnumValues.map(inviteStatusEnum => (
                  <option value={inviteStatusEnum} key={inviteStatusEnum}>
                    {inviteStatusEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Created At"
                id="invite-user-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Updated At"
                id="invite-user-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Created Id" id="invite-user-createdId" name="createdId" data-cy="createdId" type="text" />
              <ValidatedField label="Created By" id="invite-user-createdBy" name="createdBy" data-cy="createdBy" type="text" />
              <ValidatedField
                label="Activation Key"
                id="invite-user-activationKey"
                name="activationKey"
                data-cy="activationKey"
                type="text"
              />
              <ValidatedField
                label="Activation Date"
                id="invite-user-activationDate"
                name="activationDate"
                data-cy="activationDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Lang Key" id="invite-user-langKey" name="langKey" data-cy="langKey" type="text" />
              <ValidatedField label="Authorities" id="invite-user-authorities" name="authorities" data-cy="authorities" type="text" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/invite-user" replace color="info">
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

export default InviteUserUpdate;
