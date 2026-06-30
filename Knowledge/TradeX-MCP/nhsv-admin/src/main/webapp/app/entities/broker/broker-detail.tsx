import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './broker.reducer';

export const BrokerDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const brokerEntity = useAppSelector(state => state.broker.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="brokerDetailsHeading">Broker</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">Id</span>
          </dt>
          <dd>{brokerEntity.id}</dd>
          <dt>
            <span id="username">Username</span>
          </dt>
          <dd>{brokerEntity.username}</dd>
          <dt>
            <span id="fullname">Fullname</span>
          </dt>
          <dd>{brokerEntity.fullname}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{brokerEntity.status ? 'true' : 'false'}</dd>
          <dt>
            <span id="totalChatRoom">Total Chat Room</span>
          </dt>
          <dd>{brokerEntity.totalChatRoom}</dd>
          <dt>
            <span id="currentRank">Current Rank</span>
          </dt>
          <dd>{brokerEntity.currentRank}</dd>
          <dt>
            <span id="isDynamic">Is Dynamic</span>
          </dt>
          <dd>{brokerEntity.isDynamic ? 'true' : 'false'}</dd>
          <dt>
            <span id="email">Email</span>
          </dt>
          <dd>{brokerEntity.email}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>{brokerEntity.createdAt ? <TextFormat value={brokerEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>{brokerEntity.updatedAt ? <TextFormat value={brokerEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="deactivatedAt">Deactivated At</span>
          </dt>
          <dd>
            {brokerEntity.deactivatedAt ? <TextFormat value={brokerEntity.deactivatedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="deactivatedBy">Deactivated By</span>
          </dt>
          <dd>{brokerEntity.deactivatedBy}</dd>
          <dt>
            <span id="invitedBy">Invited By</span>
          </dt>
          <dd>{brokerEntity.invitedBy}</dd>
        </dl>
        <Button tag={Link} to="/broker" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/broker/${brokerEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default BrokerDetail;
