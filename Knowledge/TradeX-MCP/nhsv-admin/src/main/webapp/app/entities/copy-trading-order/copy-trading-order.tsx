import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat, getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ICopyTradingOrder } from 'app/shared/model/copy-trading-order.model';
import { getEntities } from './copy-trading-order.reducer';

export const CopyTradingOrder = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const copyTradingOrderList = useAppSelector(state => state.copyTradingOrder.entities);
  const loading = useAppSelector(state => state.copyTradingOrder.loading);
  const totalItems = useAppSelector(state => state.copyTradingOrder.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      })
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [location.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  return (
    <div>
      <h2 id="copy-trading-order-heading" data-cy="CopyTradingOrderHeading">
        Copy Trading Orders
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link
            to="/copy-trading-order/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Copy Trading Order
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {copyTradingOrderList && copyTradingOrderList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('jobId')}>
                  Job Id <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('symbol')}>
                  Symbol <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('fee')}>
                  Fee <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('tax')}>
                  Tax <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('orderNumber')}>
                  Order Number <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('sellBuyType')}>
                  Sell Buy Type <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('exchangeType')}>
                  Exchange Type <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('orderType')}>
                  Order Type <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('orderQuantity')}>
                  Order Quantity <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('orderPrice')}>
                  Order Price <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('apiParam')}>
                  Api Param <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('apiStatusCode')}>
                  Api Status Code <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('apiErrorMessage')}>
                  Api Error Message <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  Created At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('updatedAt')}>
                  Updated At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('copySubscriberId')}>
                  Copy Subscriber Id <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('copyPortfolioId')}>
                  Copy Portfolio Id <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {copyTradingOrderList.map((copyTradingOrder, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/copy-trading-order/${copyTradingOrder.id}`} color="link" size="sm">
                      {copyTradingOrder.id}
                    </Button>
                  </td>
                  <td>{copyTradingOrder.jobId}</td>
                  <td>{copyTradingOrder.symbol}</td>
                  <td>{copyTradingOrder.fee}</td>
                  <td>{copyTradingOrder.tax}</td>
                  <td>{copyTradingOrder.orderNumber}</td>
                  <td>{copyTradingOrder.sellBuyType}</td>
                  <td>{copyTradingOrder.exchangeType}</td>
                  <td>{copyTradingOrder.orderType}</td>
                  <td>{copyTradingOrder.orderQuantity}</td>
                  <td>{copyTradingOrder.orderPrice}</td>
                  <td>{copyTradingOrder.apiParam}</td>
                  <td>{copyTradingOrder.apiStatusCode}</td>
                  <td>{copyTradingOrder.apiErrorMessage}</td>
                  <td>
                    {copyTradingOrder.createdAt ? (
                      <TextFormat type="date" value={copyTradingOrder.createdAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {copyTradingOrder.updatedAt ? (
                      <TextFormat type="date" value={copyTradingOrder.updatedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{copyTradingOrder.copySubscriberId}</td>
                  <td>{copyTradingOrder.copyPortfolioId}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/copy-trading-order/${copyTradingOrder.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/copy-trading-order/${copyTradingOrder.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/copy-trading-order/${copyTradingOrder.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
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
        <div className={copyTradingOrderList && copyTradingOrderList.length > 0 ? '' : 'd-none'}>
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

export default CopyTradingOrder;
