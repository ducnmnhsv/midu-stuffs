import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import TtlIssuePlaceCodeMap from './ttl-issue-place-code-map';
import TtlIssuePlaceCodeMapDetail from './ttl-issue-place-code-map-detail';
import TtlIssuePlaceCodeMapUpdate from './ttl-issue-place-code-map-update';
import TtlIssuePlaceCodeMapDeleteDialog from './ttl-issue-place-code-map-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={TtlIssuePlaceCodeMapUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={TtlIssuePlaceCodeMapUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={TtlIssuePlaceCodeMapDetail} />
      <ErrorBoundaryRoute path={match.url} component={TtlIssuePlaceCodeMap} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={TtlIssuePlaceCodeMapDeleteDialog} />
  </>
);

export default Routes;
