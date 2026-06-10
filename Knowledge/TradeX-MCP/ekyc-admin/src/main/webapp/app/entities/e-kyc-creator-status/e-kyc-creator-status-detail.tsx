import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, byteSize, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './e-kyc-creator-status.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycCreatorStatusDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycCreatorStatusDetail = (props: IEKycCreatorStatusDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { eKycCreatorStatusEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eKycCreatorStatusDetailsHeading">
          <Translate contentKey="eKycAdminApp.eKycCreatorStatus.detail.title">EKycCreatorStatus</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eKycCreatorStatusEntity.id}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="eKycAdminApp.eKycCreatorStatus.status">Status</Translate>
            </span>
          </dt>
          <dd>{eKycCreatorStatusEntity.status}</dd>
          <dt>
            <span id="reason">
              <Translate contentKey="eKycAdminApp.eKycCreatorStatus.reason">Reason</Translate>
            </span>
          </dt>
          <dd>{eKycCreatorStatusEntity.reason}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="eKycAdminApp.eKycCreatorStatus.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>
            {eKycCreatorStatusEntity.updatedAt ? (
              <TextFormat value={eKycCreatorStatusEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedBy">
              <Translate contentKey="eKycAdminApp.eKycCreatorStatus.updatedBy">Updated By</Translate>
            </span>
          </dt>
          <dd>{eKycCreatorStatusEntity.updatedBy}</dd>
          <dt>
            <span id="fullResult">
              <Translate contentKey="eKycAdminApp.eKycCreatorStatus.fullResult">Full Result</Translate>
            </span>
          </dt>
          <dd>{eKycCreatorStatusEntity.fullResult}</dd>
          <dt>
            <Translate contentKey="eKycAdminApp.eKycCreatorStatus.eKyc">E Kyc</Translate>
          </dt>
          <dd>{eKycCreatorStatusEntity.eKyc ? eKycCreatorStatusEntity.eKyc.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/e-kyc-creator-status" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/e-kyc-creator-status/${eKycCreatorStatusEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ eKycCreatorStatus }: IRootState) => ({
  eKycCreatorStatusEntity: eKycCreatorStatus.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycCreatorStatusDetail);
