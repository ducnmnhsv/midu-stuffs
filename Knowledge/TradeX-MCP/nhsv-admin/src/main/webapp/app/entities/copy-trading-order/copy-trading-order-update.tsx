import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ICopyTradingOrder } from 'app/shared/model/copy-trading-order.model';
import { SellBuyTypeEnum } from 'app/shared/model/enumerations/sell-buy-type-enum.model';
import { ExchangeTypeEnum } from 'app/shared/model/enumerations/exchange-type-enum.model';
import { OrderTypeEnum } from 'app/shared/model/enumerations/order-type-enum.model';
import { getEntity, updateEntity, createEntity, reset } from './copy-trading-order.reducer';

export const CopyTradingOrderUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const copyTradingOrderEntity = useAppSelector(state => state.copyTradingOrder.entity);
  const loading = useAppSelector(state => state.copyTradingOrder.loading);
  const updating = useAppSelector(state => state.copyTradingOrder.updating);
  const updateSuccess = useAppSelector(state => state.copyTradingOrder.updateSuccess);
  const sellBuyTypeEnumValues = Object.keys(SellBuyTypeEnum);
  const exchangeTypeEnumValues = Object.keys(ExchangeTypeEnum);
  const orderTypeEnumValues = Object.keys(OrderTypeEnum);

  const handleClose = () => {
    navigate('/copy-trading-order' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);

    const entity = {
      ...copyTradingOrderEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          createdAt: displayDefaultDateTime(),
          updatedAt: displayDefaultDateTime(),
        }
      : {
          sellBuyType: 'BUY',
          exchangeType: 'HOSE',
          orderType: 'ATO',
          ...copyTradingOrderEntity,
          createdAt: convertDateTimeFromServer(copyTradingOrderEntity.createdAt),
          updatedAt: convertDateTimeFromServer(copyTradingOrderEntity.updatedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.copyTradingOrder.home.createOrEditLabel" data-cy="CopyTradingOrderCreateUpdateHeading">
            Create or edit a Copy Trading Order
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="copy-trading-order-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Job Id"
                id="copy-trading-order-jobId"
                name="jobId"
                data-cy="jobId"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Symbol"
                id="copy-trading-order-symbol"
                name="symbol"
                data-cy="symbol"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField label="Fee" id="copy-trading-order-fee" name="fee" data-cy="fee" type="text" />
              <ValidatedField label="Tax" id="copy-trading-order-tax" name="tax" data-cy="tax" type="text" />
              <ValidatedField
                label="Order Number"
                id="copy-trading-order-orderNumber"
                name="orderNumber"
                data-cy="orderNumber"
                type="text"
              />
              <ValidatedField
                label="Sell Buy Type"
                id="copy-trading-order-sellBuyType"
                name="sellBuyType"
                data-cy="sellBuyType"
                type="select"
              >
                {sellBuyTypeEnumValues.map(sellBuyTypeEnum => (
                  <option value={sellBuyTypeEnum} key={sellBuyTypeEnum}>
                    {sellBuyTypeEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Exchange Type"
                id="copy-trading-order-exchangeType"
                name="exchangeType"
                data-cy="exchangeType"
                type="select"
              >
                {exchangeTypeEnumValues.map(exchangeTypeEnum => (
                  <option value={exchangeTypeEnum} key={exchangeTypeEnum}>
                    {exchangeTypeEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Order Type" id="copy-trading-order-orderType" name="orderType" data-cy="orderType" type="select">
                {orderTypeEnumValues.map(orderTypeEnum => (
                  <option value={orderTypeEnum} key={orderTypeEnum}>
                    {orderTypeEnum}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Order Quantity"
                id="copy-trading-order-orderQuantity"
                name="orderQuantity"
                data-cy="orderQuantity"
                type="text"
              />
              <ValidatedField label="Order Price" id="copy-trading-order-orderPrice" name="orderPrice" data-cy="orderPrice" type="text" />
              <ValidatedField label="Api Param" id="copy-trading-order-apiParam" name="apiParam" data-cy="apiParam" type="text" />
              <ValidatedField
                label="Api Status Code"
                id="copy-trading-order-apiStatusCode"
                name="apiStatusCode"
                data-cy="apiStatusCode"
                type="text"
              />
              <ValidatedField
                label="Api Error Message"
                id="copy-trading-order-apiErrorMessage"
                name="apiErrorMessage"
                data-cy="apiErrorMessage"
                type="text"
              />
              <ValidatedField
                label="Created At"
                id="copy-trading-order-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Updated At"
                id="copy-trading-order-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Copy Subscriber Id"
                id="copy-trading-order-copySubscriberId"
                name="copySubscriberId"
                data-cy="copySubscriberId"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Copy Portfolio Id"
                id="copy-trading-order-copyPortfolioId"
                name="copyPortfolioId"
                data-cy="copyPortfolioId"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/copy-trading-order" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default CopyTradingOrderUpdate;
