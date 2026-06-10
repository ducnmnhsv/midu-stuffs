import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './e-kyc-bank-list.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycBankListDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycBankListDetail = (props: IEKycBankListDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { eKycBankListEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eKycBankListDetailsHeading">
          <Translate contentKey="eKycAdminApp.eKycBankList.detail.title">EKycBankList</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eKycBankListEntity.id}</dd>
          <dt>
            <span id="bankId">
              <Translate contentKey="eKycAdminApp.eKycBankList.bankId">Bank Id</Translate>
            </span>
          </dt>
          <dd>{eKycBankListEntity.bankId}</dd>
          <dt>
            <span id="bankName">
              <Translate contentKey="eKycAdminApp.eKycBankList.bankName">Bank Name</Translate>
            </span>
          </dt>
          <dd>{eKycBankListEntity.bankName}</dd>
          <dt>
            <span id="bankAccNo">
              <Translate contentKey="eKycAdminApp.eKycBankList.bankAccNo">Bank Acc No</Translate>
            </span>
          </dt>
          <dd>{eKycBankListEntity.bankAccNo}</dd>
          <dt>
            <span id="ownerName">
              <Translate contentKey="eKycAdminApp.eKycBankList.ownerName">Owner Name</Translate>
            </span>
          </dt>
          <dd>{eKycBankListEntity.ownerName}</dd>
          <dt>
            <span id="branchId">
              <Translate contentKey="eKycAdminApp.eKycBankList.branchId">Branch Id</Translate>
            </span>
          </dt>
          <dd>{eKycBankListEntity.branchId}</dd>
          <dt>
            <Translate contentKey="eKycAdminApp.eKycBankList.eKyc">E Kyc</Translate>
          </dt>
          <dd>{eKycBankListEntity.eKyc ? eKycBankListEntity.eKyc.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/e-kyc-bank-list" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/e-kyc-bank-list/${eKycBankListEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ eKycBankList }: IRootState) => ({
  eKycBankListEntity: eKycBankList.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycBankListDetail);
