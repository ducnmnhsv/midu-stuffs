import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ICopyPortfolioHistory } from 'app/shared/model/copy-portfolio-history.model';
import { getEntities as getCopyPortfolioHistories } from 'app/entities/copy-portfolio-history/copy-portfolio-history.reducer';
import { ICopyPortfolioDetailHistory } from 'app/shared/model/copy-portfolio-detail-history.model';
import { getEntity, updateEntity, createEntity, reset } from './copy-portfolio-detail-history.reducer';

export const CopyPortfolioDetailHistoryUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const copyPortfolioHistories = useAppSelector(state => state.copyPortfolioHistory.entities);
  const copyPortfolioDetailHistoryEntity = useAppSelector(state => state.copyPortfolioDetailHistory.entity);
  const loading = useAppSelector(state => state.copyPortfolioDetailHistory.loading);
  const updating = useAppSelector(state => state.copyPortfolioDetailHistory.updating);
  const updateSuccess = useAppSelector(state => state.copyPortfolioDetailHistory.updateSuccess);

  const handleClose = () => {
    navigate('/copy-portfolio-detail-history' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getCopyPortfolioHistories({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.createdAt = convertDateTimeToServer(values.createdAt);

    const entity = {
      ...copyPortfolioDetailHistoryEntity,
      ...values,
      copyPortfolioHistoryId: copyPortfolioHistories.find(it => it.id.toString() === values.copyPortfolioHistoryId.toString()),
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
        }
      : {
          ...copyPortfolioDetailHistoryEntity,
          createdAt: convertDateTimeFromServer(copyPortfolioDetailHistoryEntity.createdAt),
          copyPortfolioHistoryId: copyPortfolioDetailHistoryEntity?.copyPortfolioHistoryId?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.copyPortfolioDetailHistory.home.createOrEditLabel" data-cy="CopyPortfolioDetailHistoryCreateUpdateHeading">
            Create or edit a Copy Portfolio Detail History
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
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="copy-portfolio-detail-history-id"
                  label="ID"
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label="Symbol"
                id="copy-portfolio-detail-history-symbol"
                name="symbol"
                data-cy="symbol"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Weight"
                id="copy-portfolio-detail-history-weight"
                name="weight"
                data-cy="weight"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Created At"
                id="copy-portfolio-detail-history-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                id="copy-portfolio-detail-history-copyPortfolioHistoryId"
                name="copyPortfolioHistoryId"
                data-cy="copyPortfolioHistoryId"
                label="Copy Portfolio History Id"
                type="select"
              >
                <option value="" key="0" />
                {copyPortfolioHistories
                  ? copyPortfolioHistories.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button
                tag={Link}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/copy-portfolio-detail-history"
                replace
                color="info"
              >
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

export default CopyPortfolioDetailHistoryUpdate;
