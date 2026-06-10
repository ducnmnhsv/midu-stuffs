import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './e-kyc-ext.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycExtDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycExtDetail = (props: IEKycExtDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { eKycExtEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eKycExtDetailsHeading">
          <Translate contentKey="eKycAdminApp.eKycExt.detail.title">EKycExt</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eKycExtEntity.id}</dd>
          <dt>
            <span id="logId">
              <Translate contentKey="eKycAdminApp.eKycExt.logId">Log Id</Translate>
            </span>
          </dt>
          <dd>{eKycExtEntity.logId}</dd>
          <dt>
            <span id="rawData">
              <Translate contentKey="eKycAdminApp.eKycExt.rawData">Raw Data</Translate>
            </span>
          </dt>
          <dd>{eKycExtEntity.rawData}</dd>
          <dt>
            <Translate contentKey="eKycAdminApp.eKycExt.eKyc">E Kyc</Translate>
          </dt>
          <dd>{eKycExtEntity.eKyc ? eKycExtEntity.eKyc.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/e-kyc-ext" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/e-kyc-ext/${eKycExtEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ eKycExt }: IRootState) => ({
  eKycExtEntity: eKycExt.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycExtDetail);
