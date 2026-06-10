import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './e-kyc-additional-info.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycAdditionalInfoDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycAdditionalInfoDetail = (props: IEKycAdditionalInfoDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { eKycAdditionalInfoEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eKycAdditionalInfoDetailsHeading">
          <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.detail.title">EKycAdditionalInfo</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.id}</dd>
          <dt>
            <span id="fullName">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.fullName">Full Name</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.fullName}</dd>
          <dt>
            <span id="birthDay">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.birthDay">Birth Day</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.birthDay}</dd>
          <dt>
            <span id="nationality">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.nationality">Nationality</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.nationality}</dd>
          <dt>
            <span id="identifierId">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.identifierId">Identifier Id</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.identifierId}</dd>
          <dt>
            <span id="issueDate">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.issueDate">Issue Date</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.issueDate}</dd>
          <dt>
            <span id="issuePlace">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.issuePlace">Issue Place</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.issuePlace}</dd>
          <dt>
            <span id="permanentAddress">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.permanentAddress">Permanent Address</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.permanentAddress}</dd>
          <dt>
            <span id="contactAddress">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.contactAddress">Contact Address</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.contactAddress}</dd>
          <dt>
            <span id="occupation">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.occupation">Occupation</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.occupation}</dd>
          <dt>
            <span id="position">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.position">Position</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.position}</dd>
          <dt>
            <span id="phoneNumber">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.phoneNumber">Phone Number</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.phoneNumber}</dd>
          <dt>
            <span id="visaNo">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.visaNo">Visa No</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.visaNo}</dd>
          <dt>
            <span id="visaIssuePlace">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.visaIssuePlace">Visa Issue Place</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.visaIssuePlace}</dd>
          <dt>
            <span id="foreignResidence">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.foreignResidence">Foreign Residence</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.foreignResidence}</dd>
          <dt>
            <span id="investmentGoal">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.investmentGoal">Investment Goal</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.investmentGoal}</dd>
          <dt>
            <span id="risk">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.risk">Risk</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.risk}</dd>
          <dt>
            <span id="experienced">
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.experienced">Experienced</Translate>
            </span>
          </dt>
          <dd>{eKycAdditionalInfoEntity.experienced ? 'true' : 'false'}</dd>
          <dt>
            <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.eKyc">E Kyc</Translate>
          </dt>
          <dd>{eKycAdditionalInfoEntity.eKyc ? eKycAdditionalInfoEntity.eKyc.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/e-kyc-additional-info" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/e-kyc-additional-info/${eKycAdditionalInfoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ eKycAdditionalInfo }: IRootState) => ({
  eKycAdditionalInfoEntity: eKycAdditionalInfo.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycAdditionalInfoDetail);
