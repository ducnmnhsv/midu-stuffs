import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-portfolio-detail-history.reducer';

export const CopyPortfolioDetailHistoryDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copyPortfolioDetailHistoryEntity = useAppSelector(state => state.copyPortfolioDetailHistory.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copyPortfolioDetailHistoryDetailsHeading">Copy Portfolio Detail History</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copyPortfolioDetailHistoryEntity.id}</dd>
          <dt>
            <span id="symbol">Symbol</span>
          </dt>
          <dd>{copyPortfolioDetailHistoryEntity.symbol}</dd>
          <dt>
            <span id="weight">Weight</span>
          </dt>
          <dd>{copyPortfolioDetailHistoryEntity.weight}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copyPortfolioDetailHistoryEntity.createdAt ? (
              <TextFormat value={copyPortfolioDetailHistoryEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Copy Portfolio History Id</dt>
          <dd>
            {copyPortfolioDetailHistoryEntity.copyPortfolioHistoryId ? copyPortfolioDetailHistoryEntity.copyPortfolioHistoryId.id : ''}
          </dd>
        </dl>
        <Button tag={Link} to="/copy-portfolio-detail-history" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-portfolio-detail-history/${copyPortfolioDetailHistoryEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopyPortfolioDetailHistoryDetail;
