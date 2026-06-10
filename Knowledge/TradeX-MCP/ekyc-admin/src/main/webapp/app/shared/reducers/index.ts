import { combineReducers } from 'redux';
import { loadingBarReducer as loadingBar } from 'react-redux-loading-bar';

import locale, { LocaleState } from './locale';
import authentication, { AuthenticationState } from './authentication';
import applicationProfile, { ApplicationProfileState } from './application-profile';

import administration, { AdministrationState } from 'app/modules/administration/administration.reducer';
import userManagement, { UserManagementState } from 'app/modules/administration/user-management/user-management.reducer';
import register, { RegisterState } from 'app/modules/account/register/register.reducer';
import activate, { ActivateState } from 'app/modules/account/activate/activate.reducer';
import password, { PasswordState } from 'app/modules/account/password/password.reducer';
import settings, { SettingsState } from 'app/modules/account/settings/settings.reducer';
import passwordReset, { PasswordResetState } from 'app/modules/account/password-reset/password-reset.reducer';
// prettier-ignore
import eKyc, {
  EKycState
} from 'app/entities/e-kyc/e-kyc.reducer';
// prettier-ignore
import eKycCreatorStatus, {
  EKycCreatorStatusState
} from 'app/entities/e-kyc-creator-status/e-kyc-creator-status.reducer';
// prettier-ignore
import ttlIssuePlaceCodeMap, {
  TtlIssuePlaceCodeMapState
} from 'app/entities/ttl-issue-place-code-map/ttl-issue-place-code-map.reducer';
// prettier-ignore
import eKycExt, {
  EKycExtState
} from 'app/entities/e-kyc-ext/e-kyc-ext.reducer';
// prettier-ignore
// prettier-ignore
// prettier-ignore
// prettier-ignore
// prettier-ignore
// prettier-ignore
// prettier-ignore
import eKycBankList, {
  EKycBankListState
} from 'app/entities/e-kyc-bank-list/e-kyc-bank-list.reducer';
// prettier-ignore
import eKycAdditionalInfo, {
  EKycAdditionalInfoState
} from 'app/entities/e-kyc-additional-info/e-kyc-additional-info.reducer';
// prettier-ignore
import publicCoop, {
  PublicCoopState
} from 'app/entities/public-coop/public-coop.reducer';
// prettier-ignore
import blockholder, {
  BlockholderState
} from 'app/entities/blockholder/blockholder.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

export interface IRootState {
  readonly authentication: AuthenticationState;
  readonly locale: LocaleState;
  readonly applicationProfile: ApplicationProfileState;
  readonly administration: AdministrationState;
  readonly userManagement: UserManagementState;
  readonly register: RegisterState;
  readonly activate: ActivateState;
  readonly passwordReset: PasswordResetState;
  readonly password: PasswordState;
  readonly settings: SettingsState;
  readonly eKyc: EKycState;
  readonly eKycCreatorStatus: EKycCreatorStatusState;
  readonly ttlIssuePlaceCodeMap: TtlIssuePlaceCodeMapState;
  readonly eKycExt: EKycExtState;
  readonly eKycBankList: EKycBankListState;
  readonly eKycAdditionalInfo: EKycAdditionalInfoState;
  readonly publicCoop: PublicCoopState;
  readonly blockholder: BlockholderState;
  /* jhipster-needle-add-reducer-type - JHipster will add reducer type here */
  readonly loadingBar: any;
}

const rootReducer = combineReducers<IRootState>({
  authentication,
  locale,
  applicationProfile,
  administration,
  userManagement,
  register,
  activate,
  passwordReset,
  password,
  settings,
  eKyc,
  eKycCreatorStatus,
  ttlIssuePlaceCodeMap,
  eKycExt,
  eKycBankList,
  eKycAdditionalInfo,
  publicCoop,
  blockholder,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
  loadingBar,
});

export default rootReducer;
