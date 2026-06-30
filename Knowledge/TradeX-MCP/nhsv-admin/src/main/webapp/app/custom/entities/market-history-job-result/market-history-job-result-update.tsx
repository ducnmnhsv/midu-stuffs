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
import { IMarketHistoryJobResult } from 'app/shared/model/market-history-job-result.model';
import { getEntity, updateEntity, createEntity, reset } from './market-history-job-result.reducer';

export const MarketHistoryJobResultUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const marketHistoryJobResultEntity = useAppSelector(state => state.marketHistoryJobResult.entity);
  const loading = useAppSelector(state => state.marketHistoryJobResult.loading);
  const updating = useAppSelector(state => state.marketHistoryJobResult.updating);
  const updateSuccess = useAppSelector(state => state.marketHistoryJobResult.updateSuccess);

  const handleClose = () => {
    navigate('/latest-job-result');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity());
    }

    dispatch(getUsers({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.timeStart = convertDateTimeToServer(values.timeStart);
    values.timeEnd = convertDateTimeToServer(values.timeEnd);

    const entity = {
      ...marketHistoryJobResultEntity,
      ...values,
      user: users.find(it => it.id.toString() === values.user.toString()),
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
          timeStart: displayDefaultDateTime(),
          timeEnd: displayDefaultDateTime(),
        }
      : {
          ...marketHistoryJobResultEntity,
          timeStart: convertDateTimeFromServer(marketHistoryJobResultEntity.timeStart),
          timeEnd: convertDateTimeFromServer(marketHistoryJobResultEntity.timeEnd),
          user: marketHistoryJobResultEntity?.user?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.marketHistoryJobResult.home.createOrEditLabel" data-cy="MarketHistoryJobResultCreateUpdateHeading">
            Create or edit a Market History Job Result
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
                <ValidatedField name="id" required readOnly id="market-history-job-result-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Is Success"
                id="market-history-job-result-is_success"
                name="is_success"
                data-cy="is_success"
                check
                type="checkbox"
              />
              <ValidatedField
                label="Time Start"
                id="market-history-job-result-time_start"
                name="time_start"
                data-cy="time_start"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Time End"
                id="market-history-job-result-time_end"
                name="time_end"
                data-cy="time_end"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Error" id="market-history-job-result-error" name="error" data-cy="error" type="text" />
              <ValidatedField id="market-history-job-result-user" name="user" data-cy="user" label="User" type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/market-history-job-result" replace color="info">
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

export default MarketHistoryJobResultUpdate;
