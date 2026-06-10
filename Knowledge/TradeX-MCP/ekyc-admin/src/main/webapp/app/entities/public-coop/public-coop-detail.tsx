import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './public-coop.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IPublicCoopDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const PublicCoopDetail = (props: IPublicCoopDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { publicCoopEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="publicCoopDetailsHeading">
          <Translate contentKey="eKycAdminApp.publicCoop.detail.title">PublicCoop</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{publicCoopEntity.id}</dd>
          <dt>
            <span id="companyName">
              <Translate contentKey="eKycAdminApp.publicCoop.companyName">Company Name</Translate>
            </span>
          </dt>
          <dd>{publicCoopEntity.companyName}</dd>
          <dt>
            <span id="stock">
              <Translate contentKey="eKycAdminApp.publicCoop.stock">Stock</Translate>
            </span>
          </dt>
          <dd>{publicCoopEntity.stock}</dd>
          <dt>
            <span id="position">
              <Translate contentKey="eKycAdminApp.publicCoop.position">Position</Translate>
            </span>
          </dt>
          <dd>{publicCoopEntity.position}</dd>
          <dt>
            <Translate contentKey="eKycAdminApp.publicCoop.eKycAdditionalInfo">E Kyc Additional Info</Translate>
          </dt>
          <dd>{publicCoopEntity.eKycAdditionalInfo ? publicCoopEntity.eKycAdditionalInfo.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/public-coop" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/public-coop/${publicCoopEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ publicCoop }: IRootState) => ({
  publicCoopEntity: publicCoop.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(PublicCoopDetail);
