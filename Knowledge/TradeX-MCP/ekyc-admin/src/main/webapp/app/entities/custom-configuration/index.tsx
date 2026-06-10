import {Switch} from "react-router-dom";
import React from "react";
import ErrorBoundaryRoute from "app/shared/error/error-boundary-route";
import {MatchingRateConfiguration} from "app/entities/custom-configuration/matching-rate";

const Routes = ({ match }) => {
  return (
    <Switch>
      {/* prettier-ignore */}
      <ErrorBoundaryRoute path={`${match.url}/matching-rate`} component={MatchingRateConfiguration} />
    </Switch>
  );
};

export default Routes;
