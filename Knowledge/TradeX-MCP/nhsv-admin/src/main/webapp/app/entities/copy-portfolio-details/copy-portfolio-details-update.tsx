import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ICopyPortfolio } from 'app/shared/model/copy-portfolio.model';
import { getEntities as getCopyPortfolios } from 'app/entities/copy-portfolio/copy-portfolio.reducer';
import { ICopyPortfolioDetails } from 'app/shared/model/copy-portfolio-details.model';
import { getEntity, updateEntity, createEntity, reset } from './copy-portfolio-details.reducer';

export const CopyPortfolioDetailsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const copyPortfolios = useAppSelector(state => state.copyPortfolio.entities);
  const copyPortfolioDetailsEntity = useAppSelector(state => state.copyPortfolioDetails.entity);
  const loading = useAppSelector(state => state.copyPortfolioDetails.loading);
  const updating = useAppSelector(state => state.copyPortfolioDetails.updating);
  const updateSuccess = useAppSelector(state => state.copyPortfolioDetails.updateSuccess);

  const handleClose = () => {
    navigate('/copy-portfolio-details' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getCopyPortfolios({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.createdAt = convertDateTimeToServer(values.createdAt);

    const entity = {
      ...copyPortfolioDetailsEntity,
      ...values,
      copyPortfolioId: copyPortfolios.find(it => it.id.toString() === values.copyPortfolioId.toString()),
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
          ...copyPortfolioDetailsEntity,
          createdAt: convertDateTimeFromServer(copyPortfolioDetailsEntity.createdAt),
          copyPortfolioId: copyPortfolioDetailsEntity?.copyPortfolioId?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.copyPortfolioDetails.home.createOrEditLabel" data-cy="CopyPortfolioDetailsCreateUpdateHeading">
            Create or edit a Copy Portfolio Details
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
                <ValidatedField name="id" required readOnly id="copy-portfolio-details-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Symbol"
                id="copy-portfolio-details-symbol"
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
                id="copy-portfolio-details-weight"
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
                id="copy-portfolio-details-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                id="copy-portfolio-details-copyPortfolioId"
                name="copyPortfolioId"
                data-cy="copyPortfolioId"
                label="Copy Portfolio Id"
                type="select"
              >
                <option value="" key="0" />
                {copyPortfolios
                  ? copyPortfolios.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/copy-portfolio-details" replace color="info">
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

export default CopyPortfolioDetailsUpdate;
