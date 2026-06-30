import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-subscriber-history.reducer';

export const CopySubscriberHistoryDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copySubscriberHistoryEntity = useAppSelector(state => state.copySubscriberHistory.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copySubscriberHistoryDetailsHeading">Copy Subscriber History</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copySubscriberHistoryEntity.id}</dd>
          <dt>
            <span id="accountNumber">Account Number</span>
          </dt>
          <dd>{copySubscriberHistoryEntity.accountNumber}</dd>
          <dt>
            <span id="subNumber">Sub Number</span>
          </dt>
          <dd>{copySubscriberHistoryEntity.subNumber}</dd>
          <dt>
            <span id="userName">User Name</span>
          </dt>
          <dd>{copySubscriberHistoryEntity.userName}</dd>
          <dt>
            <span id="allocatedRatio">Allocated Ratio</span>
          </dt>
          <dd>{copySubscriberHistoryEntity.allocatedRatio}</dd>
          <dt>
            <span id="orderSetType">Order Set Type</span>
          </dt>
          <dd>{copySubscriberHistoryEntity.orderSetType}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copySubscriberHistoryEntity.createdAt ? (
              <TextFormat value={copySubscriberHistoryEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {copySubscriberHistoryEntity.updatedAt ? (
              <TextFormat value={copySubscriberHistoryEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Ml User Id</dt>
          <dd>{copySubscriberHistoryEntity.mlUserId ? copySubscriberHistoryEntity.mlUserId.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/copy-subscriber-history" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-subscriber-history/${copySubscriberHistoryEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopySubscriberHistoryDetail;
