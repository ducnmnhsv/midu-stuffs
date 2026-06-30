import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getCopyPortfolioDetailHistory } from '../../../handle/api/portfolio-api';
import { faArrowLeft, faArrowsRotate, faTriangleExclamation } from '@fortawesome/free-solid-svg-icons';
import { ICopyPortfolioDetailHistory } from 'app/custom/model/copyPortfolioDetailHistory.model';
import { getSortState, JhiItemCount, JhiPagination, TextFormat } from 'react-jhipster';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { APP_TIMESTAMP_FORMAT } from 'app/config/constants';
import lodash from 'lodash';

const HistoryPortfolioDetailComponent = props => {
  const goBack = props.goBack;
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const searchParams = new URLSearchParams(location.search);

  const fetchCopyPortfolioDetailHistory = () => {
    dispatch(
      getCopyPortfolioDetailHistory({
        query: `id=${searchParams.get('id')}`,
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      })
    );
  };

  const sortEntities = () => {
    fetchCopyPortfolioDetailHistory();
    const endURL = `?id=${searchParams.get('id')}&page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    if (searchParams.get('id') == null) {
      navigate(-1);
    } else {
      fetchCopyPortfolioDetailHistory();
    }
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

  const copyPortfolioDetailHistories: ICopyPortfolioDetailHistory[] = useAppSelector(
    state => state.portfolioReducer.entity.copyPortfolioDetailHistory
  );
  const totalItems = useAppSelector(state => state.portfolioReducer.entity.historyPortfolioDetailTotalItems);
  const loading = useAppSelector(state => state.portfolioReducer.loading);

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
    <div className="container-fluid p-0">
      <div className="row p-action-wrap">
        <div className="col-12 d-flex justify-content-between align-items-center">
          <button
            type="button"
            onClick={goBack}
            disabled={loading}
            className="mb-3 btn btn-secondary d-flex justify-content-end align-items-center"
          >
            <FontAwesomeIcon icon={faArrowLeft} spin={loading} className="p-icon" /> Back
          </button>
          <button
            type="button"
            onClick={handleSyncList}
            disabled={loading}
            className="mb-3 btn btn-info d-flex justify-content-end align-items-center text-dark"
          >
            <FontAwesomeIcon icon={faArrowsRotate} spin={loading} className="p-icon" /> Refresh list
          </button>
        </div>
      </div>
      <div className="row">
        <div className="col-12">
          <table className="table table-hover table-bordered table-responsive align-middle">
            <thead className="table-light">
              <tr>
                <th className="text-center" onClick={sort('symbol')}>
                  Stock Code <FontAwesomeIcon icon="sort" />
                </th>
                <th className="text-center" onClick={sort('weight')}>
                  Stock Weight <FontAwesomeIcon icon="sort" />
                </th>
                <th className="text-center" onClick={sort('createdAt')}>
                  Uploaded Time <FontAwesomeIcon icon="sort" />
                </th>
              </tr>
            </thead>
            <tbody>
              {copyPortfolioDetailHistories.length === 0 ? (
                <tr>
                  <td colSpan={3} className="text-center text-black-50">
                    <FontAwesomeIcon icon={faTriangleExclamation} className="p-icon-l" /> Details history portfolio info is empty
                  </td>
                </tr>
              ) : (
                copyPortfolioDetailHistories.map((it, i) => (
                  <tr key={`entity-${i}`} data-cy="entityTable">
                    <td className="text-center">{it.symbol}</td>
                    <td className="text-center">{lodash.round(it.weight * 100, 2)} %</td>
                    <td className="text-center">
                      {it.createdAt ? <TextFormat type="date" value={it.createdAt} format={APP_TIMESTAMP_FORMAT} /> : null}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
          {totalItems ? (
            <div className={copyPortfolioDetailHistories && copyPortfolioDetailHistories.length > 0 ? '' : 'd-none'}>
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
      </div>
    </div>
  );
};

export default HistoryPortfolioDetailComponent;
