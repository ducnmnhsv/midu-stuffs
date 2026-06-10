import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './blockholder.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IBlockholderDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BlockholderDetail = (props: IBlockholderDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { blockholderEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="blockholderDetailsHeading">
          <Translate contentKey="eKycAdminApp.blockholder.detail.title">Blockholder</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{blockholderEntity.id}</dd>
          <dt>
            <span id="companyName">
              <Translate contentKey="eKycAdminApp.blockholder.companyName">Company Name</Translate>
            </span>
          </dt>
          <dd>{blockholderEntity.companyName}</dd>
          <dt>
            <span id="stock">
              <Translate contentKey="eKycAdminApp.blockholder.stock">Stock</Translate>
            </span>
          </dt>
          <dd>{blockholderEntity.stock}</dd>
          <dt>
            <span id="position">
              <Translate contentKey="eKycAdminApp.blockholder.position">Position</Translate>
            </span>
          </dt>
          <dd>{blockholderEntity.position}</dd>
          <dt>
            <Translate contentKey="eKycAdminApp.blockholder.eKycAdditionalInfo">E Kyc Additional Info</Translate>
          </dt>
          <dd>{blockholderEntity.eKycAdditionalInfo ? blockholderEntity.eKycAdditionalInfo.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/blockholder" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/blockholder/${blockholderEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ blockholder }: IRootState) => ({
  blockholderEntity: blockholder.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BlockholderDetail);
