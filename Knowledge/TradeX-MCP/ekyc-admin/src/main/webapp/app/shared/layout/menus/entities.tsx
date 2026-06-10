import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { Translate, translate } from 'react-jhipster';
import { NavDropdown } from './menu-components';

export const EntitiesMenu = props => (
  <NavDropdown
    icon="th-list"
    name={translate('global.menu.entities.main')}
    id="entity-menu"
    data-cy="entity"
    style={{ maxHeight: '80vh', overflow: 'auto' }}
  >
    <MenuItem icon="asterisk" to="/e-kyc">
      <Translate contentKey="global.menu.entities.eKyc" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc-creator-status">
      <Translate contentKey="global.menu.entities.eKycCreatorStatus" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/ttl-issue-place-code-map">
      <Translate contentKey="global.menu.entities.ttlIssuePlaceCodeMap" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc-ext">
      <Translate contentKey="global.menu.entities.eKycExt" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc-bank-list">
      <Translate contentKey="global.menu.entities.eKycBankList" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc-additional-info">
      <Translate contentKey="global.menu.entities.eKycAdditionalInfo" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/public-coop">
      <Translate contentKey="global.menu.entities.publicCoop" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/blockholder">
      <Translate contentKey="global.menu.entities.blockholder" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-contract-info">
      <Translate contentKey="global.menu.entities.eContractInfo" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc">
      <Translate contentKey="global.menu.entities.eKyc" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc-creator-status">
      <Translate contentKey="global.menu.entities.eKycCreatorStatus" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/ttl-issue-place-code-map">
      <Translate contentKey="global.menu.entities.ttlIssuePlaceCodeMap" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-kyc-ext">
      <Translate contentKey="global.menu.entities.eKycExt" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/e-contract">
      <Translate contentKey="global.menu.entities.eContract" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/matching-rate">
      <Translate contentKey="global.menu.entities.matchingRate" />
    </MenuItem>
    {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
  </NavDropdown>
);
