import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKycCreatorStatus from './e-kyc-creator-status';
import EKycCreatorStatusDetail from './e-kyc-creator-status-detail';
import EKycCreatorStatusUpdate from './e-kyc-creator-status-update';
import EKycCreatorStatusDeleteDialog from './e-kyc-creator-status-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={EKycCreatorStatusUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={EKycCreatorStatusUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={EKycCreatorStatusDetail} />
      <ErrorBoundaryRoute path={match.url} component={EKycCreatorStatus} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={EKycCreatorStatusDeleteDialog} />
  </>
);

export default Routes;
