import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { NavDropdown } from './menu-components';
import './admin.scss'

const adminMenuItems = (isAdmin, isBroker, isMarketLeader) => (
  <>
    {isAdmin ? (
      <>
        <MenuItem icon="users" to="/admin/user-management">
          User Management
        </MenuItem>
        <MenuItem icon="asterisk" to="/admin/invite-user">
          User Invitations
        </MenuItem>
        <MenuItem icon="asterisk" to="/admin/chat-room">
          Chat Room Management
        </MenuItem>
        <MenuItem icon="asterisk" to="/admin/latest-job-result">
          Market History Corrector
        </MenuItem>
      </>
    ) : null}
    {isBroker ? (
      <MenuItem icon="asterisk" to="/admin/my-chat-room">
        My Chat Room Management
      </MenuItem>
    ) : null}
    {isMarketLeader || isAdmin ? (
      <MenuItem icon="asterisk" to="/admin/portfolio-management">
        Market leader's portfolio management
      </MenuItem>
    ) : null}
  </>
);

export const AdminMenu = ({ isAdmin, isBroker, isMarketLeader }) => (
  <div className="p-account-menu" style={{ }}>
    <NavDropdown className="p-account-menu"  icon="users-cog" name="Administration" id="admin-menu" data-cy="adminMenu">
      {adminMenuItems(isAdmin, isBroker, isMarketLeader)}
    </NavDropdown>
  </div>
);

const pAccountMenu = {
  position: 'absolute',
};

export default AdminMenu;
