import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './ttl-issue-place-code-map.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface ITtlIssuePlaceCodeMapDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const TtlIssuePlaceCodeMapDetail = (props: ITtlIssuePlaceCodeMapDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { ttlIssuePlaceCodeMapEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="ttlIssuePlaceCodeMapDetailsHeading">
          <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.detail.title">TtlIssuePlaceCodeMap</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{ttlIssuePlaceCodeMapEntity.id}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.code">Code</Translate>
            </span>
          </dt>
          <dd>{ttlIssuePlaceCodeMapEntity.code}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.name">Name</Translate>
            </span>
          </dt>
          <dd>{ttlIssuePlaceCodeMapEntity.name}</dd>
          <dt>
            <span id="enableRegex">
              <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.enableRegex">Enable Regex</Translate>
            </span>
          </dt>
          <dd>{ttlIssuePlaceCodeMapEntity.enableRegex ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/ttl-issue-place-code-map" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/ttl-issue-place-code-map/${ttlIssuePlaceCodeMapEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ ttlIssuePlaceCodeMap }: IRootState) => ({
  ttlIssuePlaceCodeMapEntity: ttlIssuePlaceCodeMap.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(TtlIssuePlaceCodeMapDetail);
