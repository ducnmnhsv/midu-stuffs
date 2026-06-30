import React, { CSSProperties } from 'react';
import { IAccount } from 'app/custom/model/account.model';

const AccountInfoComponent = props => {
  const accountInfo: IAccount = props.accountInfo;

  return (
    <div className="p-0">
      {accountInfo ? (
        <div className="bordered-div flex-container">
          <h4>Introduction</h4>
          <pre style={pMlIntro}>{accountInfo.introduction}</pre>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

const pMlIntro: CSSProperties = {
  width: '100%',
  margin: 0,
  whiteSpace: 'pre-wrap',
  overflow: 'hidden',
};

export default AccountInfoComponent;
