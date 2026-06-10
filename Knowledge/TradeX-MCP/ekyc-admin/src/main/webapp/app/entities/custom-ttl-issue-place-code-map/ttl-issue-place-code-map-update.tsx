import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { ToastContainer, toast } from 'react-toastify';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { getEntity, updateEntity, createEntity, reset } from './ttl-issue-place-code-map.reducer';
import { ITtlIssuePlaceCodeMap } from 'app/shared/model/ttl-issue-place-code-map.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import axios from 'axios';

export interface ITtlIssuePlaceCodeMapUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const TtlIssuePlaceCodeMapUpdate = (props: ITtlIssuePlaceCodeMapUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { ttlIssuePlaceCodeMapEntity, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/ttl-issue-place-code-map' + props.location.search);
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const reloadCache = async () => {
    await axios
      .post(`/api/v1/ekyc-admin/ekyc/reloadTtlCodeMap`, {})
      .then(res => {
        toast.success("Reload cache sucess");
      })
      .catch(err => {
        toast.error("Error");
        console.log(err);
      });
  };

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...ttlIssuePlaceCodeMapEntity,
        ...values,
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
      <ToastContainer position={toast.POSITION.TOP_LEFT} className="toastify-container" toastClassName="toastify-toast" />
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="eKycAdminApp.ttlIssuePlaceCodeMap.home.createOrEditLabel" data-cy="TtlIssuePlaceCodeMapCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.home.createOrEditLabel">
              Create or edit a TtlIssuePlaceCodeMap
            </Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : ttlIssuePlaceCodeMapEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="ttl-issue-place-code-map-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="ttl-issue-place-code-map-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="codeLabel" for="ttl-issue-place-code-map-code">
                  <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.code">Code</Translate>
                </Label>
                <AvField
                  id="ttl-issue-place-code-map-code"
                  data-cy="code"
                  type="text"
                  name="code"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="nameLabel" for="ttl-issue-place-code-map-name">
                  <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.name">Name</Translate>
                </Label>
                <AvField
                  id="ttl-issue-place-code-map-name"
                  data-cy="name"
                  type="text"
                  name="name"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup check>
                <Label id="enableRegexLabel">
                  <AvInput
                    id="ttl-issue-place-code-map-enableRegex"
                    data-cy="enableRegex"
                    type="checkbox"
                    className="form-check-input"
                    name="enableRegex"
                  />
                  <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.enableRegex">Enable Regex</Translate>
                </Label>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/ttl-issue-place-code-map" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              {!isNew && (
                <Button onClick={() => reloadCache()} tag={Link} id="cancel-save" replace color="warning">
                  <FontAwesomeIcon icon="sync" />
                  &nbsp;
                  <span className="d-none d-md-inline">
                    <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.reloadCache">Reload Cache</Translate>
                  </span>
                </Button>
              )}
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
  ttlIssuePlaceCodeMapEntity: storeState.ttlIssuePlaceCodeMap.entity,
  loading: storeState.ttlIssuePlaceCodeMap.loading,
  updating: storeState.ttlIssuePlaceCodeMap.updating,
  updateSuccess: storeState.ttlIssuePlaceCodeMap.updateSuccess,
});

const mapDispatchToProps = {
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(TtlIssuePlaceCodeMapUpdate);
