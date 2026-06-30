import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-subscriber.reducer';

export const CopySubscriberDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copySubscriberEntity = useAppSelector(state => state.copySubscriber.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copySubscriberDetailsHeading">Copy Subscriber</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copySubscriberEntity.id}</dd>
          <dt>
            <span id="accountNumber">Account Number</span>
          </dt>
          <dd>{copySubscriberEntity.accountNumber}</dd>
          <dt>
            <span id="subNumber">Sub Number</span>
          </dt>
          <dd>{copySubscriberEntity.subNumber}</dd>
          <dt>
            <span id="userName">User Name</span>
          </dt>
          <dd>{copySubscriberEntity.userName}</dd>
          <dt>
            <span id="allocatedRatio">Allocated Ratio</span>
          </dt>
          <dd>{copySubscriberEntity.allocatedRatio}</dd>
          <dt>
            <span id="orderSetType">Order Set Type</span>
          </dt>
          <dd>{copySubscriberEntity.orderSetType}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copySubscriberEntity.createdAt ? (
              <TextFormat value={copySubscriberEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {copySubscriberEntity.updatedAt ? (
              <TextFormat value={copySubscriberEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Ml User Id</dt>
          <dd>{copySubscriberEntity.mlUserId ? copySubscriberEntity.mlUserId.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/copy-subscriber" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-subscriber/${copySubscriberEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopySubscriberDetail;
