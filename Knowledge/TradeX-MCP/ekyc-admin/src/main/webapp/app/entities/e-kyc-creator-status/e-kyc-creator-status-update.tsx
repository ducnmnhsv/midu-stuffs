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
import { getEntity, updateEntity, createEntity, setBlob, reset } from './e-kyc-creator-status.reducer';
import { IEKycCreatorStatus } from 'app/shared/model/e-kyc-creator-status.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IEKycCreatorStatusUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycCreatorStatusUpdate = (props: IEKycCreatorStatusUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { eKycCreatorStatusEntity, eKycs, loading, updating } = props;

  const { fullResult } = eKycCreatorStatusEntity;

  const handleClose = () => {
    props.history.push('/e-kyc-creator-status');
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
    values.updatedAt = convertDateTimeToServer(values.updatedAt);

    if (errors.length === 0) {
      const entity = {
        ...eKycCreatorStatusEntity,
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
          <h2 id="eKycAdminApp.eKycCreatorStatus.home.createOrEditLabel" data-cy="EKycCreatorStatusCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.eKycCreatorStatus.home.createOrEditLabel">Create or edit a EKycCreatorStatus</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : eKycCreatorStatusEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="e-kyc-creator-status-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="e-kyc-creator-status-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="statusLabel" for="e-kyc-creator-status-status">
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.status">Status</Translate>
                </Label>
                <AvField id="e-kyc-creator-status-status" data-cy="status" type="text" name="status" />
              </AvGroup>
              <AvGroup>
                <Label id="reasonLabel" for="e-kyc-creator-status-reason">
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.reason">Reason</Translate>
                </Label>
                <AvField id="e-kyc-creator-status-reason" data-cy="reason" type="text" name="reason" />
              </AvGroup>
              <AvGroup>
                <Label id="updatedAtLabel" for="e-kyc-creator-status-updatedAt">
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.updatedAt">Updated At</Translate>
                </Label>
                <AvInput
                  id="e-kyc-creator-status-updatedAt"
                  data-cy="updatedAt"
                  type="datetime-local"
                  className="form-control"
                  name="updatedAt"
                  placeholder={'YYYY-MM-DD HH:mm'}
                  value={isNew ? displayDefaultDateTime() : convertDateTimeFromServer(props.eKycCreatorStatusEntity.updatedAt)}
                />
              </AvGroup>
              <AvGroup>
                <Label id="updatedByLabel" for="e-kyc-creator-status-updatedBy">
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.updatedBy">Updated By</Translate>
                </Label>
                <AvField id="e-kyc-creator-status-updatedBy" data-cy="updatedBy" type="text" name="updatedBy" />
              </AvGroup>
              <AvGroup>
                <Label id="fullResultLabel" for="e-kyc-creator-status-fullResult">
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.fullResult">Full Result</Translate>
                </Label>
                <AvInput id="e-kyc-creator-status-fullResult" data-cy="fullResult" type="textarea" name="fullResult" />
              </AvGroup>
              <AvGroup>
                <Label for="e-kyc-creator-status-eKyc">
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.eKyc">E Kyc</Translate>
                </Label>
                <AvInput id="e-kyc-creator-status-eKyc" data-cy="eKyc" type="select" className="form-control" name="eKycId">
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
              <Button tag={Link} id="cancel-save" to="/e-kyc-creator-status" replace color="info">
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
  eKycCreatorStatusEntity: storeState.eKycCreatorStatus.entity,
  loading: storeState.eKycCreatorStatus.loading,
  updating: storeState.eKycCreatorStatus.updating,
  updateSuccess: storeState.eKycCreatorStatus.updateSuccess,
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

export default connect(mapStateToProps, mapDispatchToProps)(EKycCreatorStatusUpdate);
