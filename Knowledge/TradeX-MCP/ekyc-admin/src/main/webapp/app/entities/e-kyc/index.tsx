import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKyc from './e-kyc';
import EKycDetail from './e-kyc-detail';
import EKycUpdate from './e-kyc-update';
import EKycDeleteDialog from './e-kyc-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={EKycUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={EKycUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={EKycDetail} />
      <ErrorBoundaryRoute path={match.url} component={EKyc} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={EKycDeleteDialog} />
  </>
);

export default Routes;
