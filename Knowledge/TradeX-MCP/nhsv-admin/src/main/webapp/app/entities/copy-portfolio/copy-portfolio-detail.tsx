import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-portfolio.reducer';

export const CopyPortfolioDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copyPortfolioEntity = useAppSelector(state => state.copyPortfolio.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copyPortfolioDetailsHeading">Copy Portfolio</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copyPortfolioEntity.id}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copyPortfolioEntity.createdAt ? (
              <TextFormat value={copyPortfolioEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Ml User Id</dt>
          <dd>{copyPortfolioEntity.mlUserId ? copyPortfolioEntity.mlUserId.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/copy-portfolio" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-portfolio/${copyPortfolioEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopyPortfolioDetail;
