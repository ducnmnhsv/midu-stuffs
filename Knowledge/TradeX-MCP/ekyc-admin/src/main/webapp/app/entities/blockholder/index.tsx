import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Blockholder from './blockholder';
import BlockholderDetail from './blockholder-detail';
import BlockholderUpdate from './blockholder-update';
import BlockholderDeleteDialog from './blockholder-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={BlockholderUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={BlockholderUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={BlockholderDetail} />
      <ErrorBoundaryRoute path={match.url} component={Blockholder} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={BlockholderDeleteDialog} />
  </>
);

export default Routes;
