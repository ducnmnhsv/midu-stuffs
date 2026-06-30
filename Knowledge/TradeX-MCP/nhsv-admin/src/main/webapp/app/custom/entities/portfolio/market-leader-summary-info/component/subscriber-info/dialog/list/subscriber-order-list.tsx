import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { APP_TIMESTAMP_FORMAT } from 'app/config/constants';
import '../../../../../portfolio.scss';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { getSortState, JhiItemCount, JhiPagination, TextFormat } from 'react-jhipster';
import { ASC, DESC } from 'app/shared/util/pagination.constants';
import { Button, FormGroup, Input, Label, Table } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getOrderHistoryOfSub } from 'app/custom/entities/portfolio/handle/api/portfolio-api';
import { SellBuyTypeEnum } from 'app/shared/model/enumerations/sell-buy-type-enum.model';
import { ICopySubscriber } from 'app/shared/model/copy-subscriber.model';

const SubscriberOrderListComponent = ({ subscriberData }: { subscriberData?: ICopySubscriber }) => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const ITEMS_PER_PAGE = 10;
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const copyTradingOrderList = useAppSelector(state => state.portfolioReducer.entity?.mlCopyTradingOrders);
  const loading = useAppSelector(state => state.portfolioReducer.entity.loadingSubHistoryOrders);
  const totalItems = useAppSelector(state => state.portfolioReducer.entity.historyOrderTotalItems);

  const [filterData, setFilterData] = useState({
    fromDate: localStorage.getItem('subOrderFilterFromDate') !== null ? localStorage.getItem('subOrderFilterFromDate') : '',
    toDate: localStorage.getItem('subOrderFilterToDate') !== null ? localStorage.getItem('subOrderFilterToDate') : '',
    roles: localStorage.getItem('subOrderFilterRoles') ? localStorage.getItem('subOrderFilterRoles') : '',
    sellBuyType: localStorage.getItem('subOrderFilterSellBuyType') ? localStorage.getItem('subOrderFilterSellBuyType') : '',
    stockCode: localStorage.getItem('subOrderFilterStockCode') ? localStorage.getItem('subOrderFilterStockCode') : '',
  });
  const sellBuyType = [SellBuyTypeEnum.SELL, SellBuyTypeEnum.BUY];

  const getAllCopyTradingOrder = (requestParams: string) => {
    dispatch(getOrderHistoryOfSub(requestParams));
  };
  const makeRequestParam = subscriberId => {
    const paramArray = [];
    let paramString = ``;
    if (filterData.fromDate) {
      paramArray.push('fromDate=' + (filterData.fromDate ? new Date(filterData.fromDate).toISOString() : ''));
    }
    if (filterData.toDate) {
      paramArray.push('toDate=' + (filterData.toDate ? new Date(filterData.toDate).toISOString() : ''));
    }
    if (filterData.sellBuyType) {
      paramArray.push('sellBuyType=' + filterData.sellBuyType);
    }
    if (filterData.stockCode) {
      paramArray.push('stockCode=' + filterData.stockCode);
    }
    paramArray.push(`subScriberId=${subscriberId}`);
    paramArray.push(`page=${paginationState.activePage - 1}`);
    paramArray.push(
      `size=${paginationState.itemsPerPage && paginationState.itemsPerPage === 0 ? ITEMS_PER_PAGE : paginationState.itemsPerPage}`
    );
    paramArray.push(`sort=${paginationState.sort},${paginationState.order}`);
    if (paramArray.length > 0) {
      for (let i = 0; i < paramArray.length; i++) {
        if (i === 0) {
          paramString += `?${paramArray[i]}`;
        } else {
          paramString += `&${paramArray[i]}`;
        }
      }
    }
    return paramString;
  };

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  useEffect(() => {
    let paramString = makeRequestParam(subscriberData?.id);
    getAllCopyTradingOrder(paramString);
  }, [dispatch, paginationState.activePage, paginationState.order, paginationState.sort, filterData]);

  const handlePagination = currentPage => {
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });
  };

  const handleSyncList = () => {
    let paramString = makeRequestParam(subscriberData?.id);
    getAllCopyTradingOrder(paramString);
  };

  const onFromDateChange = e => {
    setFilterData({
      ...filterData,
      fromDate: e.target.value,
      toDate: new Date(e.target.value) > new Date(filterData.toDate) ? e.target.value : filterData.toDate,
    });
  };

  const onToDateChange = e => {
    setFilterData({
      ...filterData,
      toDate: e.target.value,
      fromDate: new Date(e.target.value) < new Date(filterData.fromDate) ? e.target.value : filterData.fromDate,
    });
  };

  const onStatusChange = e => {
    setFilterData({ ...filterData, sellBuyType: e.target.value });
  };
  const onFullNameChange = e => {
    setFilterData({ ...filterData, stockCode: e.target.value });
  };

  return (
    <div>
      <div className="container-fluid p-0">
        <div className="row">
          <div className="col-12">
            <div className="d-flex justify-content-end align-items-center">
              <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
                <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
              </Button>
            </div>
          </div>
        </div>
        <div className="row p-orders-wrap">
          <div className="col-12">
            <div className="row">
              <div className="col-3">
                <FormGroup>
                  <Label className="mr-2" for="fromDate">
                    From Date
                  </Label>
                  <Input
                    onChange={onFromDateChange}
                    value={filterData.fromDate}
                    type="date"
                    name="fromDate"
                    id="fromDate"
                    placeholder="date placeholder"
                  />
                </FormGroup>
              </div>

              <div className="col-3">
                <FormGroup>
                  <Label className="mr-2" for="toDate">
                    To date
                  </Label>
                  <Input
                    onChange={onToDateChange}
                    value={filterData.toDate}
                    type="date"
                    name="toDate"
                    id="toDate"
                    placeholder="date placeholder"
                  />
                </FormGroup>
              </div>
              <div className="col-3">
                <FormGroup>
                  <Label className="mr-2" for="fullName">
                    Stock code
                  </Label>
                  <Input onChange={onFullNameChange} defaultValue={''} type="text" name="fullName" id="fullName"></Input>
                </FormGroup>
              </div>
              <div className="col-3">
                <FormGroup>
                  <Label className="mr-2" for="status">
                    Buy/ Sell
                  </Label>
                  <Input onChange={onStatusChange} placeholder="Status" type="select" name="status" id="status">
                    <option value="">ALL</option>
                    {sellBuyType.map((value, key) => (
                      <option key={key} value={value}>
                        {value}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="table-responsive">
        {copyTradingOrderList && Array.isArray(copyTradingOrderList) && copyTradingOrderList.length > 0 ? (
          <Table responsive className="table-hover align-middle">
            <thead className="table-light">
              <tr>
                <th className="hand">ID</th>
                <th className="hand">Symbol</th>
                <th className="hand">Order Number</th>
                <th className="hand">Sell Buy Type</th>
                <th className="hand">Exchange Type</th>
                <th className="hand">Order Type</th>
                <th className="hand">Order Quantity</th>
                <th className="hand">Order Price</th>
                <th className="hand">Api Error Message</th>
                <th className="hand">Created At</th>
                <th className="hand">Updated At</th>
              </tr>
            </thead>
            <tbody>
              {copyTradingOrderList.map((copyTradingOrder, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{copyTradingOrder.id} </td>
                  <td>{copyTradingOrder.symbol}</td>
                  <td>{copyTradingOrder.orderNumber}</td>
                  <td>{copyTradingOrder.sellBuyType}</td>
                  <td>{copyTradingOrder.exchangeType}</td>
                  <td>{copyTradingOrder.orderType}</td>
                  <td>{copyTradingOrder.orderQuantity}</td>
                  <td>{copyTradingOrder.orderPrice}</td>
                  <td>{copyTradingOrder.apiErrorMessage}</td>
                  <td>
                    {copyTradingOrder.createdAt ? (
                      <TextFormat type="date" value={copyTradingOrder.createdAt} format={APP_TIMESTAMP_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {copyTradingOrder.updatedAt ? (
                      <TextFormat type="date" value={copyTradingOrder.updatedAt} format={APP_TIMESTAMP_FORMAT} />
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Copy Trading Orders found</div>
        )}
      </div>
      {totalItems ? (
        <div className={copyTradingOrderList && copyTradingOrderList.length > 0 ? 'p-pagination' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default SubscriberOrderListComponent;
