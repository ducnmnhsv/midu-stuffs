import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IEKycAdditionalInfo } from 'app/shared/model/e-kyc-additional-info.model';
import { getEntities as getEKycAdditionalInfos } from 'app/entities/e-kyc-additional-info/e-kyc-additional-info.reducer';
import { getEntity, updateEntity, createEntity, reset } from './blockholder.reducer';
import { IBlockholder } from 'app/shared/model/blockholder.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IBlockholderUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BlockholderUpdate = (props: IBlockholderUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { blockholderEntity, eKycAdditionalInfos, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/blockholder');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getEKycAdditionalInfos();
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...blockholderEntity,
        ...values,
        eKycAdditionalInfo: eKycAdditionalInfos.find(it => it.id.toString() === values.eKycAdditionalInfoId.toString()),
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
          <h2 id="eKycAdminApp.blockholder.home.createOrEditLabel" data-cy="BlockholderCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.blockholder.home.createOrEditLabel">Create or edit a Blockholder</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : blockholderEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="blockholder-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="blockholder-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="companyNameLabel" for="blockholder-companyName">
                  <Translate contentKey="eKycAdminApp.blockholder.companyName">Company Name</Translate>
                </Label>
                <AvField id="blockholder-companyName" data-cy="companyName" type="text" name="companyName" />
              </AvGroup>
              <AvGroup>
                <Label id="stockLabel" for="blockholder-stock">
                  <Translate contentKey="eKycAdminApp.blockholder.stock">Stock</Translate>
                </Label>
                <AvField id="blockholder-stock" data-cy="stock" type="text" name="stock" />
              </AvGroup>
              <AvGroup>
                <Label id="positionLabel" for="blockholder-position">
                  <Translate contentKey="eKycAdminApp.blockholder.position">Position</Translate>
                </Label>
                <AvField id="blockholder-position" data-cy="position" type="text" name="position" />
              </AvGroup>
              <AvGroup>
                <Label for="blockholder-eKycAdditionalInfo">
                  <Translate contentKey="eKycAdminApp.blockholder.eKycAdditionalInfo">E Kyc Additional Info</Translate>
                </Label>
                <AvInput
                  id="blockholder-eKycAdditionalInfo"
                  data-cy="eKycAdditionalInfo"
                  type="select"
                  className="form-control"
                  name="eKycAdditionalInfoId"
                >
                  <option value="" key="0" />
                  {eKycAdditionalInfos
                    ? eKycAdditionalInfos.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/blockholder" replace color="info">
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
  eKycAdditionalInfos: storeState.eKycAdditionalInfo.entities,
  blockholderEntity: storeState.blockholder.entity,
  loading: storeState.blockholder.loading,
  updating: storeState.blockholder.updating,
  updateSuccess: storeState.blockholder.updateSuccess,
});

const mapDispatchToProps = {
  getEKycAdditionalInfos,
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BlockholderUpdate);
