import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './market-history-job-result.reducer';

export const MarketHistoryJobResultDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const marketHistoryJobResultEntity = useAppSelector(state => state.marketHistoryJobResult.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="marketHistoryJobResultDetailsHeading">Market History Job Result</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{marketHistoryJobResultEntity.id}</dd>
          <dt>
            <span id="is_success">Is Success</span>
          </dt>
          <dd>{marketHistoryJobResultEntity.is_success ? 'true' : 'false'}</dd>
          <dt>
            <span id="time_start">Time Start</span>
          </dt>
          <dd>
            {marketHistoryJobResultEntity.time_start ? (
              <TextFormat value={marketHistoryJobResultEntity.time_start} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="time_end">Time End</span>
          </dt>
          <dd>
            {marketHistoryJobResultEntity.time_end ? (
              <TextFormat value={marketHistoryJobResultEntity.time_end} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="error">Error</span>
          </dt>
          <dd>{marketHistoryJobResultEntity.error}</dd>
          <dt>
            <span id="symbols">Symbols</span>
          </dt>
          <dd>{marketHistoryJobResultEntity.symbols}</dd>
          <dt>User</dt>
          <dd>{marketHistoryJobResultEntity.user ? marketHistoryJobResultEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/market-history-job-result" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/market-history-job-result/${marketHistoryJobResultEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default MarketHistoryJobResultDetail;
