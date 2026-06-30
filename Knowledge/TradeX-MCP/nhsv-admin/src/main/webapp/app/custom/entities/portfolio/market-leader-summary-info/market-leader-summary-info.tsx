import React, { useEffect, useState } from 'react';
import './../portfolio.scss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChartLine, faClockRotateLeft } from '@fortawesome/free-solid-svg-icons';
import { faUser } from '@fortawesome/free-solid-svg-icons/faUser';
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers';
import { useNavigate, useResolvedPath } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getAccountInfo } from 'app/custom/entities/portfolio/handle/api/portfolio-api';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { AUTHORITIES } from 'app/config/constants';
import { IAccount } from 'app/custom/model/account.model';
import CurrentPortfolio from 'app/custom/entities/portfolio/market-leader-summary-info/component/current-portfolio/current-portfolio';
import HistoryPortfolio from 'app/custom/entities/portfolio/market-leader-summary-info/component/history/history-portfolio';
import AccountInfo from 'app/custom/entities/portfolio/market-leader-summary-info/component/account-info/account-info';
import SubscriberInfo from 'app/custom/entities/portfolio/market-leader-summary-info/component/subscriber-info/subscriber-info';
import lodash from 'lodash';

const MarketLeaderSummaryInfo = props => {
  const isAdmin = useAppSelector(state =>
    hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.SUPER_ADMIN, AUTHORITIES.ADMIN])
  );
  const backToList = isAdmin ? props.backToList : null;
  const [isSelectedCurrentPortfolio, setSelectedCurrentPortfolio] = useState(false);
  const [isSelectedAccountInfo, setSelectedAccountInfo] = useState(true);
  const [isSelectedHistoryPortfolio, setSelectedHistoryPortfolio] = useState(false);
  const [isSelectedSubscribers, setSelectedSubscribers] = useState(false);
  const navigate = useNavigate();
  const portfolioRootPath = useResolvedPath('/admin/portfolio-management/summary-info');
  const dispatch = useAppDispatch();

  const userSelected = props.userSelected;
  const mlAccountInfo: IAccount = useAppSelector(state => state.portfolioReducer.entity.accountInfo);
  const loginAccountInfo: IAccount = useAppSelector(state => state.authentication.account);
  const totalSub = mlAccountInfo?.copyMarketLeaderDetailsDTO?.filter(x => x.key === 'TOTAL_SUB')[0]?.value;

  const fetchAccountInfo = () => {
    dispatch(getAccountInfo(userSelected?.id ? userSelected.id : loginAccountInfo?.id ? loginAccountInfo.id : null));
  };

  useEffect(() => {
    console.log(mlAccountInfo);
    if (isSelectedAccountInfo) {
      navigate(`${portfolioRootPath.pathname}/${loginAccountInfo.id}/account-info`, { replace: true });
    }
  }, [mlAccountInfo]);

  useEffect(() => {
    console.log(mlAccountInfo);
    fetchAccountInfo();
  }, [loginAccountInfo, userSelected]);

  const showCurrentPortfolio = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    e.preventDefault();
    clearSelectedStatus();
    setSelectedCurrentPortfolio(true);
    navigate(`${portfolioRootPath.pathname}/${mlAccountInfo.id}/current-portfolio`, { replace: true });
  };

  const showMLAccountInfo = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    e.preventDefault();
    clearSelectedStatus();
    setSelectedAccountInfo(true);
    navigate(`${portfolioRootPath.pathname}/${mlAccountInfo.id}/account-info`, { replace: true });
  };

  const showSubscriberInfo = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    e.preventDefault();
    clearSelectedStatus();
    setSelectedSubscribers(true);
    navigate(`${portfolioRootPath.pathname}/${mlAccountInfo.id}/subscribers`, {
      state: {
        accountInfo: mlAccountInfo,
      },
      replace: true,
    });
  };

  const clearSelectedStatus = () => {
    setSelectedCurrentPortfolio(false);
    setSelectedAccountInfo(false);
    setSelectedSubscribers(false);
    setSelectedHistoryPortfolio(false);
  };

  const showHistoryPortfolio = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    e.preventDefault();
    clearSelectedStatus();
    setSelectedHistoryPortfolio(true);
    navigate(`${portfolioRootPath.pathname}/${mlAccountInfo.id}/history-portfolio`, {
      state: {
        accountInfo: mlAccountInfo,
      },
      replace: true,
    });
  };

  return mlAccountInfo ? (
    <div className="container-fluid p-wrap">
      <div className="row">
        <div className="col-8">
          <h3 className="p-header-title">Market leader's portfolio management</h3>
        </div>
        {isAdmin ? (
          <div className="col-4 d-flex justify-content-end align-items-center">
            <button id="cancel-save" onClick={backToList} data-cy="entityCreateCancelButton" className="btn btn-secondary">
              <FontAwesomeIcon icon="arrow-left" />
              &nbsp;
              <span className="d-none d-md-inline">Back</span>
            </button>
          </div>
        ) : (
          ''
        )}
      </div>
      <div className="row">
        <div className="accordion p-header-info" id="accordionExample">
          <div className="accordion-item">
            <h2 className="accordion-header">
              <button
                className="accordion-button p-2"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#collapseOne"
                aria-expanded="true"
                aria-controls="collapseOne"
              >
                Market leader summary info
              </button>
            </h2>
            <div id="collapseOne" className="accordion-collapse collapse show" data-bs-parent="#accordionExample">
              <div className="accordion-body">
                <div className="container-fluid p-0">
                  <div className="d-flex flex-sm-column flex-lg-row justify-content-center align-items-center">
                    <div className="p-image-wrap card p-2" style={marginX20}>
                      {mlAccountInfo.photoLink ? (
                        <img src={mlAccountInfo.photoLink} className="card-img-top p-image rounded-circle" alt="..." />
                      ) : (
                        ''
                      )}
                    </div>
                    <div className="p-account-text-info p-2" style={marginX20}>
                      <p className="m-1 p-account-title text-center">
                        <strong>{mlAccountInfo.fullName}</strong>
                      </p>
                      <p className="m-1 p-account-username text-center">{mlAccountInfo.login}</p>
                    </div>
                    <div className="p-2" style={marginX20}>
                      <span className="p-account-title">
                        Subscribers : <strong className="text-primary">{lodash.isEmpty(totalSub) ? 0 : totalSub}</strong>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>{' '}
      {/* End header info */}
      <div className="row">
        <div className="col-sm-12 col-md-3 p-2 d-flex justify-content-center align-items-center">
          <div
            className={`card p-card w-100 p-0 ${isSelectedAccountInfo ? 'p-card-selected' : ''}`}
            onClick={event => showMLAccountInfo(event)}
          >
            <div className="card-body text-dark d-flex justify-content-center align-items-center p-content-tab">
              <FontAwesomeIcon icon={faUser} className="p-icon" />
              <h6 className="card-title p-text-capitalize m-0">Market leader account</h6>
            </div>
          </div>
        </div>
        <div className="col-sm-12 col-md-3 p-2 d-flex justify-content-center align-items-center">
          <div
            className={`card p-card w-100 p-0 ${isSelectedCurrentPortfolio ? 'p-card-selected' : ''}`}
            onClick={event => showCurrentPortfolio(event)}
          >
            <div className="card-body text-dark d-flex justify-content-center align-items-center p-content-tab">
              <FontAwesomeIcon icon={faChartLine} className="p-icon" />
              <h6 className="card-title p-text-capitalize m-0">Current portfolio</h6>
            </div>
          </div>
        </div>
        <div className="col-sm-12 col-md-3 p-2 d-flex justify-content-center align-items-center">
          <div
            className={`card p-card w-100 p-0 ${isSelectedHistoryPortfolio ? 'p-card-selected' : ''}`}
            onClick={event => showHistoryPortfolio(event)}
          >
            <div className="card-body text-dark d-flex justify-content-center align-items-center p-content-tab">
              <FontAwesomeIcon icon={faClockRotateLeft} className="p-icon" />
              <h6 className="card-title p-text-capitalize m-0">Historical portfolio</h6>
            </div>
          </div>
        </div>
        <div className="col-sm-12 col-md-3 p-2 d-flex justify-content-center align-items-center">
          <div
            className={`card p-card w-100 p-0 ${isSelectedSubscribers ? 'p-card-selected' : ''}`}
            onClick={event => showSubscriberInfo(event)}
          >
            <div className="card-body text-dark d-flex justify-content-center align-items-center p-content-tab">
              <FontAwesomeIcon icon={faUsers} className="p-icon" />
              <h6 className="card-title p-text-capitalize m-0">Subscribers</h6>
            </div>
          </div>
        </div>
      </div>
      {/* END tab info */}
      {/* Details info */}
      <div className="row p-component-wrap">
        {isSelectedCurrentPortfolio ? <CurrentPortfolio accountInfo={mlAccountInfo} /> : ''}
        {isSelectedHistoryPortfolio ? <HistoryPortfolio accountInfo={mlAccountInfo} /> : ''}
        {isSelectedSubscribers ? <SubscriberInfo accountInfo={mlAccountInfo} /> : ''}
        {isSelectedAccountInfo ? <AccountInfo accountInfo={mlAccountInfo} /> : ''}
      </div>
      {/* END Details info */}
    </div>
  ) : (
    <div>empty</div>
  );
};

const marginX20 = {
  margin: '0 20px',
};

export default MarketLeaderSummaryInfo;
