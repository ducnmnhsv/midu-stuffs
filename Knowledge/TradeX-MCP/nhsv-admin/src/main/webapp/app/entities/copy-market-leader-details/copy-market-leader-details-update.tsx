import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IUser } from 'app/shared/model/user.model';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { ICopyMarketLeaderDetails } from 'app/shared/model/copy-market-leader-details.model';
import { getEntity, updateEntity, createEntity, reset } from './copy-market-leader-details.reducer';

export const CopyMarketLeaderDetailsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const copyMarketLeaderDetailsEntity = useAppSelector(state => state.copyMarketLeaderDetails.entity);
  const loading = useAppSelector(state => state.copyMarketLeaderDetails.loading);
  const updating = useAppSelector(state => state.copyMarketLeaderDetails.updating);
  const updateSuccess = useAppSelector(state => state.copyMarketLeaderDetails.updateSuccess);

  const handleClose = () => {
    navigate('/copy-market-leader-details' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);

    const entity = {
      ...copyMarketLeaderDetailsEntity,
      ...values,
      mlUserId: users.find(it => it.id.toString() === values.mlUserId.toString()),
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
        }
      : {
          ...copyMarketLeaderDetailsEntity,
          createdAt: convertDateTimeFromServer(copyMarketLeaderDetailsEntity.createdAt),
          updatedAt: convertDateTimeFromServer(copyMarketLeaderDetailsEntity.updatedAt),
          mlUserId: copyMarketLeaderDetailsEntity?.mlUserId?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.copyMarketLeaderDetails.home.createOrEditLabel" data-cy="CopyMarketLeaderDetailsCreateUpdateHeading">
            Create or edit a Copy Market Leader Details
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
                <ValidatedField name="id" required readOnly id="copy-market-leader-details-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Created At"
                id="copy-market-leader-details-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Updated At"
                id="copy-market-leader-details-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Type"
                id="copy-market-leader-details-type"
                name="type"
                data-cy="type"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Label"
                id="copy-market-leader-details-label"
                name="label"
                data-cy="label"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Key"
                id="copy-market-leader-details-key"
                name="key"
                data-cy="key"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Value"
                id="copy-market-leader-details-value"
                name="value"
                data-cy="value"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 2000, message: 'This field cannot be longer than 2000 characters.' },
                }}
              />
              <ValidatedField id="copy-market-leader-details-mlUserId" name="mlUserId" data-cy="mlUserId" label="Ml User Id" type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/copy-market-leader-details" replace color="info">
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

export default CopyMarketLeaderDetailsUpdate;
