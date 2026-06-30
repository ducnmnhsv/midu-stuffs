import React from 'react';
import './subscriber-info.scss';
import SubscriberListComponent from 'app/custom/entities/portfolio/market-leader-summary-info/component/subscriber-info/list/subscriber-list';
import { IAccount } from 'app/custom/model/account.model';

const SubscriberInfoComponent = props => {
  const accountInfo: IAccount = props.accountInfo;
  return (
    <div className="container-fluid p-0">
      <div className="row">
        <div className="col-12">
          <SubscriberListComponent accountInfo={accountInfo} />
        </div>
      </div>
    </div>
  );
};

export default SubscriberInfoComponent;
