import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { setFileData, byteSize, Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IEKyc } from 'app/shared/model/e-kyc.model';
import { getEntities as getEKycs } from 'app/entities/e-kyc/e-kyc.reducer';
import { getEntity, updateEntity, createEntity, setBlob, reset } from './e-kyc-ext.reducer';
import { IEKycExt } from 'app/shared/model/e-kyc-ext.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IEKycExtUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycExtUpdate = (props: IEKycExtUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { eKycExtEntity, eKycs, loading, updating } = props;

  const { rawData } = eKycExtEntity;

  const handleClose = () => {
    props.history.push('/e-kyc-ext');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getEKycs();
  }, []);

  const onBlobChange = (isAnImage, name) => event => {
    setFileData(event, (contentType, data) => props.setBlob(name, data, contentType), isAnImage);
  };

  const clearBlob = name => () => {
    props.setBlob(name, undefined, undefined);
  };

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...eKycExtEntity,
        ...values,
        eKyc: eKycs.find(it => it.id.toString() === values.eKycId.toString()),
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="eKycAdminApp.eKycExt.home.createOrEditLabel" data-cy="EKycExtCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.eKycExt.home.createOrEditLabel">Create or edit a EKycExt</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : eKycExtEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="e-kyc-ext-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="e-kyc-ext-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="logIdLabel" for="e-kyc-ext-logId">
                  <Translate contentKey="eKycAdminApp.eKycExt.logId">Log Id</Translate>
                </Label>
                <AvField id="e-kyc-ext-logId" data-cy="logId" type="text" name="logId" />
              </AvGroup>
              <AvGroup>
                <Label id="rawDataLabel" for="e-kyc-ext-rawData">
                  <Translate contentKey="eKycAdminApp.eKycExt.rawData">Raw Data</Translate>
                </Label>
                <AvInput id="e-kyc-ext-rawData" data-cy="rawData" type="textarea" name="rawData" />
              </AvGroup>
              <AvGroup>
                <Label for="e-kyc-ext-eKyc">
                  <Translate contentKey="eKycAdminApp.eKycExt.eKyc">E Kyc</Translate>
                </Label>
                <AvInput id="e-kyc-ext-eKyc" data-cy="eKyc" type="select" className="form-control" name="eKycId">
                  <option value="" key="0" />
                  {eKycs
                    ? eKycs.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/e-kyc-ext" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  eKycs: storeState.eKyc.entities,
  eKycExtEntity: storeState.eKycExt.entity,
  loading: storeState.eKycExt.loading,
  updating: storeState.eKycExt.updating,
  updateSuccess: storeState.eKycExt.updateSuccess,
});

const mapDispatchToProps = {
  getEKycs,
  getEntity,
  updateEntity,
  setBlob,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycExtUpdate);
