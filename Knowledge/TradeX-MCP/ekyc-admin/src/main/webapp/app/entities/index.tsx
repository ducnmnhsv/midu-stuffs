import React from 'react';
import { Switch } from 'react-router-dom';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKyc from './e-kyc';
import CustomEKyc from './custom-e-kyc';
import EKycCreatorStatus from './e-kyc-creator-status';
import TtlIssuePlaceCodeMap from './ttl-issue-place-code-map';
import EKycExt from './e-kyc-ext';
import EKycBankList from './e-kyc-bank-list';
import EKycAdditionalInfo from './e-kyc-additional-info';
import PublicCoop from './public-coop';
import Blockholder from './blockholder';
/* jhipster-needle-add-route-import - JHipster will add routes here */
import CustomTtlIssuePlaceCodeMap from './custom-ttl-issue-place-code-map';
import EKycDetail from './custom-e-kyc/e-kyc-popup-detail';

const Routes = ({ match }) => {
  return (
    <div>
      <Switch>
        {/* prettier-ignore */}
        <ErrorBoundaryRoute path={`${match.url}e-kyc`} component={EKyc} />
        <ErrorBoundaryRoute path={`${match.url}ttl-issue-place-code-map`} component={TtlIssuePlaceCodeMap} />
        <ErrorBoundaryRoute path={`${match.url}e-kyc-creator-status`} component={EKycCreatorStatus} />
        <ErrorBoundaryRoute path={`${match.url}e-kyc-ext`} component={EKycExt} />
        <ErrorBoundaryRoute path={`${match.url}e-kyc-bank-list`} component={EKycBankList} />
        <ErrorBoundaryRoute path={`${match.url}e-kyc-additional-info`} component={EKycAdditionalInfo} />
        <ErrorBoundaryRoute path={`${match.url}public-coop`} component={PublicCoop} />
        <ErrorBoundaryRoute path={`${match.url}blockholder`} component={Blockholder} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}

        {/* custom-e-kyc-route */}
        <ErrorBoundaryRoute path={`${match.url}custom-ttl-issue-place-code-map`} component={CustomTtlIssuePlaceCodeMap} />
        <ErrorBoundaryRoute exact path={`${match.url}`} component={CustomEKyc} />
        <ErrorBoundaryRoute exact path={`${match.url}custom-e-kyc/:id`} component={EKycDetail} />
      </Switch>
    </div>
  );
};

export default Routes;
