import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { Translate, translate } from 'react-jhipster';
import { NavDropdown } from './menu-components';
import config from "app/config/constants";

export const EntitiesMenu = props => (
  <NavDropdown
    icon="th-list"
    name={translate('global.menu.entities.main')}
    id="entity-menu"
    data-cy="entity"
    style={{ maxHeight: '80vh', overflow: 'auto' }}
  >
    {config.domain === 'nhsv' ? (
      <>
        <MenuItem icon="asterisk" to="/configuration/matching-rate">
          Matching Rate Configuration
        </MenuItem>
      </>
    ) : (
      <>
        <MenuItem icon="asterisk" to="/custom-ttl-issue-place-code-map">
          <Translate contentKey="global.menu.entities.ttlIssuePlaceCodeMap" />
        </MenuItem>
      </>
    )}
    {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
  </NavDropdown>
);
