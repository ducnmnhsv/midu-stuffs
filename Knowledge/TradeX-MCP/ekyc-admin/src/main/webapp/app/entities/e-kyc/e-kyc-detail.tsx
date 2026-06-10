import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './e-kyc.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycDetail = (props: IEKycDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { eKycEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eKycDetailsHeading">
          <Translate contentKey="eKycAdminApp.eKyc.detail.title">EKyc</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.id}</dd>
          <dt>
            <span id="identifierId">
              <Translate contentKey="eKycAdminApp.eKyc.identifierId">Identifier Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.identifierId}</dd>
          <dt>
            <span id="fullName">
              <Translate contentKey="eKycAdminApp.eKyc.fullName">Full Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.fullName}</dd>
          <dt>
            <span id="phoneNo">
              <Translate contentKey="eKycAdminApp.eKyc.phoneNo">Phone No</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.phoneNo}</dd>
          <dt>
            <span id="gender">
              <Translate contentKey="eKycAdminApp.eKyc.gender">Gender</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.gender}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="eKycAdminApp.eKyc.type">Type</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.type}</dd>
          <dt>
            <span id="birthDay">
              <Translate contentKey="eKycAdminApp.eKyc.birthDay">Birth Day</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.birthDay ? <TextFormat value={eKycEntity.birthDay} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="expiredDate">
              <Translate contentKey="eKycAdminApp.eKyc.expiredDate">Expired Date</Translate>
            </span>
          </dt>
          <dd>
            {eKycEntity.expiredDate ? <TextFormat value={eKycEntity.expiredDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="issueDate">
              <Translate contentKey="eKycAdminApp.eKyc.issueDate">Issue Date</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.issueDate ? <TextFormat value={eKycEntity.issueDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="issuePlace">
              <Translate contentKey="eKycAdminApp.eKyc.issuePlace">Issue Place</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.issuePlace}</dd>
          <dt>
            <span id="address">
              <Translate contentKey="eKycAdminApp.eKyc.address">Address</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.address}</dd>
          <dt>
            <span id="occupation">
              <Translate contentKey="eKycAdminApp.eKyc.occupation">Occupation</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.occupation}</dd>
          <dt>
            <span id="homeTown">
              <Translate contentKey="eKycAdminApp.eKyc.homeTown">Home Town</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.homeTown}</dd>
          <dt>
            <span id="permanentProvince">
              <Translate contentKey="eKycAdminApp.eKyc.permanentProvince">Permanent Province</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.permanentProvince}</dd>
          <dt>
            <span id="permanentDistrict">
              <Translate contentKey="eKycAdminApp.eKyc.permanentDistrict">Permanent District</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.permanentDistrict}</dd>
          <dt>
            <span id="permanentAddress">
              <Translate contentKey="eKycAdminApp.eKyc.permanentAddress">Permanent Address</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.permanentAddress}</dd>
          <dt>
            <span id="contactProvince">
              <Translate contentKey="eKycAdminApp.eKyc.contactProvince">Contact Province</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contactProvince}</dd>
          <dt>
            <span id="contactDistrict">
              <Translate contentKey="eKycAdminApp.eKyc.contactDistrict">Contact District</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contactDistrict}</dd>
          <dt>
            <span id="contactAddress">
              <Translate contentKey="eKycAdminApp.eKyc.contactAddress">Contact Address</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contactAddress}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="eKycAdminApp.eKyc.email">Email</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.email}</dd>
          <dt>
            <span id="referrerIdName">
              <Translate contentKey="eKycAdminApp.eKyc.referrerIdName">Referrer Id Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.referrerIdName}</dd>
          <dt>
            <span id="referrerBranch">
              <Translate contentKey="eKycAdminApp.eKyc.referrerBranch">Referrer Branch</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.referrerBranch}</dd>
          <dt>
            <span id="bankAccount">
              <Translate contentKey="eKycAdminApp.eKyc.bankAccount">Bank Account</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.bankAccount}</dd>
          <dt>
            <span id="accountName">
              <Translate contentKey="eKycAdminApp.eKyc.accountName">Account Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.accountName}</dd>
          <dt>
            <span id="bankName">
              <Translate contentKey="eKycAdminApp.eKyc.bankName">Bank Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.bankName}</dd>
          <dt>
            <span id="branch">
              <Translate contentKey="eKycAdminApp.eKyc.branch">Branch</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.branch}</dd>
          <dt>
            <span id="nationality">
              <Translate contentKey="eKycAdminApp.eKyc.nationality">Nationality</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.nationality}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="eKycAdminApp.eKyc.status">Status</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.status}</dd>
          <dt>
            <span id="frontImageUrl">
              <Translate contentKey="eKycAdminApp.eKyc.frontImageUrl">Front Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.frontImageUrl}</dd>
          <dt>
            <span id="backImageUrl">
              <Translate contentKey="eKycAdminApp.eKyc.backImageUrl">Back Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.backImageUrl}</dd>
          <dt>
            <span id="portraitImageUrl">
              <Translate contentKey="eKycAdminApp.eKyc.portraitImageUrl">Portrait Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.portraitImageUrl}</dd>
          <dt>
            <span id="signatureImageUrl">
              <Translate contentKey="eKycAdminApp.eKyc.signatureImageUrl">Signature Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.signatureImageUrl}</dd>
          <dt>
            <span id="tradingCodeImageUrl">
              <Translate contentKey="eKycAdminApp.eKyc.tradingCodeImageUrl">Trading Code Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.tradingCodeImageUrl}</dd>
          <dt>
            <span id="isMargin">
              <Translate contentKey="eKycAdminApp.eKyc.isMargin">Is Margin</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.isMargin ? 'true' : 'false'}</dd>
          <dt>
            <span id="matchingRate">
              <Translate contentKey="eKycAdminApp.eKyc.matchingRate">Matching Rate</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.matchingRate}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="eKycAdminApp.eKyc.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.updatedAt ? <TextFormat value={eKycEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="eKycAdminApp.eKyc.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.createdAt ? <TextFormat value={eKycEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="branchId">
              <Translate contentKey="eKycAdminApp.eKyc.branchId">Branch Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.branchId}</dd>
          <dt>
            <span id="channelId">
              <Translate contentKey="eKycAdminApp.eKyc.channelId">Channel Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.channelId}</dd>
          <dt>
            <span id="eKycId">
              <Translate contentKey="eKycAdminApp.eKyc.eKycId">E Kyc Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.eKycId}</dd>
          <dt>
            <span id="taxNumber">
              <Translate contentKey="eKycAdminApp.eKyc.taxNumber">Tax Number</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.taxNumber}</dd>
          <dt>
            <span id="onlineTrading">
              <Translate contentKey="eKycAdminApp.eKyc.onlineTrading">Online Trading</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.onlineTrading ? 'true' : 'false'}</dd>
          <dt>
            <span id="authenMethod">
              <Translate contentKey="eKycAdminApp.eKyc.authenMethod">Authen Method</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.authenMethod}</dd>
          <dt>
            <span id="otpReceiveMethod">
              <Translate contentKey="eKycAdminApp.eKyc.otpReceiveMethod">Otp Receive Method</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.otpReceiveMethod}</dd>
          <dt>
            <span id="advancedCashIncluded">
              <Translate contentKey="eKycAdminApp.eKyc.advancedCashIncluded">Advanced Cash Included</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.advancedCashIncluded ? 'true' : 'false'}</dd>
          <dt>
            <span id="smsMethod">
              <Translate contentKey="eKycAdminApp.eKyc.smsMethod">Sms Method</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.smsMethod}</dd>
          <dt>
            <span id="emailNotification">
              <Translate contentKey="eKycAdminApp.eKyc.emailNotification">Email Notification</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.emailNotification ? 'true' : 'false'}</dd>
          <dt>
            <span id="referral">
              <Translate contentKey="eKycAdminApp.eKyc.referral">Referral</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.referral}</dd>
          <dt>
            <span id="partnerId">
              <Translate contentKey="eKycAdminApp.eKyc.partnerId">Partner Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.partnerId}</dd>
          <dt>
            <span id="partnerName">
              <Translate contentKey="eKycAdminApp.eKyc.partnerName">Partner Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.partnerName}</dd>
          <dt>
            <span id="customerSupport">
              <Translate contentKey="eKycAdminApp.eKyc.customerSupport">Customer Support</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.customerSupport ? 'true' : 'false'}</dd>
          <dt>
            <span id="csPartnerId">
              <Translate contentKey="eKycAdminApp.eKyc.csPartnerId">Cs Partner Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.csPartnerId}</dd>
          <dt>
            <span id="csName">
              <Translate contentKey="eKycAdminApp.eKyc.csName">Cs Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.csName}</dd>
          <dt>
            <span id="contractId">
              <Translate contentKey="eKycAdminApp.eKyc.contractId">Contract Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contractId}</dd>
          <dt>
            <span id="contractStatus">
              <Translate contentKey="eKycAdminApp.eKyc.contractStatus">Contract Status</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contractStatus}</dd>
          <dt>
            <span id="fatca">
              <Translate contentKey="eKycAdminApp.eKyc.fatca">Fatca</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.fatca ? 'true' : 'false'}</dd>
          <dt>
            <span id="contractNo">
              <Translate contentKey="eKycAdminApp.eKyc.contractNo">Contract No</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contractNo}</dd>
          <dt>
            <span id="accountNumber">
              <Translate contentKey="eKycAdminApp.eKyc.accountNumber">Account Number</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.accountNumber}</dd>
          <dt>
            <span id="ocrLogId">
              <Translate contentKey="eKycAdminApp.eKyc.ocrLogId">Ocr Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.ocrLogId}</dd>
          <dt>
            <span id="cardLivenessLogId">
              <Translate contentKey="eKycAdminApp.eKyc.cardLivenessLogId">Card Liveness Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.cardLivenessLogId}</dd>
          <dt>
            <span id="cardRearLogId">
              <Translate contentKey="eKycAdminApp.eKyc.cardRearLogId">Card Rear Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.cardRearLogId}</dd>
          <dt>
            <span id="compareLogId">
              <Translate contentKey="eKycAdminApp.eKyc.compareLogId">Compare Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.compareLogId}</dd>
          <dt>
            <span id="faceLivenessLogId">
              <Translate contentKey="eKycAdminApp.eKyc.faceLivenessLogId">Face Liveness Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.faceLivenessLogId}</dd>
          <dt>
            <span id="faceMaskLogId">
              <Translate contentKey="eKycAdminApp.eKyc.faceMaskLogId">Face Mask Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.faceMaskLogId}</dd>
        </dl>
        <Button tag={Link} to="/e-kyc" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/e-kyc/${eKycEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ eKyc }: IRootState) => ({
  eKycEntity: eKyc.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycDetail);
