import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity } from './copy-trading-register.reducer';
export const CopyTradingRegisterDetail = () => {
  const dispatch = useAppDispatch();
  const { id } = useParams<'id'>();
  useEffect(() => {
    dispatch(getEntity(id));
  }, []);
  const copyTradingRegisterEntity = useAppSelector(state => state.copyTradingRegister.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copyTradingRegisterDetailsHeading">Copy Trading Register</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copyTradingRegisterEntity.id}</dd>
          <dt>
            <span id="accountNumber">Account Number</span>
          </dt>
          <dd>{copyTradingRegisterEntity.accountNumber}</dd>
          <dt>
            <span id="subAccount">Sub Account</span>
          </dt>
          <dd>{copyTradingRegisterEntity.subAccount}</dd>
          <dt>
            <span id="customerName">Customer Name</span>
          </dt>
          <dd>{copyTradingRegisterEntity.customerName}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{copyTradingRegisterEntity.status ? 'true' : 'false'}</dd>
          <dt>
            <span id="createAt">Create At</span>
          </dt>
          <dd>
            {copyTradingRegisterEntity.createAt ? (
              <TextFormat value={copyTradingRegisterEntity.createAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {copyTradingRegisterEntity.updatedAt ? (
              <TextFormat value={copyTradingRegisterEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
        </dl>
        <Button tag={Link} to="/copy-trading-register" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-trading-register/${copyTradingRegisterEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};
export default CopyTradingRegisterDetail;
