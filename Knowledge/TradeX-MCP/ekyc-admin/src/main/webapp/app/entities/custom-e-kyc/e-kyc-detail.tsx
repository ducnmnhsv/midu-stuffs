import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './e-kyc.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import axios from 'axios';

export interface IEKycDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycDetail = (props: IEKycDetailProps) => {
  const [creatorStatus, setCreatorStatus] = useState(null);

  useEffect(() => {
    props.getEntity(props.match.params.id);
    fetchCreatorStatus();
  }, []);

  const fetchCreatorStatus = async () => {
    await axios
      .get(`/api/e-kyc-creator-statuses`)
      .then(res => {
        const entityCreatorStatus = res.data.find(ele => ele.ekyc.id === Number(props.match.params.id)) || {};
        setCreatorStatus(entityCreatorStatus);
      })
      .catch(err => {
        console.log(err);
      });
  };

  const classOfMatchingRate = value => {
    let name = '';
    const valueOfMatchingRate = Number(value);
    if (valueOfMatchingRate > 90) {
      name = 'high';
    } else if (valueOfMatchingRate > 80 && valueOfMatchingRate <= 90) {
      name = 'little-high';
    } else if (valueOfMatchingRate > 70 && valueOfMatchingRate <= 80) {
      name = 'medium';
    } else {
      name = 'low';
    }
    return name;
  };

