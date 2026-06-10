import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { getEntity, updateEntity, createEntity, reset } from './e-kyc.reducer';
import { IEKyc } from 'app/shared/model/e-kyc.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IEKycUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycUpdate = (props: IEKycUpdateProps) => {
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { eKycEntity, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/e-kyc');
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
          <h2 id="eKycAdminApp.eKyc.home.createOrEditLabel" data-cy="EKycCreateUpdateHeading">
            <Translate contentKey="eKycAdminApp.eKyc.home.createOrEditLabel">Create or edit a EKyc</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.identifierId">Identifier Id</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.fullName">Full Name</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.phoneNo">Phone No</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.gender">Gender</Translate>
                </Label>
                <AvField id="e-kyc-gender" data-cy="gender" type="text" name="gender" />
              </AvGroup>
              <AvGroup>
                <Label id="typeLabel" for="e-kyc-type">
                  <Translate contentKey="eKycAdminApp.eKyc.type">Type</Translate>
                </Label>
                <AvInput
                  id="e-kyc-type"
                  data-cy="type"
                  type="select"
                  className="form-control"
                  name="type"
                  value={(!isNew && eKycEntity.type) || 'CMND'}
                >
                  <option value="CMND">{translate('eKycAdminApp.EkycType.CMND')}</option>
                  <option value="CC">{translate('eKycAdminApp.EkycType.CC')}</option>
                  <option value="PASSPORT">{translate('eKycAdminApp.EkycType.PASSPORT')}</option>
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label id="birthDayLabel" for="e-kyc-birthDay">
                  <Translate contentKey="eKycAdminApp.eKyc.birthDay">Birth Day</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.expiredDate">Expired Date</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.issueDate">Issue Date</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.issuePlace">Issue Place</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.address">Address</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.occupation">Occupation</Translate>
                </Label>
                <AvField id="e-kyc-occupation" data-cy="occupation" type="text" name="occupation" />
              </AvGroup>
              <AvGroup>
                <Label id="homeTownLabel" for="e-kyc-homeTown">
                  <Translate contentKey="eKycAdminApp.eKyc.homeTown">Home Town</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.permanentProvince">Permanent Province</Translate>
                </Label>
                <AvField id="e-kyc-permanentProvince" data-cy="permanentProvince" type="text" name="permanentProvince" />
              </AvGroup>
              <AvGroup>
                <Label id="permanentDistrictLabel" for="e-kyc-permanentDistrict">
                  <Translate contentKey="eKycAdminApp.eKyc.permanentDistrict">Permanent District</Translate>
                </Label>
                <AvField id="e-kyc-permanentDistrict" data-cy="permanentDistrict" type="text" name="permanentDistrict" />
              </AvGroup>
              <AvGroup>
                <Label id="permanentAddressLabel" for="e-kyc-permanentAddress">
                  <Translate contentKey="eKycAdminApp.eKyc.permanentAddress">Permanent Address</Translate>
                </Label>
                <AvField id="e-kyc-permanentAddress" data-cy="permanentAddress" type="text" name="permanentAddress" />
              </AvGroup>
              <AvGroup>
                <Label id="contactProvinceLabel" for="e-kyc-contactProvince">
                  <Translate contentKey="eKycAdminApp.eKyc.contactProvince">Contact Province</Translate>
                </Label>
                <AvField id="e-kyc-contactProvince" data-cy="contactProvince" type="text" name="contactProvince" />
              </AvGroup>
              <AvGroup>
                <Label id="contactDistrictLabel" for="e-kyc-contactDistrict">
                  <Translate contentKey="eKycAdminApp.eKyc.contactDistrict">Contact District</Translate>
                </Label>
                <AvField id="e-kyc-contactDistrict" data-cy="contactDistrict" type="text" name="contactDistrict" />
              </AvGroup>
              <AvGroup>
                <Label id="contactAddressLabel" for="e-kyc-contactAddress">
                  <Translate contentKey="eKycAdminApp.eKyc.contactAddress">Contact Address</Translate>
                </Label>
                <AvField id="e-kyc-contactAddress" data-cy="contactAddress" type="text" name="contactAddress" />
              </AvGroup>
              <AvGroup>
                <Label id="emailLabel" for="e-kyc-email">
                  <Translate contentKey="eKycAdminApp.eKyc.email">Email</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.referrerIdName">Referrer Id Name</Translate>
                </Label>
                <AvField id="e-kyc-referrerIdName" data-cy="referrerIdName" type="text" name="referrerIdName" />
              </AvGroup>
              <AvGroup>
                <Label id="referrerBranchLabel" for="e-kyc-referrerBranch">
                  <Translate contentKey="eKycAdminApp.eKyc.referrerBranch">Referrer Branch</Translate>
                </Label>
                <AvField id="e-kyc-referrerBranch" data-cy="referrerBranch" type="text" name="referrerBranch" />
              </AvGroup>
              <AvGroup>
                <Label id="bankAccountLabel" for="e-kyc-bankAccount">
                  <Translate contentKey="eKycAdminApp.eKyc.bankAccount">Bank Account</Translate>
                </Label>
                <AvField id="e-kyc-bankAccount" data-cy="bankAccount" type="text" name="bankAccount" />
              </AvGroup>
              <AvGroup>
                <Label id="accountNameLabel" for="e-kyc-accountName">
                  <Translate contentKey="eKycAdminApp.eKyc.accountName">Account Name</Translate>
                </Label>
                <AvField id="e-kyc-accountName" data-cy="accountName" type="text" name="accountName" />
              </AvGroup>
              <AvGroup>
                <Label id="bankNameLabel" for="e-kyc-bankName">
                  <Translate contentKey="eKycAdminApp.eKyc.bankName">Bank Name</Translate>
                </Label>
                <AvField id="e-kyc-bankName" data-cy="bankName" type="text" name="bankName" />
              </AvGroup>
              <AvGroup>
                <Label id="branchLabel" for="e-kyc-branch">
                  <Translate contentKey="eKycAdminApp.eKyc.branch">Branch</Translate>
                </Label>
                <AvField id="e-kyc-branch" data-cy="branch" type="text" name="branch" />
              </AvGroup>
              <AvGroup>
                <Label id="nationalityLabel" for="e-kyc-nationality">
                  <Translate contentKey="eKycAdminApp.eKyc.nationality">Nationality</Translate>
                </Label>
                <AvField id="e-kyc-nationality" data-cy="nationality" type="text" name="nationality" />
              </AvGroup>
              <AvGroup>
                <Label id="statusLabel" for="e-kyc-status">
                  <Translate contentKey="eKycAdminApp.eKyc.status">Status</Translate>
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
                  <option value="AUTO_APPROVED">{translate('eKycAdminApp.Status.AUTO_APPROVED')}</option>
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label id="frontImageUrlLabel" for="e-kyc-frontImageUrl">
                  <Translate contentKey="eKycAdminApp.eKyc.frontImageUrl">Front Image Url</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.backImageUrl">Back Image Url</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.portraitImageUrl">Portrait Image Url</Translate>
                </Label>
                <AvField id="e-kyc-portraitImageUrl" data-cy="portraitImageUrl" type="text" name="portraitImageUrl" />
              </AvGroup>
              <AvGroup>
                <Label id="signatureImageUrlLabel" for="e-kyc-signatureImageUrl">
                  <Translate contentKey="eKycAdminApp.eKyc.signatureImageUrl">Signature Image Url</Translate>
                </Label>
                <AvField id="e-kyc-signatureImageUrl" data-cy="signatureImageUrl" type="text" name="signatureImageUrl" />
              </AvGroup>
              <AvGroup>
                <Label id="tradingCodeImageUrlLabel" for="e-kyc-tradingCodeImageUrl">
                  <Translate contentKey="eKycAdminApp.eKyc.tradingCodeImageUrl">Trading Code Image Url</Translate>
                </Label>
                <AvField id="e-kyc-tradingCodeImageUrl" data-cy="tradingCodeImageUrl" type="text" name="tradingCodeImageUrl" />
              </AvGroup>
              <AvGroup check>
                <Label id="isMarginLabel">
                  <AvInput id="e-kyc-isMargin" data-cy="isMargin" type="checkbox" className="form-check-input" name="isMargin" />
                  <Translate contentKey="eKycAdminApp.eKyc.isMargin">Is Margin</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="matchingRateLabel" for="e-kyc-matchingRate">
                  <Translate contentKey="eKycAdminApp.eKyc.matchingRate">Matching Rate</Translate>
                </Label>
                <AvField id="e-kyc-matchingRate" data-cy="matchingRate" type="string" className="form-control" name="matchingRate" />
              </AvGroup>
              <AvGroup>
                <Label id="updatedAtLabel" for="e-kyc-updatedAt">
                  <Translate contentKey="eKycAdminApp.eKyc.updatedAt">Updated At</Translate>
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
                  <Translate contentKey="eKycAdminApp.eKyc.createdAt">Created At</Translate>
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
              <AvGroup>
                <Label id="branchIdLabel" for="e-kyc-branchId">
                  <Translate contentKey="eKycAdminApp.eKyc.branchId">Branch Id</Translate>
                </Label>
                <AvField id="e-kyc-branchId" data-cy="branchId" type="text" name="branchId" />
              </AvGroup>
              <AvGroup>
                <Label id="channelIdLabel" for="e-kyc-channelId">
                  <Translate contentKey="eKycAdminApp.eKyc.channelId">Channel Id</Translate>
                </Label>
                <AvField id="e-kyc-channelId" data-cy="channelId" type="text" name="channelId" />
              </AvGroup>
              <AvGroup>
                <Label id="eKycIdLabel" for="e-kyc-eKycId">
                  <Translate contentKey="eKycAdminApp.eKyc.eKycId">E Kyc Id</Translate>
                </Label>
                <AvField id="e-kyc-eKycId" data-cy="eKycId" type="text" name="eKycId" />
              </AvGroup>
              <AvGroup>
                <Label id="taxNumberLabel" for="e-kyc-taxNumber">
                  <Translate contentKey="eKycAdminApp.eKyc.taxNumber">Tax Number</Translate>
                </Label>
                <AvField id="e-kyc-taxNumber" data-cy="taxNumber" type="text" name="taxNumber" />
              </AvGroup>
              <AvGroup check>
                <Label id="onlineTradingLabel">
                  <AvInput
                    id="e-kyc-onlineTrading"
                    data-cy="onlineTrading"
                    type="checkbox"
                    className="form-check-input"
                    name="onlineTrading"
                  />
                  <Translate contentKey="eKycAdminApp.eKyc.onlineTrading">Online Trading</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="authenMethodLabel" for="e-kyc-authenMethod">
                  <Translate contentKey="eKycAdminApp.eKyc.authenMethod">Authen Method</Translate>
                </Label>
                <AvField id="e-kyc-authenMethod" data-cy="authenMethod" type="text" name="authenMethod" />
              </AvGroup>
              <AvGroup>
                <Label id="otpReceiveMethodLabel" for="e-kyc-otpReceiveMethod">
                  <Translate contentKey="eKycAdminApp.eKyc.otpReceiveMethod">Otp Receive Method</Translate>
                </Label>
                <AvField id="e-kyc-otpReceiveMethod" data-cy="otpReceiveMethod" type="text" name="otpReceiveMethod" />
              </AvGroup>
              <AvGroup check>
                <Label id="advancedCashIncludedLabel">
                  <AvInput
                    id="e-kyc-advancedCashIncluded"
                    data-cy="advancedCashIncluded"
                    type="checkbox"
                    className="form-check-input"
                    name="advancedCashIncluded"
                  />
                  <Translate contentKey="eKycAdminApp.eKyc.advancedCashIncluded">Advanced Cash Included</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="smsMethodLabel" for="e-kyc-smsMethod">
                  <Translate contentKey="eKycAdminApp.eKyc.smsMethod">Sms Method</Translate>
                </Label>
                <AvField id="e-kyc-smsMethod" data-cy="smsMethod" type="text" name="smsMethod" />
              </AvGroup>
              <AvGroup check>
                <Label id="emailNotificationLabel">
                  <AvInput
                    id="e-kyc-emailNotification"
                    data-cy="emailNotification"
                    type="checkbox"
                    className="form-check-input"
                    name="emailNotification"
                  />
                  <Translate contentKey="eKycAdminApp.eKyc.emailNotification">Email Notification</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="referralLabel" for="e-kyc-referral">
                  <Translate contentKey="eKycAdminApp.eKyc.referral">Referral</Translate>
                </Label>
                <AvField id="e-kyc-referral" data-cy="referral" type="text" name="referral" />
              </AvGroup>
              <AvGroup>
                <Label id="partnerIdLabel" for="e-kyc-partnerId">
                  <Translate contentKey="eKycAdminApp.eKyc.partnerId">Partner Id</Translate>
                </Label>
                <AvField id="e-kyc-partnerId" data-cy="partnerId" type="text" name="partnerId" />
              </AvGroup>
              <AvGroup>
                <Label id="partnerNameLabel" for="e-kyc-partnerName">
                  <Translate contentKey="eKycAdminApp.eKyc.partnerName">Partner Name</Translate>
                </Label>
                <AvField id="e-kyc-partnerName" data-cy="partnerName" type="text" name="partnerName" />
              </AvGroup>
              <AvGroup check>
                <Label id="customerSupportLabel">
                  <AvInput
                    id="e-kyc-customerSupport"
                    data-cy="customerSupport"
                    type="checkbox"
                    className="form-check-input"
                    name="customerSupport"
                  />
                  <Translate contentKey="eKycAdminApp.eKyc.customerSupport">Customer Support</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="csPartnerIdLabel" for="e-kyc-csPartnerId">
                  <Translate contentKey="eKycAdminApp.eKyc.csPartnerId">Cs Partner Id</Translate>
                </Label>
                <AvField id="e-kyc-csPartnerId" data-cy="csPartnerId" type="text" name="csPartnerId" />
              </AvGroup>
              <AvGroup>
                <Label id="csNameLabel" for="e-kyc-csName">
                  <Translate contentKey="eKycAdminApp.eKyc.csName">Cs Name</Translate>
                </Label>
                <AvField id="e-kyc-csName" data-cy="csName" type="text" name="csName" />
              </AvGroup>
              <AvGroup>
                <Label id="contractIdLabel" for="e-kyc-contractId">
                  <Translate contentKey="eKycAdminApp.eKyc.contractId">Contract Id</Translate>
                </Label>
                <AvField id="e-kyc-contractId" data-cy="contractId" type="text" name="contractId" />
              </AvGroup>
              <AvGroup>
                <Label id="contractStatusLabel" for="e-kyc-contractStatus">
                  <Translate contentKey="eKycAdminApp.eKyc.contractStatus">Contract Status</Translate>
                </Label>
                <AvField id="e-kyc-contractStatus" data-cy="contractStatus" type="text" name="contractStatus" />
              </AvGroup>
              <AvGroup check>
                <Label id="fatcaLabel">
                  <AvInput id="e-kyc-fatca" data-cy="fatca" type="checkbox" className="form-check-input" name="fatca" />
                  <Translate contentKey="eKycAdminApp.eKyc.fatca">Fatca</Translate>
                </Label>
              </AvGroup>
              <AvGroup>
                <Label id="contractNoLabel" for="e-kyc-contractNo">
                  <Translate contentKey="eKycAdminApp.eKyc.contractNo">Contract No</Translate>
                </Label>
                <AvField
                  id="e-kyc-contractNo"
                  data-cy="contractNo"
                  type="text"
                  name="contractNo"
                  validate={{
                    maxLength: { value: 255, errorMessage: translate('entity.validation.maxlength', { max: 255 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="accountNumberLabel" for="e-kyc-accountNumber">
                  <Translate contentKey="eKycAdminApp.eKyc.accountNumber">Account Number</Translate>
                </Label>
                <AvField
                  id="e-kyc-accountNumber"
                  data-cy="accountNumber"
                  type="text"
                  name="accountNumber"
                  validate={{
                    maxLength: { value: 255, errorMessage: translate('entity.validation.maxlength', { max: 255 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="ocrLogIdLabel" for="e-kyc-ocrLogId">
                  <Translate contentKey="eKycAdminApp.eKyc.ocrLogId">Ocr Log Id</Translate>
                </Label>
                <AvField id="e-kyc-ocrLogId" data-cy="ocrLogId" type="text" name="ocrLogId" />
              </AvGroup>
              <AvGroup>
                <Label id="cardLivenessLogIdLabel" for="e-kyc-cardLivenessLogId">
                  <Translate contentKey="eKycAdminApp.eKyc.cardLivenessLogId">Card Liveness Log Id</Translate>
                </Label>
                <AvField id="e-kyc-cardLivenessLogId" data-cy="cardLivenessLogId" type="text" name="cardLivenessLogId" />
              </AvGroup>
              <AvGroup>
                <Label id="cardRearLogIdLabel" for="e-kyc-cardRearLogId">
                  <Translate contentKey="eKycAdminApp.eKyc.cardRearLogId">Card Rear Log Id</Translate>
                </Label>
                <AvField id="e-kyc-cardRearLogId" data-cy="cardRearLogId" type="text" name="cardRearLogId" />
              </AvGroup>
              <AvGroup>
                <Label id="compareLogIdLabel" for="e-kyc-compareLogId">
                  <Translate contentKey="eKycAdminApp.eKyc.compareLogId">Compare Log Id</Translate>
                </Label>
                <AvField id="e-kyc-compareLogId" data-cy="compareLogId" type="text" name="compareLogId" />
              </AvGroup>
              <AvGroup>
                <Label id="faceLivenessLogIdLabel" for="e-kyc-faceLivenessLogId">
                  <Translate contentKey="eKycAdminApp.eKyc.faceLivenessLogId">Face Liveness Log Id</Translate>
                </Label>
                <AvField id="e-kyc-faceLivenessLogId" data-cy="faceLivenessLogId" type="text" name="faceLivenessLogId" />
              </AvGroup>
              <AvGroup>
                <Label id="faceMaskLogIdLabel" for="e-kyc-faceMaskLogId">
                  <Translate contentKey="eKycAdminApp.eKyc.faceMaskLogId">Face Mask Log Id</Translate>
                </Label>
                <AvField id="e-kyc-faceMaskLogId" data-cy="faceMaskLogId" type="text" name="faceMaskLogId" />
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
