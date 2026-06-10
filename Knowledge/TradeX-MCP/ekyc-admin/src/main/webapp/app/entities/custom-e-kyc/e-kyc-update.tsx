import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { getEntity, updateEntity, createEntity, reset } from './e-kyc.reducer';
import { IEKyc } from 'app/shared/model/custom-e-kyc.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IEKycUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycUpdate = (props: IEKycUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { eKycEntity, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/');
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

  const saveEntity = (event, errors, values) => {
    values.updatedAt = convertDateTimeToServer(values.updatedAt);
    values.createdAt = convertDateTimeToServer(values.createdAt);

    if (errors.length === 0) {
      const entity = {
        ...eKycEntity,
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
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="eKycAdminApp.customEKyc.home.createOrEditLabel" data-cy="EKycCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.customEKyc.home.createOrEditLabel">Create or edit a EKyc</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : eKycEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="e-kyc-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="e-kyc-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="identifierIdLabel" for="e-kyc-identifierId">
                  <Translate contentKey="eKycAdminApp.customEKyc.identifierId">Identifier Id</Translate>
                </Label>
                <AvField
                  id="e-kyc-identifierId"
                  data-cy="identifierId"
                  type="text"
                  name="identifierId"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    minLength: { value: 1, errorMessage: translate('entity.validation.minlength', { min: 1 }) },
                    maxLength: { value: 20, errorMessage: translate('entity.validation.maxlength', { max: 20 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="fullNameLabel" for="e-kyc-fullName">
                  <Translate contentKey="eKycAdminApp.customEKyc.fullName">Full Name</Translate>
                </Label>
                <AvField
                  id="e-kyc-fullName"
                  data-cy="fullName"
                  type="text"
                  name="fullName"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    minLength: { value: 1, errorMessage: translate('entity.validation.minlength', { min: 1 }) },
                    maxLength: { value: 50, errorMessage: translate('entity.validation.maxlength', { max: 50 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="phoneNoLabel" for="e-kyc-phoneNo">
                  <Translate contentKey="eKycAdminApp.customEKyc.phoneNo">Phone No</Translate>
                </Label>
                <AvField
                  id="e-kyc-phoneNo"
                  data-cy="phoneNo"
                  type="text"
                  name="phoneNo"
                  validate={{
                    pattern: {
                      value: '^[+]{0,1}[-\\s0-9]*$',
                      errorMessage: translate('entity.validation.pattern', { pattern: '^[+]{0,1}[-\\s0-9]*$' }),
                    },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="genderLabel" for="e-kyc-gender">
                  <Translate contentKey="eKycAdminApp.customEKyc.gender">Gender</Translate>
                </Label>
                <AvField id="e-kyc-gender" data-cy="gender" type="text" name="gender" />
              </AvGroup>
              <AvGroup>
                <Label id="typeLabel" for="e-kyc-type">
                  <Translate contentKey="eKycAdminApp.customEKyc.type">Type</Translate>
                </Label>
                <AvInput
                  id="e-kyc-type"
                  data-cy="type"
                  type="select"
                  className="form-control"
                  name="type"
                  value={(!isNew && eKycEntity.type) || 'CMND'}
                >
                  <option value="CMND">{translate('eKycAdminApp.customEKyc.ype.CMND')}</option>
                  <option value="CC">{translate('eKycAdminApp.customEKyc.ype.CC')}</option>
                  <option value="PASSPORT">{translate('eKycAdminApp.customEKyc.ype.PASSPORT')}</option>
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label id="birthDayLabel" for="e-kyc-birthDay">
                  <Translate contentKey="eKycAdminApp.customEKyc.birthDay">Birth Day</Translate>
                </Label>
                <AvField
                  id="e-kyc-birthDay"
                  data-cy="birthDay"
                  type="date"
                  className="form-control"
                  name="birthDay"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="expiredDateLabel" for="e-kyc-expiredDate">
                  <Translate contentKey="eKycAdminApp.customEKyc.expiredDate">Expired Date</Translate>
                </Label>
                <AvField
                  id="e-kyc-expiredDate"
                  data-cy="expiredDate"
                  type="date"
                  className="form-control"
                  name="expiredDate"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="issueDateLabel" for="e-kyc-issueDate">
                  <Translate contentKey="eKycAdminApp.customEKyc.issueDate">Issue Date</Translate>
                </Label>
                <AvField
                  id="e-kyc-issueDate"
                  data-cy="issueDate"
                  type="date"
                  className="form-control"
                  name="issueDate"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="issuePlaceLabel" for="e-kyc-issuePlace">
                  <Translate contentKey="eKycAdminApp.customEKyc.issuePlace">Issue Place</Translate>
                </Label>
                <AvField
                  id="e-kyc-issuePlace"
                  data-cy="issuePlace"
                  type="text"
                  name="issuePlace"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    minLength: { value: 1, errorMessage: translate('entity.validation.minlength', { min: 1 }) },
                    maxLength: { value: 100, errorMessage: translate('entity.validation.maxlength', { max: 100 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="addressLabel" for="e-kyc-address">
                  <Translate contentKey="eKycAdminApp.customEKyc.address">Address</Translate>
                </Label>
                <AvField
                  id="e-kyc-address"
                  data-cy="address"
                  type="text"
                  name="address"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    minLength: { value: 1, errorMessage: translate('entity.validation.minlength', { min: 1 }) },
                    maxLength: { value: 100, errorMessage: translate('entity.validation.maxlength', { max: 100 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="occupationLabel" for="e-kyc-occupation">
                  <Translate contentKey="eKycAdminApp.customEKyc.occupation">Occupation</Translate>
                </Label>
                <AvField id="e-kyc-occupation" data-cy="occupation" type="text" name="occupation" />
              </AvGroup>
              <AvGroup>
                <Label id="homeTownLabel" for="e-kyc-homeTown">
                  <Translate contentKey="eKycAdminApp.customEKyc.homeTown">Home Town</Translate>
                </Label>
                <AvField
                  id="e-kyc-homeTown"
                  data-cy="homeTown"
                  type="text"
                  name="homeTown"
                  validate={{
                    minLength: { value: 1, errorMessage: translate('entity.validation.minlength', { min: 1 }) },
                    maxLength: { value: 100, errorMessage: translate('entity.validation.maxlength', { max: 100 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="permanentProvinceLabel" for="e-kyc-permanentProvince">
                  <Translate contentKey="eKycAdminApp.customEKyc.permanentProvince">Permanent Province</Translate>
                </Label>
                <AvField id="e-kyc-permanentProvince" data-cy="permanentProvince" type="text" name="permanentProvince" />
              </AvGroup>
              <AvGroup>
                <Label id="permanentDistrictLabel" for="e-kyc-permanentDistrict">
                  <Translate contentKey="eKycAdminApp.customEKyc.permanentDistrict">Permanent District</Translate>
                </Label>
                <AvField id="e-kyc-permanentDistrict" data-cy="permanentDistrict" type="text" name="permanentDistrict" />
              </AvGroup>
              <AvGroup>
                <Label id="permanentAddressLabel" for="e-kyc-permanentAddress">
                  <Translate contentKey="eKycAdminApp.customEKyc.permanentAddress">Permanent Address</Translate>
                </Label>
                <AvField id="e-kyc-permanentAddress" data-cy="permanentAddress" type="text" name="permanentAddress" />
              </AvGroup>
              <AvGroup>
                <Label id="contactProvinceLabel" for="e-kyc-contactProvince">
                  <Translate contentKey="eKycAdminApp.customEKyc.contactProvince">Contact Province</Translate>
                </Label>
                <AvField id="e-kyc-contactProvince" data-cy="contactProvince" type="text" name="contactProvince" />
              </AvGroup>
              <AvGroup>
                <Label id="contactDistrictLabel" for="e-kyc-contactDistrict">
                  <Translate contentKey="eKycAdminApp.customEKyc.contactDistrict">Contact District</Translate>
                </Label>
                <AvField id="e-kyc-contactDistrict" data-cy="contactDistrict" type="text" name="contactDistrict" />
              </AvGroup>
              <AvGroup>
                <Label id="contactAddressLabel" for="e-kyc-contactAddress">
                  <Translate contentKey="eKycAdminApp.customEKyc.contactAddress">Contact Address</Translate>
                </Label>
                <AvField id="e-kyc-contactAddress" data-cy="contactAddress" type="text" name="contactAddress" />
              </AvGroup>
              <AvGroup>
                <Label id="emailLabel" for="e-kyc-email">
                  <Translate contentKey="eKycAdminApp.customEKyc.email">Email</Translate>
                </Label>
                <AvField
                  id="e-kyc-email"
                  data-cy="email"
                  type="text"
                  name="email"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="referrerIdNameLabel" for="e-kyc-referrerIdName">
                  <Translate contentKey="eKycAdminApp.customEKyc.referrerIdName">Referrer Id Name</Translate>
                </Label>
                <AvField id="e-kyc-referrerIdName" data-cy="referrerIdName" type="text" name="referrerIdName" />
              </AvGroup>
              <AvGroup>
                <Label id="referrerBranchLabel" for="e-kyc-referrerBranch">
                  <Translate contentKey="eKycAdminApp.customEKyc.referrerBranch">Referrer Branch</Translate>
                </Label>
                <AvField id="e-kyc-referrerBranch" data-cy="referrerBranch" type="text" name="referrerBranch" />
              </AvGroup>
              <AvGroup>
                <Label id="bankAccountLabel" for="e-kyc-bankAccount">
                  <Translate contentKey="eKycAdminApp.customEKyc.bankAccount">Bank Account</Translate>
                </Label>
                <AvField id="e-kyc-bankAccount" data-cy="bankAccount" type="text" name="bankAccount" />
              </AvGroup>
              <AvGroup>
                <Label id="accountNameLabel" for="e-kyc-accountName">
                  <Translate contentKey="eKycAdminApp.customEKyc.accountName">Account Name</Translate>
                </Label>
                <AvField id="e-kyc-accountName" data-cy="accountName" type="text" name="accountName" />
              </AvGroup>
              <AvGroup>
                <Label id="bankNameLabel" for="e-kyc-bankName">
                  <Translate contentKey="eKycAdminApp.customEKyc.bankName">Bank Name</Translate>
                </Label>
                <AvField id="e-kyc-bankName" data-cy="bankName" type="text" name="bankName" />
              </AvGroup>
              <AvGroup>
                <Label id="branchLabel" for="e-kyc-branch">
                  <Translate contentKey="eKycAdminApp.customEKyc.branch">Branch</Translate>
                </Label>
                <AvField id="e-kyc-branch" data-cy="branch" type="text" name="branch" />
              </AvGroup>
              <AvGroup>
                <Label id="nationalityLabel" for="e-kyc-nationality">
                  <Translate contentKey="eKycAdminApp.customEKyc.nationality">Nationality</Translate>
                </Label>
                <AvField id="e-kyc-nationality" data-cy="nationality" type="text" name="nationality" />
              </AvGroup>
              <AvGroup>
                <Label id="statusLabel" for="e-kyc-status">
                  <Translate contentKey="eKycAdminApp.customEKyc.status">Status</Translate>
                </Label>
                <AvInput
                  id="e-kyc-status"
                  data-cy="status"
                  type="select"
                  className="form-control"
                  name="status"
                  value={(!isNew && eKycEntity.status) || 'PENDING'}
                >
                  <option value="PENDING">{translate('eKycAdminApp.Status.PENDING')}</option>
                  <option value="REJECT">{translate('eKycAdminApp.Status.REJECT')}</option>
                  <option value="APPROVED">{translate('eKycAdminApp.Status.APPROVED')}</option>
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label id="frontImageUrlLabel" for="e-kyc-frontImageUrl">
                  <Translate contentKey="eKycAdminApp.customEKyc.frontImageUrl">Front Image Url</Translate>
                </Label>
                <AvField
                  id="e-kyc-frontImageUrl"
                  data-cy="frontImageUrl"
                  type="text"
                  name="frontImageUrl"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="backImageUrlLabel" for="e-kyc-backImageUrl">
                  <Translate contentKey="eKycAdminApp.customEKyc.backImageUrl">Back Image Url</Translate>
                </Label>
                <AvField
                  id="e-kyc-backImageUrl"
                  data-cy="backImageUrl"
                  type="text"
                  name="backImageUrl"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="portraitImageUrlLabel" for="e-kyc-portraitImageUrl">
                  <Translate contentKey="eKycAdminApp.customEKyc.portraitImageUrl">Portrait Image Url</Translate>
                </Label>
                <AvField id="e-kyc-portraitImageUrl" data-cy="portraitImageUrl" type="text" name="portraitImageUrl" />
              </AvGroup>
              <AvGroup>
                <Label id="signatureImageUrlLabel" for="e-kyc-signatureImageUrl">
                  <Translate contentKey="eKycAdminApp.customEKyc.signatureImageUrl">Signature Image Url</Translate>
                </Label>
                <AvField id="e-kyc-signatureImageUrl" data-cy="signatureImageUrl" type="text" name="signatureImageUrl" />
              </AvGroup>
              <AvGroup>
                <Label id="tradingCodeImageUrlLabel" for="e-kyc-tradingCodeImageUrl">
                  <Translate contentKey="eKycAdminApp.customEKyc.tradingCodeImageUrl">Trading Code Image Url</Translate>
                </Label>
                <AvField id="e-kyc-tradingCodeImageUrl" data-cy="tradingCodeImageUrl" type="text" name="tradingCodeImageUrl" />
              </AvGroup>
              <AvGroup check>
                <Label id="isMarginLabel">
                  <AvInput id="e-kyc-isMargin" data-cy="isMargin" type="checkbox" className="form-check-input" name="isMargin" />
                  <Translate contentKey="eKycAdminApp.customEKyc.isMargin">Is Margin</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="matchingRateLabel" for="e-kyc-matchingRate">
                  <Translate contentKey="eKycAdminApp.customEKyc.matchingRate">Matching Rate</Translate>
                </Label>
                <AvField id="e-kyc-matchingRate" data-cy="matchingRate" type="string" className="form-control" name="matchingRate" />
              </AvGroup>
              <AvGroup>
                <Label id="updatedAtLabel" for="e-kyc-updatedAt">
                  <Translate contentKey="eKycAdminApp.customEKyc.updatedAt">Updated At</Translate>
                </Label>
                <AvInput
                  id="e-kyc-updatedAt"
                  data-cy="updatedAt"
                  type="datetime-local"
                  className="form-control"
                  name="updatedAt"
                  placeholder={'YYYY-MM-DD HH:mm'}
                  value={isNew ? displayDefaultDateTime() : convertDateTimeFromServer(props.eKycEntity.updatedAt)}
                />
              </AvGroup>
              <AvGroup>
                <Label id="createdAtLabel" for="e-kyc-createdAt">
                  <Translate contentKey="eKycAdminApp.customEKyc.createdAt">Created At</Translate>
                </Label>
                <AvInput
                  id="e-kyc-createdAt"
                  data-cy="createdAt"
                  type="datetime-local"
                  className="form-control"
                  name="createdAt"
                  placeholder={'YYYY-MM-DD HH:mm'}
                  value={isNew ? displayDefaultDateTime() : convertDateTimeFromServer(props.eKycEntity.createdAt)}
                />
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/e-kyc" replace color="info">
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
  eKycEntity: storeState.eKyc.entity,
  loading: storeState.eKyc.loading,
  updating: storeState.eKyc.updating,
  updateSuccess: storeState.eKyc.updateSuccess,
});

const mapDispatchToProps = {
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycUpdate);
