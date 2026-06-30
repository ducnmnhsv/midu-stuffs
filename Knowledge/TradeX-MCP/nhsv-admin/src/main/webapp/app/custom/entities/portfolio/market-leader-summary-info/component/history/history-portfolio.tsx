import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { IAccount } from 'app/custom/model/account.model';
import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useResolvedPath } from 'react-router-dom';
import { getCopyPortfolioHistory } from '../../../handle/api/portfolio-api';
import { faArrowsRotate } from '@fortawesome/free-solid-svg-icons';
import { Button, FormGroup, Input, Label } from 'reactstrap';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { getSortState, JhiItemCount, JhiPagination, TextFormat } from 'react-jhipster';
import { ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { ICopyPortfolioHistory } from 'app/custom/model/copyPortfolioHistory.model';
import { APP_TIMESTAMP_FORMAT } from 'app/config/constants';
import TableListEmpty from 'app/custom/entities/commons/table-list-empty';
import HistoryPortfolioDetail from 'app/custom/entities/portfolio/market-leader-summary-info/component/history/history-portfolio-detail';

const HistoryPortfolioComponent = (props) => {
  const accountInfo = props.accountInfo;
  const dispatch = useAppDispatch();
  const location = useLocation();
  const [fromDate, setFromDate] = useState(
    localStorage.getItem('subHistoryFilterFromDate') !== null ? localStorage.getItem('subHistoryFilterFromDate') : ''
  );
  const [toDate, setToDate] = useState(localStorage.getItem('subHistoryFilterToDate') !== null ? localStorage.getItem('subHistoryFilterToDate') : '');
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );
  const navigate = useNavigate();
  const portfolioRootPath = useResolvedPath('/admin/portfolio-management/summary-info');
  const [isShowHistoryDetail, setShowHistoryDetail] = useState(false);

  useEffect(() => {
    if (window.localStorage.filterStatus) {
      localStorage.removeItem('subHistoryFilterFromDate');
    }
    if (window.localStorage.filterRoles) {
      localStorage.removeItem('filterRoles');
    }
  }, [])

  const fetchCopyPortfolioHistory = () => {
    dispatch(
      getCopyPortfolioHistory({
        query: `mlID=${accountInfo.id}&fromDate=${fromDate}&toDate=${toDate}`,
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},desc`,
      })
    );
  };

  const sortEntities = () => {
    fetchCopyPortfolioHistory();
    const endURL = `?fromDate=${fromDate}&toDate=${toDate}&page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`, {
        state: {
          accountInfo: accountInfo,
        },
      });
    }
  };

  useEffect(() => {
    if (accountInfo.id == null) {
      navigate(`${portfolioRootPath.pathname}`, { replace: true });
    } else {
      fetchCopyPortfolioHistory();
    }
  }, [paginationState.activePage, paginationState.order, paginationState.sort, fromDate, toDate]);

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

  const copyPortfolioHistories: ICopyPortfolioHistory[] = useAppSelector(state => state.portfolioReducer.entity.copyPortfolioHistory);
  const loading = useAppSelector(state => state.portfolioReducer.loading);
  const totalItems = useAppSelector(state => state.portfolioReducer.entity.historyPortfolioTotalItems);

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleFromDateChange = e => {
    setFromDate(e.target.value);
    localStorage.setItem('filterFromDate', e.target.value);
  };

  const handleToDateChange = e => {
    setToDate(e.target.value);
    localStorage.setItem('filterToDate', e.target.value);
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const showHistoryPortfolio = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>, id: number) => {
    e.preventDefault();
    setShowHistoryDetail(true);
    navigate(`${portfolioRootPath.pathname}/${accountInfo.id}/history-portfolio/detail?id=${id}`);
  };

  const goBack = () => {
    setShowHistoryDetail(false);
    navigate(`${portfolioRootPath.pathname}/${accountInfo.id}/history-portfolio/detail`);
  };

  return (
    <div>
      {isShowHistoryDetail ? (
        <HistoryPortfolioDetail goBack={goBack} />
      ) : (
        <div className="container-fluid p-0">
          <div className="row p-action-wrap">
            <div className="col-9 d-flex align-items-center">
              <div className="filter-group">
                <div className="form-inline">
                  <FormGroup>
                    <Label className="mr-2" for="fromDate">
                      From Date
                    </Label>
                    <Input
                      onChange={handleFromDateChange}
                      value={fromDate}
                      type="date"
                      name="fromDate"
                      id="fromDate"
                      placeholder="date placeholder"
                      className="mr-2"
                    />
                  </FormGroup>
                  <FormGroup>
                    <Label className="mr-2" for="toDate">
                      To Date
                    </Label>
                    <Input
                      onChange={handleToDateChange}
                      value={toDate}
                      type="date"
                      name="toDate"
                      id="toDate"
                      placeholder="date placeholder"
                      className="mr-2"
                    />
                  </FormGroup>
                </div>
              </div>
            </div>
            <div className="col-3 d-flex justify-content-end align-items-center">
              <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
                <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
              </Button>
            </div>
          </div>
          <div className="row">
            <div className="col-12">
              <table className="table table-hover table-bordered table-responsive align-middle">
                <thead className="table-light">
                  <tr>
                    <th className="p-width-percent-20" scope="col">
                      ID
                    </th>
                    <th className="p-width-percent-60" scope="col">
                      Uploaded Time
                    </th>
                    <th className="p-width-percent-20" scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {copyPortfolioHistories && copyPortfolioHistories.length > 0 ? (
                    copyPortfolioHistories.map((it, i) => (
                      <tr key={`entity-${i}`} data-cy="entityTable">
                        <td className="p-width-percent-20">{it.id}</td>
                        <td className="p-width-percent-60">
                          {it.createdAt ? <TextFormat type="date" value={it.createdAt} format={APP_TIMESTAMP_FORMAT} /> : null}
                        </td>
                        <td className="p-width-percent-20">
                          <Button
                            onClick={event => showHistoryPortfolio(event, it.id)}
                            color="info"
                            size="sm"
                            data-cy="entityDetailsButton"
                          >
                            <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                          </Button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <TableListEmpty title={'Portfolio history'} />
                  )}
                </tbody>
              </table>
              {totalItems ? (
                <div className={copyPortfolioHistories && copyPortfolioHistories.length > 0 ? '' : 'd-none'}>
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
      )}
    </div>
  );
};

export default HistoryPortfolioComponent;
