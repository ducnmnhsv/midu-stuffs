import { ReducersMapObject, combineReducers } from '@reduxjs/toolkit';
import { loadingBarReducer as loadingBar } from 'react-redux-loading-bar';

import authentication from './authentication';
import applicationProfile from './application-profile';

import administration from 'app/modules/administration/administration.reducer';
import userManagement from 'app/modules/administration/user-management/user-management.reducer';
import register from 'app/modules/account/register/register.reducer';
import activate from 'app/modules/account/activate/activate.reducer';
import password from 'app/modules/account/password/password.reducer';
import settings from 'app/modules/account/settings/settings.reducer';
import passwordReset from 'app/modules/account/password-reset/password-reset.reducer';
import chatRoom from 'app/modules/administration/chat-room/chat-room.reducer';
import inviteUser from 'app/modules/administration/invite-user/invite-user.reducer';
import createdChatRoom from 'app/modules/administration/my-chat-room/my-chat-room.reducer';
import portfolioReducer from 'app/custom/entities/portfolio/handle/redux/portfolio-reducer';
import latestJobResultReducer from 'app/custom/entities/market-history-job-result/market-history-job-result.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const rootReducer: ReducersMapObject = {
  authentication,
  applicationProfile,
  administration,
  userManagement,
  register,
  activate,
  passwordReset,
  password,
  settings,
  loadingBar,
  chatRoom,
  inviteUser,
  createdChatRoom,
  portfolioReducer,
  latestJobResultReducer
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default rootReducer;
