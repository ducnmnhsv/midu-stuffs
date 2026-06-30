import React, { useEffect } from 'react';
import './portfolio.scss';
import { useAppSelector } from 'app/config/store';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { AUTHORITIES } from 'app/config/constants';
import { useNavigate, useResolvedPath } from 'react-router-dom';
import MarketLeaderSummaryInfo from 'app/custom/entities/portfolio/market-leader-summary-info/market-leader-summary-info';
import MarketLeaderManagement from 'app/custom/entities/portfolio/market-leader-management/market-leader-management';

const PortfolioComponent = () => {
  const navigate = useNavigate();
  const isMarketLeader = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.MARKET_LEADER]));
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.ADMIN, AUTHORITIES.SUPER_ADMIN]));
  const portfolioRootPath = useResolvedPath('/admin/portfolio-management');

  useEffect(() => {
    navigate(`${portfolioRootPath.pathname}${isMarketLeader ? '/summary-info' : '/market-leaders'}`, {
      replace: true,
    });
  }, []);

  return (
    <div className="container-fluid p-wrap">
      <div className="row">{isAdmin && isMarketLeader ? <MarketLeaderManagement /> : isMarketLeader ? <MarketLeaderSummaryInfo /> : <MarketLeaderManagement />}</div>{' '}
    </div>
  );
};

export default PortfolioComponent;
