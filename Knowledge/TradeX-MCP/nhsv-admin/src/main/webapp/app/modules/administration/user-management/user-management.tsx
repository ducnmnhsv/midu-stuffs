import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table, Badge, FormGroup, Input, Label } from 'reactstrap';
import { JhiPagination, JhiItemCount, getSortState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { getEntitiesFilter } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';

const status = ['ACTIVATED', 'DEACTIVATED'];
let roles = ['SUPER ADMINISTRATOR', 'ADMINISTRATOR', 'BROKER', 'MARKET_LEADER'];

export const UserManagement = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const account = useAppSelector(state => state.authentication.account);
  const users = useAppSelector(state => state.userManagement.users);
  const totalItems = useAppSelector(state => state.userManagement.totalItems);
  const loading = useAppSelector(state => state.userManagement.loading);
  const [paramQuery, setParamQuery] = useState('');
  const [filterData, setFilterData] = useState({
    roles: '',
    status: '',
    fullName: ''
  });
  const searchParams = new URLSearchParams(history.state.usr);

  const [pagination, setPagination] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const handlePagination = currentPage =>
    setPagination({
      ...pagination,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    dispatch(getEntitiesFilter(paramQuery));
  };

  useEffect(() => {
    dispatch(getSession());
    let paramString = ``;
    if (searchParams.get('page')) {
      setPagination({
        ...pagination,
        activePage: +searchParams.get('page') + 1,
      });
      setFilterData({
        ...filterData,
        status: searchParams.get('status'),
        roles: searchParams.get('roles'),
        fullName: searchParams.get('fullName')
      });
      setParamQuery("?" + searchParams.toString());
      paramString = "?" + searchParams.toString();
      history.state.usr = '';
    } else {
      const paramArray = [];
      if (filterData.status) {
        paramArray.push(`status=${filterData.status}`);
      }
      if (filterData.roles) {
        paramArray.push(`roles=${filterData.roles}`);
      }
      if (filterData.fullName) {
        paramArray.push(`fullName=${filterData.fullName}`);
      }
      paramArray.push(`page=${pagination.activePage - 1}`);
      paramArray.push(`size=${pagination.itemsPerPage}`);
      if (paramArray.length > 0) {
        for (let i = 0; i < paramArray.length; i++) {
          if (i === 0) {
            paramString += `?${paramArray[i]}`;
          } else {
            paramString += `&${paramArray[i]}`;
          }
        }
      }
      setParamQuery(paramString);
    }
    dispatch(
      getEntitiesFilter(paramString)
    );
  }, [filterData, pagination.activePage]);

  const onStatusChange = e => {
    setFilterData({ ...filterData, status: e.target.value });
  };

  const onRolesChange = e => {
    setFilterData({ ...filterData, roles: e.target.value });
  };

  const onFullNameChange = e => {
    setFilterData({ ...filterData, fullName: e.target.value });
  };

  const handleClearFilter = () => {
    setFilterData({
      roles: '',
      status: '',
      fullName: ''
    });
  }

  window.addEventListener('beforeunload', e => {
    history.replaceState(null, '', window.location.href);
    return '';
  });

  if (account.authorities.includes('SUPER_ADMINISTRATOR')) {
    roles = ['ADMINISTRATOR', 'BROKER', 'MARKET_LEADER'];
  } else {
    roles = ['BROKER', 'MARKET_LEADER'];
  }

  const handleDeactivate = login => () => {
    navigate(`${login}/deactivate`, { state: paramQuery });
  }

  const handleEdit = login => () => {
    navigate(`${login}/edit`, { state: paramQuery });
  }

  const handleView = login => () => {
    navigate(`${login}`, { state: paramQuery });
  }

  const handleCreate = () => {
    navigate(`/admin/invite-user/new`, { state: paramQuery });
  }

  return (
    <div>
      <h2 id="user-management-page-heading" data-cy="userManagementPageHeading">
        User Management
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="secondary" onClick={handleClearFilter} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Clear Filter
          </Button>
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh List
          </Button>
          <Button className="me-2" color="primary" onClick={handleCreate}>
            <FontAwesomeIcon icon="plus" /> Invite User
          </Button>
        </div>
        <div className="filter-group">
          <div className='form-inline'>
            <FormGroup>
              <span>
                <Label className="mr-2" for="roles">
                  Roles
                </Label>
              </span>
              <span>
                <Input bsSize="sm" onChange={onRolesChange}
                  type="select" name="roles" id="roles"
                  defaultValue={searchParams.get("roles")}
                  value={filterData.roles}>
                  <option value="">ALL</option>
                  {roles.map((value, key) => (
                    <option key={key} value={value}>
                      {value}
                    </option>
                  ))}
                </Input>
              </span>

            </FormGroup>
            <FormGroup>
              <Label className="mr-2" for="status">
                Status
              </Label>
              <Input bsSize="sm" onChange={onStatusChange}
                type="select" name="status" id="status"
                defaultValue={searchParams.get("status")}
                value={filterData.status}>
                <option value="">ALL</option>
                {status.map((value, key) => (
                  <option key={key} value={value}>
                    {value}
                  </option>
                ))}
              </Input>
            </FormGroup>
            <FormGroup>
              <Label className="mr-2" for="fullName">
                Full Name
              </Label>
              <Input bsSize="sm" onChange={onFullNameChange}
                defaultValue={searchParams.get('fullName')}
                value={filterData.fullName}
                type="text" name="fullName" id="fullName">
              </Input>
            </FormGroup>
          </div>
        </div>
      </h2>
      <Table responsive>
        {loading ? <p>Loading...</p> : null}
        {!loading && users && users.length > 0 ? (
          <><thead>
            <tr>
              <th>
                ID
              </th>
              <th>
                Username
              </th>
              <th>
                Full Name
              </th>
              <th>
                Email
              </th>
              <th>Roles</th>
              <th>
                Status
              </th>
            </tr>
          </thead><tbody>
              {users != null ? users.map((user, i) => (
                <tr id={user.login} key={`user-${i}`}>
                  <td>
                    <Button onClick={handleView(user.login)} color="link" size="sm">
                      {user.id}
                    </Button>
                  </td>
                  <td>{user.login}</td>
                  <td>{user.fullName}</td>
                  <td>{user.email}</td>

                  <td>
                    {user.authorities
                      ? user.authorities.map((authority, j) => (
                        <div key={`user-auth-${i}-${j}`}>
                          <Badge color="info">{authority}</Badge>
                        </div>
                      ))
                      : null}
                  </td>
                  <td>
                    <Button color={user.activated ? "success" : "danger"}>
                      <span id="status">
                        {user.activated ?
                          'Activated'
                          :
                          'Deactivated'}
                      </span>
                    </Button>
                  </td>
                  <td className="text-center">
                    <div className="btn-group flex-btn-group-container">
                      <Button onClick={handleView(user.login)} color="info" size="sm">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button onClick={handleEdit(user.login)} color="primary" size="sm" disabled={!user.activated}>
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button onClick={handleDeactivate(user.login)} color="danger" size="sm" disabled={account.login === user.login || !user.activated}>
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Deactivate</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              )) : null}
            </tbody></>
        ) : (
          !loading && <div className="alert alert-warning">No User found</div>
        )}
      </Table>
      {!loading && totalItems ? (
        <div className={users && users.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={pagination.activePage} total={totalItems} itemsPerPage={pagination.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={pagination.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={pagination.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default UserManagement;
