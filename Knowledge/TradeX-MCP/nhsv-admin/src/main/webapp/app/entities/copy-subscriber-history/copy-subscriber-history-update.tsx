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
import { ICopySubscriberHistory } from 'app/shared/model/copy-subscriber-history.model';
import { OrderSetTypeEnum } from 'app/shared/model/enumerations/order-set-type-enum.model';
import { getEntity, updateEntity, createEntity, reset } from './copy-subscriber-history.reducer';

export const CopySubscriberHistoryUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const copySubscriberHistoryEntity = useAppSelector(state => state.copySubscriberHistory.entity);
  const loading = useAppSelector(state => state.copySubscriberHistory.loading);
  const updating = useAppSelector(state => state.copySubscriberHistory.updating);
  const updateSuccess = useAppSelector(state => state.copySubscriberHistory.updateSuccess);
  const orderSetTypeEnumValues = Object.keys(OrderSetTypeEnum);

  const handleClose = () => {
    navigate('/copy-subscriber-history' + location.search);
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
      ...copySubscriberHistoryEntity,
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
          orderSetType: 'QUICK_MATCH',
          ...copySubscriberHistoryEntity,
          createdAt: convertDateTimeFromServer(copySubscriberHistoryEntity.createdAt),
          updatedAt: convertDateTimeFromServer(copySubscriberHistoryEntity.updatedAt),
          mlUserId: copySubscriberHistoryEntity?.mlUserId?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.copySubscriberHistory.home.createOrEditLabel" data-cy="CopySubscriberHistoryCreateUpdateHeading">
            Create or edit a Copy Subscriber History
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
                <ValidatedField name="id" required readOnly id="copy-subscriber-history-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Account Number"
                id="copy-subscriber-history-accountNumber"
                name="accountNumber"
                data-cy="accountNumber"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Sub Number"
                id="copy-subscriber-history-subNumber"
                name="subNumber"
                data-cy="subNumber"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="User Name"
                id="copy-subscriber-history-userName"
                name="userName"
                data-cy="userName"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Allocated Ratio"
                id="copy-subscriber-history-allocatedRatio"
                name="allocatedRatio"
                data-cy="allocatedRatio"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Order Set Type"
                id="copy-subscriber-history-orderSetType"
                name="orderSetType"
                data-cy="orderSetType"
                type="select"
              >
                {orderSetTypeEnumValues.map(orderSetTypeEnum => (
                  <option value={orderSetTypeEnum} key={orderSetTypeEnum}>
                    {orderSetTypeEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Created At"
                id="copy-subscriber-history-createdAt"
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
                id="copy-subscriber-history-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField id="copy-subscriber-history-mlUserId" name="mlUserId" data-cy="mlUserId" label="Ml User Id" type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/copy-subscriber-history" replace color="info">
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

export default CopySubscriberHistoryUpdate;
