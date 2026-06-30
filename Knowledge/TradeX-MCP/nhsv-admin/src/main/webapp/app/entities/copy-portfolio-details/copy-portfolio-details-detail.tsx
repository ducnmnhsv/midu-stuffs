import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-portfolio-details.reducer';

export const CopyPortfolioDetailsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copyPortfolioDetailsEntity = useAppSelector(state => state.copyPortfolioDetails.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copyPortfolioDetailsDetailsHeading">Copy Portfolio Details</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copyPortfolioDetailsEntity.id}</dd>
          <dt>
            <span id="symbol">Symbol</span>
          </dt>
          <dd>{copyPortfolioDetailsEntity.symbol}</dd>
          <dt>
            <span id="weight">Weight</span>
          </dt>
          <dd>{copyPortfolioDetailsEntity.weight}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copyPortfolioDetailsEntity.createdAt ? (
              <TextFormat value={copyPortfolioDetailsEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Copy Portfolio Id</dt>
          <dd>{copyPortfolioDetailsEntity.copyPortfolioId ? copyPortfolioDetailsEntity.copyPortfolioId.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/copy-portfolio-details" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-portfolio-details/${copyPortfolioDetailsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopyPortfolioDetailsDetail;
