import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import PublicCoop from './public-coop';
import PublicCoopDetail from './public-coop-detail';
import PublicCoopUpdate from './public-coop-update';
import PublicCoopDeleteDialog from './public-coop-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={PublicCoopUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={PublicCoopUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={PublicCoopDetail} />
      <ErrorBoundaryRoute path={match.url} component={PublicCoop} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={PublicCoopDeleteDialog} />
  </>
);

export default Routes;
