import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKycAdditionalInfo from './e-kyc-additional-info';
import EKycAdditionalInfoDetail from './e-kyc-additional-info-detail';
import EKycAdditionalInfoUpdate from './e-kyc-additional-info-update';
import EKycAdditionalInfoDeleteDialog from './e-kyc-additional-info-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={EKycAdditionalInfoUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={EKycAdditionalInfoUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={EKycAdditionalInfoDetail} />
      <ErrorBoundaryRoute path={match.url} component={EKycAdditionalInfo} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={EKycAdditionalInfoDeleteDialog} />
  </>
);

export default Routes;
