import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IEKyc } from 'app/shared/model/e-kyc.model';
import { getEntities as getEKycs } from 'app/entities/e-kyc/e-kyc.reducer';
import { getEntity, updateEntity, createEntity, reset } from './e-kyc-bank-list.reducer';
import { IEKycBankList } from 'app/shared/model/e-kyc-bank-list.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IEKycBankListUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycBankListUpdate = (props: IEKycBankListUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { eKycBankListEntity, eKycs, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/e-kyc-bank-list');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getEKycs();
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...eKycBankListEntity,
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
          <h2 id="eKycAdminApp.eKycBankList.home.createOrEditLabel" data-cy="EKycBankListCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.eKycBankList.home.createOrEditLabel">Create or edit a EKycBankList</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : eKycBankListEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="e-kyc-bank-list-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="e-kyc-bank-list-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="bankIdLabel" for="e-kyc-bank-list-bankId">
                  <Translate contentKey="eKycAdminApp.eKycBankList.bankId">Bank Id</Translate>
                </Label>
                <AvField id="e-kyc-bank-list-bankId" data-cy="bankId" type="text" name="bankId" />
              </AvGroup>
              <AvGroup>
                <Label id="bankNameLabel" for="e-kyc-bank-list-bankName">
                  <Translate contentKey="eKycAdminApp.eKycBankList.bankName">Bank Name</Translate>
                </Label>
                <AvField id="e-kyc-bank-list-bankName" data-cy="bankName" type="text" name="bankName" />
              </AvGroup>
              <AvGroup>
                <Label id="bankAccNoLabel" for="e-kyc-bank-list-bankAccNo">
                  <Translate contentKey="eKycAdminApp.eKycBankList.bankAccNo">Bank Acc No</Translate>
                </Label>
                <AvField id="e-kyc-bank-list-bankAccNo" data-cy="bankAccNo" type="text" name="bankAccNo" />
              </AvGroup>
              <AvGroup>
                <Label id="ownerNameLabel" for="e-kyc-bank-list-ownerName">
                  <Translate contentKey="eKycAdminApp.eKycBankList.ownerName">Owner Name</Translate>
                </Label>
                <AvField id="e-kyc-bank-list-ownerName" data-cy="ownerName" type="text" name="ownerName" />
              </AvGroup>
              <AvGroup>
                <Label id="branchIdLabel" for="e-kyc-bank-list-branchId">
                  <Translate contentKey="eKycAdminApp.eKycBankList.branchId">Branch Id</Translate>
                </Label>
                <AvField id="e-kyc-bank-list-branchId" data-cy="branchId" type="text" name="branchId" />
              </AvGroup>
              <AvGroup>
                <Label for="e-kyc-bank-list-eKyc">
                  <Translate contentKey="eKycAdminApp.eKycBankList.eKyc">E Kyc</Translate>
                </Label>
                <AvInput id="e-kyc-bank-list-eKyc" data-cy="eKyc" type="select" className="form-control" name="eKycId">
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
              <Button tag={Link} id="cancel-save" to="/e-kyc-bank-list" replace color="info">
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
  eKycBankListEntity: storeState.eKycBankList.entity,
  loading: storeState.eKycBankList.loading,
  updating: storeState.eKycBankList.updating,
  updateSuccess: storeState.eKycBankList.updateSuccess,
});

const mapDispatchToProps = {
  getEKycs,
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycBankListUpdate);