  const { eKycEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eKycDetailsHeading">
          <Translate contentKey="eKycAdminApp.customEKyc.detail.title">EKyc</Translate>
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
              <Translate contentKey="eKycAdminApp.customEKyc.identifierId">Identifier Id</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.identifierId}</dd>
          <dt>
            <span id="fullName">
              <Translate contentKey="eKycAdminApp.customEKyc.fullName">Full Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.fullName}</dd>
          <dt>
            <span id="phoneNo">
              <Translate contentKey="eKycAdminApp.customEKyc.phoneNo">Phone No</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.phoneNo}</dd>
          <dt>
            <span id="gender">
              <Translate contentKey="eKycAdminApp.customEKyc.gender">Gender</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.gender}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="eKycAdminApp.customEKyc.type">Type</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.type}</dd>
          <dt>
            <span id="birthDay">
              <Translate contentKey="eKycAdminApp.customEKyc.birthDay">Birth Day</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.birthDay ? <TextFormat value={eKycEntity.birthDay} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="expiredDate">
              <Translate contentKey="eKycAdminApp.customEKyc.expiredDate">Expired Date</Translate>
            </span>
          </dt>
          <dd>
            {eKycEntity.expiredDate ? <TextFormat value={eKycEntity.expiredDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="issueDate">
              <Translate contentKey="eKycAdminApp.customEKyc.issueDate">Issue Date</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.issueDate ? <TextFormat value={eKycEntity.issueDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="issuePlace">
              <Translate contentKey="eKycAdminApp.customEKyc.issuePlace">Issue Place</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.issuePlace}</dd>
          <dt>
            <span id="address">
              <Translate contentKey="eKycAdminApp.customEKyc.address">Address</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.address}</dd>
          <dt>
            <span id="occupation">
              <Translate contentKey="eKycAdminApp.customEKyc.occupation">Occupation</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.occupation}</dd>
          <dt>
            <span id="homeTown">
              <Translate contentKey="eKycAdminApp.customEKyc.homeTown">Home Town</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.homeTown}</dd>
          <dt>
            <span id="permanentProvince">
              <Translate contentKey="eKycAdminApp.customEKyc.permanentProvince">Permanent Province</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.permanentProvince}</dd>
          <dt>
            <span id="permanentDistrict">
              <Translate contentKey="eKycAdminApp.customEKyc.permanentDistrict">Permanent District</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.permanentDistrict}</dd>
          <dt>
            <span id="permanentAddress">
              <Translate contentKey="eKycAdminApp.customEKyc.permanentAddress">Permanent Address</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.permanentAddress}</dd>
          <dt>
            <span id="contactProvince">
              <Translate contentKey="eKycAdminApp.customEKyc.contactProvince">Contact Province</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contactProvince}</dd>
          <dt>
            <span id="contactDistrict">
              <Translate contentKey="eKycAdminApp.customEKyc.contactDistrict">Contact District</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contactDistrict}</dd>
          <dt>
            <span id="contactAddress">
              <Translate contentKey="eKycAdminApp.customEKyc.contactAddress">Contact Address</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.contactAddress}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="eKycAdminApp.customEKyc.email">Email</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.email}</dd>
          <dt>
            <span id="referrerIdName">
              <Translate contentKey="eKycAdminApp.customEKyc.referrerIdName">Referrer Id Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.referrerIdName}</dd>
          <dt>
            <span id="referrerBranch">
              <Translate contentKey="eKycAdminApp.customEKyc.referrerBranch">Referrer Branch</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.referrerBranch}</dd>
          <dt>
            <span id="bankAccount">
              <Translate contentKey="eKycAdminApp.customEKyc.bankAccount">Bank Account</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.bankAccount}</dd>
          <dt>
            <span id="accountName">
              <Translate contentKey="eKycAdminApp.customEKyc.accountName">Account Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.accountName}</dd>
          <dt>
            <span id="bankName">
              <Translate contentKey="eKycAdminApp.customEKyc.bankName">Bank Name</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.bankName}</dd>
          <dt>
            <span id="branch">
              <Translate contentKey="eKycAdminApp.customEKyc.branch">Branch</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.branch}</dd>
          <dt>
            <span id="nationality">
              <Translate contentKey="eKycAdminApp.customEKyc.nationality">Nationality</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.nationality}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="eKycAdminApp.customEKyc.status">Status</Translate>
            </span>
          </dt>
          <dd className={`status-${eKycEntity?.status?.toLowerCase()}`}>{eKycEntity.status}</dd>
          <dt>
            <span id="creatorStatus">
              <Translate contentKey="eKycAdminApp.customEKyc.creatorStatus">Creator Status</Translate>
            </span>
          </dt>
          <dd className={`status-${(creatorStatus?.status || '').toLowerCase()}`}>{creatorStatus?.status || ''}</dd>
          <dt>
            <span id="reason">
              <Translate contentKey="eKycAdminApp.customEKyc.reason">Reason</Translate>
            </span>
          </dt>
          <dd>{creatorStatus?.reason || ''}</dd>
          <dt>
            <span id="fullResult">
              <Translate contentKey="eKycAdminApp.customEKyc.fullResult">Full Result</Translate>
            </span>
          </dt>
          <dd>{creatorStatus?.fullResult || ''}</dd>
          <dt>
            <span id="frontImageUrl">
              <Translate contentKey="eKycAdminApp.customEKyc.frontImageUrl">Front Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.frontImageUrl}</dd>
          <dt>
            <span id="backImageUrl">
              <Translate contentKey="eKycAdminApp.customEKyc.backImageUrl">Back Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.backImageUrl}</dd>
          <dt>
            <span id="portraitImageUrl">
              <Translate contentKey="eKycAdminApp.customEKyc.portraitImageUrl">Portrait Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.portraitImageUrl}</dd>
          <dt>
            <span id="signatureImageUrl">
              <Translate contentKey="eKycAdminApp.customEKyc.signatureImageUrl">Signature Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.signatureImageUrl}</dd>
          <dt>
            <span id="tradingCodeImageUrl">
              <Translate contentKey="eKycAdminApp.customEKyc.tradingCodeImageUrl">Trading Code Image Url</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.tradingCodeImageUrl}</dd>
          <dt>
            <span id="isMargin">
              <Translate contentKey="eKycAdminApp.customEKyc.isMargin">Is Margin</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.isMargin ? 'true' : 'false'}</dd>
          <dt>
            <span id="matchingRate">
              <Translate contentKey="eKycAdminApp.customEKyc.matchingRate">Matching Rate</Translate>
            </span>
          </dt>
          <dd className={`matchingRate-${classOfMatchingRate(eKycEntity.matchingRate?.toFixed(2))}`}>{eKycEntity.matchingRate?.toFixed(2)}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="eKycAdminApp.customEKyc.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.updatedAt ? <TextFormat value={eKycEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="eKycAdminApp.customEKyc.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{eKycEntity.createdAt ? <TextFormat value={eKycEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
        </dl>
        <Button tag={Link} to="/" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/custom-e-kyc/${eKycEntity.id}/edit`} replace color="primary">
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
