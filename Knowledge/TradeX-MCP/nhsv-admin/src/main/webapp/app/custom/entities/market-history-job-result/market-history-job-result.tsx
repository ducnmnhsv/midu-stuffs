import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat, JhiItemCount, JhiPagination, getSortState } from 'react-jhipster';

import {APP_TIMESTAMP_FORMAT} from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import {getEntities} from './market-history-job-result.reducer';
import CreateMarketHistoryJobResult from './create-market-history-job-result';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { IMarketHistoryJobResultStockEvent, StockEventType } from 'app/custom/model/market-history-job-result.model';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTriangleExclamation } from '@fortawesome/free-solid-svg-icons';

export const MarketHistoryJobResult = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id', DESC), location.search)
  );

  const entities: IMarketHistoryJobResultStockEvent[] = useAppSelector(state => state.latestJobResultReducer.entities);
  const totalItems: number = useAppSelector(state => state.latestJobResultReducer.totalItems);

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

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  return (
    <div>
      <h2 id="market-history-job-result-heading" data-cy="MarketHistoryJobResultHeading">
        Market History Job Results
        <div className="d-flex justify-content-end">
        </div>
      </h2>
      <div className="table-responsive">
        <Table responsive>
          <thead>
            <tr>
              <th>ID</th>
              <th>Symbols</th>
              <th>Event Type</th>
              <th>Event Name</th>
              <th>Is Success</th>
              <th>Time Start</th>
              <th>Time End</th>
              <th>Error</th>
            </tr>
          </thead>
          <tbody>
            {entities && entities.length > 0 ? (
              entities.map((it: IMarketHistoryJobResultStockEvent, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{it.id}</td>
                  <td>{it.symbols}</td>
                  <td>{StockEventType[it.eventType]}</td>
                  <td>{it.eventName}</td>
                  <td>{it.isSuccess ? 'true' : 'false'}</td>
                  <td>
                    {it.timeStart ? (
                      <TextFormat type="date" value={it.timeStart} format={APP_TIMESTAMP_FORMAT}/>
                    ) : null}
                  </td>
                  <td>
                    {it.timeEnd ? (
                      <TextFormat type="date" value={it.timeEnd} format={APP_TIMESTAMP_FORMAT}/>
                    ) : null}
                  </td>
                  <td>{it.error}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={8} className="text-center">
                  <FontAwesomeIcon icon={faTriangleExclamation} /> Data is empty
                </td>
              </tr>
            )}
          </tbody>
        </Table>
        {totalItems ? (
          <div className={entities?.length > 0 ? '' : 'd-none'}>
            <div className="justify-content-center d-flex">
              <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
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
      <CreateMarketHistoryJobResult />
    </div>
  );
};

export default MarketHistoryJobResult;
