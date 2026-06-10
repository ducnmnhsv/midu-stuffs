import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKycExt from './e-kyc-ext';
import EKycExtDetail from './e-kyc-ext-detail';
import EKycExtUpdate from './e-kyc-ext-update';
import EKycExtDeleteDialog from './e-kyc-ext-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={EKycExtUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={EKycExtUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={EKycExtDetail} />
      <ErrorBoundaryRoute path={match.url} component={EKycExt} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={EKycExtDeleteDialog} />
  </>
);

export default Routes;
