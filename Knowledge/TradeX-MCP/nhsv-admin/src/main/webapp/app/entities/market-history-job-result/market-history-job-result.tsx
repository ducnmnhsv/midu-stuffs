import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IMarketHistoryJobResult } from 'app/shared/model/market-history-job-result.model';
import { getEntities } from './market-history-job-result.reducer';
import CreateMarketHistoryJobResult from './create-market-history-job-result'; // Import component mới

export const MarketHistoryJobResult = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const marketHistoryJobResultList = useAppSelector(state => state.marketHistoryJobResult.entities);
  const loading = useAppSelector(state => state.marketHistoryJobResult.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="market-history-job-result-heading" data-cy="MarketHistoryJobResultHeading">
        Market History Job Results
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link
            to="/market-history-job-result/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Market History Job Result
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {marketHistoryJobResultList && marketHistoryJobResultList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>Is Success</th>
                <th>Time Start</th>
                <th>Time End</th>
                <th>Error</th>
                <th>Symbols</th>
                <th>User</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {marketHistoryJobResultList.map((marketHistoryJobResult, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/latest-job-result/${marketHistoryJobResult.id}`} color="link" size="sm">
                      {marketHistoryJobResult.id}
                    </Button>
                  </td>
                  <td>{marketHistoryJobResult.is_success ? 'true' : 'false'}</td>
                  <td>
                    {marketHistoryJobResult.time_start ? (
                      <TextFormat type="date" value={marketHistoryJobResult.time_start} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {marketHistoryJobResult.time_end ? (
                      <TextFormat type="date" value={marketHistoryJobResult.time_end} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{marketHistoryJobResult.error}</td>
                  <td>{marketHistoryJobResult.symbols}</td>
                  <td>{marketHistoryJobResult.user ? marketHistoryJobResult.user.login : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/latest-job-result/${marketHistoryJobResult.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/latest-job-result/${marketHistoryJobResult.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/latest-job-result/${marketHistoryJobResult.id}/delete`}
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
          !loading && <div className="alert alert-warning">No Market History Job Results found</div>
        )}
      </div>
    </div>
  );
};

export default MarketHistoryJobResult;
