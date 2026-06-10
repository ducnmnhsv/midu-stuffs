import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import EKycBankList from './e-kyc-bank-list';
import EKycBankListDetail from './e-kyc-bank-list-detail';
import EKycBankListUpdate from './e-kyc-bank-list-update';
import EKycBankListDeleteDialog from './e-kyc-bank-list-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={EKycBankListUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={EKycBankListUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={EKycBankListDetail} />
      <ErrorBoundaryRoute path={match.url} component={EKycBankList} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={EKycBankListDeleteDialog} />
  </>
);

export default Routes;
