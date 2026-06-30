import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUpload } from '@fortawesome/free-solid-svg-icons';
import '../../../portfolio.scss';
import './current-portfolio.scss';
import { IAccount } from 'app/custom/model/account.model';
import { getPortfolioDetail } from '../../../handle/api/portfolio-api';
import CurrentPortfolioList from './list/current-portfolio-list';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { AUTHORITIES } from 'app/config/constants';
import CurrentPortfolioUpload from 'app/custom/entities/portfolio/market-leader-summary-info/component/current-portfolio/upload-dialog/current-portfolio-upload';
import { ICopyPortfolioDetail } from 'app/custom/model/copy-portfolio-detail.model';
import { Button } from 'reactstrap';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { getSortState } from 'react-jhipster';
import { useLocation, useParams } from "react-router-dom";

const CurrentPortfolioComponent = props => {
  const accountInfo: IAccount = props.accountInfo;
  const isMarketLeader = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.MARKET_LEADER]));
  const [isPortfolioOfMarketLeader, setPortfolioOfMarketLeader] = useState(false);
  const dispatch = useAppDispatch();
  const [isShowUploadDialog, setShowUploadDialog] = useState(false);
  const copyPortfolioDetails: ICopyPortfolioDetail[] = useAppSelector(state => state.portfolioReducer.entity?.copyPortfolioDetails);
  const loading = useAppSelector(state => state.portfolioReducer.loading);
  const { pathname } = useLocation();
  const account = useAppSelector(state => state.authentication.account);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  useEffect(() => {
    fetchCopyPortfolioDetail();
  }, [dispatch, paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    if (pathname.includes(`summary-info/${account.id}/current-portfolio`)) {
      setPortfolioOfMarketLeader(true);
    }
  }, [pathname]);

  const fetchCopyPortfolioDetail = () => {
    dispatch(getPortfolioDetail(makeRequestParam()));
  };

  const makeRequestParam = () => {
    const paramArray = [];
    let paramString = ``;
    paramArray.push(`mlID=${accountInfo?.id}`);
    paramArray.push(`page=${paginationState.activePage - 1}`);
    paramArray.push(
      `size=${paginationState.itemsPerPage && paginationState.itemsPerPage === 0 ? ITEMS_PER_PAGE : paginationState.itemsPerPage}`
    );
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

  const handlePagination = currentPage => {
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });
  };

  const reloadCurrentPortfolio = () => {
    fetchCopyPortfolioDetail();
  };

  const toggleUpload = () => {
    setShowUploadDialog(!isShowUploadDialog);
  };

  return (
    <div className="container-fluid p-0">
      <div className="row p-action-wrap">
        <div className="col-12 d-flex justify-content-end align-items-center">
          <Button className="me-2" color="info" onClick={reloadCurrentPortfolio} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          {isMarketLeader && isPortfolioOfMarketLeader ? (
            <button
              type="button"
              className="btn btn-primary"
              onClick={toggleUpload}
              data-bs-toggle="modal"
              data-bs-target="#uploadPortfolio"
            >
              <FontAwesomeIcon icon={faUpload} className="p-icon" /> Upload
            </button>
          ) : (
            ''
          )}
        </div>
      </div>
      <div className="row">
        <div className="col-12">
          <CurrentPortfolioList
            loading={loading}
            copyPortfolioDetails={copyPortfolioDetails}
            paginationState={paginationState}
            handlePagination={handlePagination}
          />
        </div>
      </div>
      <div className="row">
        <CurrentPortfolioUpload
          reloadCurrentPortfolio={reloadCurrentPortfolio}
          isShowUploadDialog={isShowUploadDialog}
          toggleUpload={toggleUpload}
          accountInfo={accountInfo}
        />
      </div>
    </div>
  );
};

export default CurrentPortfolioComponent;
