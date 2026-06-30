import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-market-leader-details.reducer';

export const CopyMarketLeaderDetailsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copyMarketLeaderDetailsEntity = useAppSelector(state => state.copyMarketLeaderDetails.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copyMarketLeaderDetailsDetailsHeading">Copy Market Leader Details</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copyMarketLeaderDetailsEntity.id}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copyMarketLeaderDetailsEntity.createdAt ? (
              <TextFormat value={copyMarketLeaderDetailsEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {copyMarketLeaderDetailsEntity.updatedAt ? (
              <TextFormat value={copyMarketLeaderDetailsEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="type">Type</span>
          </dt>
          <dd>{copyMarketLeaderDetailsEntity.type}</dd>
          <dt>
            <span id="label">Label</span>
          </dt>
          <dd>{copyMarketLeaderDetailsEntity.label}</dd>
          <dt>
            <span id="key">Key</span>
          </dt>
          <dd>{copyMarketLeaderDetailsEntity.key}</dd>
          <dt>
            <span id="value">Value</span>
          </dt>
          <dd>{copyMarketLeaderDetailsEntity.value}</dd>
          <dt>Ml User Id</dt>
          <dd>{copyMarketLeaderDetailsEntity.mlUserId ? copyMarketLeaderDetailsEntity.mlUserId.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/copy-market-leader-details" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-market-leader-details/${copyMarketLeaderDetailsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopyMarketLeaderDetailsDetail;
