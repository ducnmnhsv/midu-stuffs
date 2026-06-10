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
import { getEntity, updateEntity, createEntity, reset } from './e-kyc-additional-info.reducer';
import { IEKycAdditionalInfo } from 'app/shared/model/e-kyc-additional-info.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IEKycAdditionalInfoUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycAdditionalInfoUpdate = (props: IEKycAdditionalInfoUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { eKycAdditionalInfoEntity, eKycs, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/e-kyc-additional-info');
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
        ...eKycAdditionalInfoEntity,
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
          <h2 id="eKycAdminApp.eKycAdditionalInfo.home.createOrEditLabel" data-cy="EKycAdditionalInfoCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.home.createOrEditLabel">Create or edit a EKycAdditionalInfo</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : eKycAdditionalInfoEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="e-kyc-additional-info-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="e-kyc-additional-info-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="fullNameLabel" for="e-kyc-additional-info-fullName">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.fullName">Full Name</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-fullName" data-cy="fullName" type="text" name="fullName" />
              </AvGroup>
              <AvGroup>
                <Label id="birthDayLabel" for="e-kyc-additional-info-birthDay">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.birthDay">Birth Day</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-birthDay" data-cy="birthDay" type="text" name="birthDay" />
              </AvGroup>
              <AvGroup>
                <Label id="nationalityLabel" for="e-kyc-additional-info-nationality">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.nationality">Nationality</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-nationality" data-cy="nationality" type="text" name="nationality" />
              </AvGroup>
              <AvGroup>
                <Label id="identifierIdLabel" for="e-kyc-additional-info-identifierId">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.identifierId">Identifier Id</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-identifierId" data-cy="identifierId" type="text" name="identifierId" />
              </AvGroup>
              <AvGroup>
                <Label id="issueDateLabel" for="e-kyc-additional-info-issueDate">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.issueDate">Issue Date</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-issueDate" data-cy="issueDate" type="text" name="issueDate" />
              </AvGroup>
              <AvGroup>
                <Label id="issuePlaceLabel" for="e-kyc-additional-info-issuePlace">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.issuePlace">Issue Place</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-issuePlace" data-cy="issuePlace" type="text" name="issuePlace" />
              </AvGroup>
              <AvGroup>
                <Label id="permanentAddressLabel" for="e-kyc-additional-info-permanentAddress">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.permanentAddress">Permanent Address</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-permanentAddress" data-cy="permanentAddress" type="text" name="permanentAddress" />
              </AvGroup>
              <AvGroup>
                <Label id="contactAddressLabel" for="e-kyc-additional-info-contactAddress">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.contactAddress">Contact Address</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-contactAddress" data-cy="contactAddress" type="text" name="contactAddress" />
              </AvGroup>
              <AvGroup>
                <Label id="occupationLabel" for="e-kyc-additional-info-occupation">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.occupation">Occupation</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-occupation" data-cy="occupation" type="text" name="occupation" />
              </AvGroup>
              <AvGroup>
                <Label id="positionLabel" for="e-kyc-additional-info-position">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.position">Position</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-position" data-cy="position" type="text" name="position" />
              </AvGroup>
              <AvGroup>
                <Label id="phoneNumberLabel" for="e-kyc-additional-info-phoneNumber">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.phoneNumber">Phone Number</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-phoneNumber" data-cy="phoneNumber" type="text" name="phoneNumber" />
              </AvGroup>
              <AvGroup>
                <Label id="visaNoLabel" for="e-kyc-additional-info-visaNo">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.visaNo">Visa No</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-visaNo" data-cy="visaNo" type="text" name="visaNo" />
              </AvGroup>
              <AvGroup>
                <Label id="visaIssuePlaceLabel" for="e-kyc-additional-info-visaIssuePlace">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.visaIssuePlace">Visa Issue Place</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-visaIssuePlace" data-cy="visaIssuePlace" type="text" name="visaIssuePlace" />
              </AvGroup>
              <AvGroup>
                <Label id="foreignResidenceLabel" for="e-kyc-additional-info-foreignResidence">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.foreignResidence">Foreign Residence</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-foreignResidence" data-cy="foreignResidence" type="text" name="foreignResidence" />
              </AvGroup>
              <AvGroup>
                <Label id="investmentGoalLabel" for="e-kyc-additional-info-investmentGoal">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.investmentGoal">Investment Goal</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-investmentGoal" data-cy="investmentGoal" type="text" name="investmentGoal" />
              </AvGroup>
              <AvGroup>
                <Label id="riskLabel" for="e-kyc-additional-info-risk">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.risk">Risk</Translate>
                </Label>
                <AvField id="e-kyc-additional-info-risk" data-cy="risk" type="text" name="risk" />
              </AvGroup>
              <AvGroup check>
                <Label id="experiencedLabel">
                  <AvInput
                    id="e-kyc-additional-info-experienced"
                    data-cy="experienced"
                    type="checkbox"
                    className="form-check-input"
                    name="experienced"
                  />
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.experienced">Experienced</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label for="e-kyc-additional-info-eKyc">
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.eKyc">E Kyc</Translate>
                </Label>
                <AvInput id="e-kyc-additional-info-eKyc" data-cy="eKyc" type="select" className="form-control" name="eKycId">
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
              <Button tag={Link} id="cancel-save" to="/e-kyc-additional-info" replace color="info">
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
  eKycAdditionalInfoEntity: storeState.eKycAdditionalInfo.entity,
  loading: storeState.eKycAdditionalInfo.loading,
  updating: storeState.eKycAdditionalInfo.updating,
  updateSuccess: storeState.eKycAdditionalInfo.updateSuccess,
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

export default connect(mapStateToProps, mapDispatchToProps)(EKycAdditionalInfoUpdate);
