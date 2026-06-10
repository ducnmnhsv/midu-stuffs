import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKyc from './e-kyc';
import EKycDetail from './e-kyc-popup-detail';

const Routes = ({ match }) => {
  return (
    <>
      <Switch>
        <ErrorBoundaryRoute exact path={`${match.url}custom-e-kyc/:id`} component={EKycDetail} />
        <ErrorBoundaryRoute path={match.url} component={EKyc} />
      </Switch>
    </>
  );
};

export default Routes;
