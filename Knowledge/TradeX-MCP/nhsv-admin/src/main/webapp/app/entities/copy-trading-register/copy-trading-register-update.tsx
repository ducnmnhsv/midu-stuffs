import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { ICopyTradingRegister } from 'app/shared/model/copy-trading-register.model';
import { getEntity, updateEntity, createEntity, reset } from './copy-trading-register.reducer';
export const CopyTradingRegisterUpdate = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;
  const copyTradingRegisterEntity = useAppSelector(state => state.copyTradingRegister.entity);
  const loading = useAppSelector(state => state.copyTradingRegister.loading);
  const updating = useAppSelector(state => state.copyTradingRegister.updating);
  const updateSuccess = useAppSelector(state => state.copyTradingRegister.updateSuccess);
  const handleClose = () => {
    navigate('/copy-trading-register' + location.search);
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
    values.createAt = convertDateTimeToServer(values.createAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);
    const entity = {
      ...copyTradingRegisterEntity,
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
        createAt: displayDefaultDateTime(),
        updatedAt: displayDefaultDateTime(),
      }
      : {
        ...copyTradingRegisterEntity,
        createAt: convertDateTimeFromServer(copyTradingRegisterEntity.createAt),
        updatedAt: convertDateTimeFromServer(copyTradingRegisterEntity.updatedAt),
      };
  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.copyTradingRegister.home.createOrEditLabel" data-cy="CopyTradingRegisterCreateUpdateHeading">
            Create or edit a Copy Trading Register
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
                <ValidatedField name="id" required readOnly id="copy-trading-register-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Account Number"
                id="copy-trading-register-accountNumber"
                name="accountNumber"
                data-cy="accountNumber"
                type="text"
              />
              <ValidatedField
                label="Sub Account"
                id="copy-trading-register-subAccount"
                name="subAccount"
                data-cy="subAccount"
                type="text"
              />
              <ValidatedField
                label="Customer Name"
                id="copy-trading-register-customerName"
                name="customerName"
                data-cy="customerName"
                type="text"
              />
              <ValidatedField label="Status" id="copy-trading-register-status" name="status" data-cy="status" check type="checkbox" />
              <ValidatedField
                label="Create At"
                id="copy-trading-register-createAt"
                name="createAt"
                data-cy="createAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Updated At"
                id="copy-trading-register-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/copy-trading-register" replace color="info">
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
export default CopyTradingRegisterUpdate;
