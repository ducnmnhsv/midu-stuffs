import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './copy-trading-order.reducer';

export const CopyTradingOrderDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const copyTradingOrderEntity = useAppSelector(state => state.copyTradingOrder.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="copyTradingOrderDetailsHeading">Copy Trading Order</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{copyTradingOrderEntity.id}</dd>
          <dt>
            <span id="jobId">Job Id</span>
          </dt>
          <dd>{copyTradingOrderEntity.jobId}</dd>
          <dt>
            <span id="symbol">Symbol</span>
          </dt>
          <dd>{copyTradingOrderEntity.symbol}</dd>
          <dt>
            <span id="fee">Fee</span>
          </dt>
          <dd>{copyTradingOrderEntity.fee}</dd>
          <dt>
            <span id="tax">Tax</span>
          </dt>
          <dd>{copyTradingOrderEntity.tax}</dd>
          <dt>
            <span id="orderNumber">Order Number</span>
          </dt>
          <dd>{copyTradingOrderEntity.orderNumber}</dd>
          <dt>
            <span id="sellBuyType">Sell Buy Type</span>
          </dt>
          <dd>{copyTradingOrderEntity.sellBuyType}</dd>
          <dt>
            <span id="exchangeType">Exchange Type</span>
          </dt>
          <dd>{copyTradingOrderEntity.exchangeType}</dd>
          <dt>
            <span id="orderType">Order Type</span>
          </dt>
          <dd>{copyTradingOrderEntity.orderType}</dd>
          <dt>
            <span id="orderQuantity">Order Quantity</span>
          </dt>
          <dd>{copyTradingOrderEntity.orderQuantity}</dd>
          <dt>
            <span id="orderPrice">Order Price</span>
          </dt>
          <dd>{copyTradingOrderEntity.orderPrice}</dd>
          <dt>
            <span id="apiParam">Api Param</span>
          </dt>
          <dd>{copyTradingOrderEntity.apiParam}</dd>
          <dt>
            <span id="apiStatusCode">Api Status Code</span>
          </dt>
          <dd>{copyTradingOrderEntity.apiStatusCode}</dd>
          <dt>
            <span id="apiErrorMessage">Api Error Message</span>
          </dt>
          <dd>{copyTradingOrderEntity.apiErrorMessage}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {copyTradingOrderEntity.createdAt ? (
              <TextFormat value={copyTradingOrderEntity.createdAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {copyTradingOrderEntity.updatedAt ? (
              <TextFormat value={copyTradingOrderEntity.updatedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="copySubscriberId">Copy Subscriber Id</span>
          </dt>
          <dd>{copyTradingOrderEntity.copySubscriberId}</dd>
          <dt>
            <span id="copyPortfolioId">Copy Portfolio Id</span>
          </dt>
          <dd>{copyTradingOrderEntity.copyPortfolioId}</dd>
        </dl>
        <Button tag={Link} to="/copy-trading-order" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/copy-trading-order/${copyTradingOrderEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CopyTradingOrderDetail;
