import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useResolvedPath } from 'react-router-dom';
import { Badge, Button, FormGroup, Input, Label, Table } from 'reactstrap';
import { getSortState, JhiItemCount, JhiPagination } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { getEntitiesFilter } from './market-leader-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import MarketLeaderSummaryInfo from 'app/custom/entities/portfolio/market-leader-summary-info/market-leader-summary-info';
import { IAccount } from 'app/custom/model/account.model';

const status = ['ACTIVATED', 'DEACTIVATED'];
let roles = ['SUPER ADMINISTRATOR', 'ADMINISTRATOR', 'BROKER', 'MARKET_LEADER'];

export const MarketLeaderManagementComponent = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [pagination, setPagination] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );
  const portfolioRootPath = useResolvedPath('/admin/portfolio-management');

  const handlePagination = currentPage =>
    setPagination({
      ...pagination,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    getEntitiesFilter(paramQuery);
  };

  const account = useAppSelector(state => state.authentication.account);
  const [userSelected, setUserSelected] = useState<IAccount>(null)
  const users = useAppSelector(state => state.userManagement.users);
  const totalItems = useAppSelector(state => state.userManagement.totalItems);
  const loading = useAppSelector(state => state.userManagement.loading);
  const [windowSize, setWindowSize] = useState({
    width: undefined,
    height: undefined,
  });
  const [paramQuery, setParamQuery] = useState('');

  const [filterData, setFilterData] = useState({
    roles: 'MARKET_LEADER',
    status: localStorage.getItem('filterStatus') ? localStorage.getItem('filterStatus') : '',
    fullName: localStorage.getItem('filterFullName') ? localStorage.getItem('filterFullName') : '',
  });
  const searchParams = new URLSearchParams(history.state.usr);
  useEffect(() => {
    function handleResize() {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    }

    setParamQuery(searchParams.toString());
    window.addEventListener('resize', handleResize);
    handleResize();
    const paramArray = [];
    let paramString = ``;
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
    dispatch(getEntitiesFilter(paramString));
    return () => window.removeEventListener('resize', handleResize);
  }, [filterData, pagination.activePage, pagination.order, pagination.sort]);

  const onStatusChange = e => {
    setFilterData({ ...filterData, status: e.target.value });
    localStorage.setItem('filterStatus', e.target.value);
  };
  const onRolesChange = e => {
    setFilterData({ ...filterData, roles: e.target.value });
    localStorage.setItem('filterRoles', e.target.value);
  };
  const onFullNameChange = e => {
    setFilterData({ ...filterData, fullName: e.target.value });
    localStorage.setItem('filterFullName', e.target.value);
  };

  window.addEventListener('beforeunload', e => {
    e.preventDefault();
    if (window.localStorage.filterStatus) {
      localStorage.removeItem('filterStatus');
    }
    if (window.localStorage.filterRoles) {
      localStorage.removeItem('filterRoles');
    }
    if (window.localStorage.filterFullName) {
      localStorage.removeItem('filterFullName');
    }
    history.replaceState(null, '', window.location.href);
    return '';
  });

  if (account.authorities.includes('SUPER_ADMINISTRATOR')) {
    roles = ['ADMINISTRATOR', 'BROKER', 'MARKET_LEADER'];
  } else {
    roles = ['BROKER', 'MARKET_LEADER'];
  }

  const [showMLSummary, setShowMLSummary] = useState(false);
  const showCurrentPortfolio = user => {
    setShowMLSummary(true);
    setUserSelected(user);
    navigate(`${portfolioRootPath.pathname}/summary-info`, {
      replace: true,
    });
  };

  const backToList = () => {
    setShowMLSummary(false);
    navigate(`/admin/portfolio-management/market-leaders`, {
      replace: true,
    });
  };

  return (
    <div>
      {showMLSummary ? (
        <div className="end-elements">
          <MarketLeaderSummaryInfo userSelected={userSelected} backToList={backToList}></MarketLeaderSummaryInfo>
        </div>
      ) : (
        <div className="container-fluid">
          <h2 id="user-management-page-heading" data-cy="userManagementPageHeading">
            <div className="d-flex justify-content-end">
              <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
                <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
              </Button>
              <Link
                to="/admin/invite-user/new"
                className="btn btn-primary jh-create-entity"
                id="jh-create-entity"
                data-cy="entityCreateButton"
              >
                <FontAwesomeIcon icon="plus" />
                &nbsp; Invite User
              </Link>
            </div>
            <div className="filter-group">
              <div className="form-inline">
                <FormGroup>
                  <span>
                    <Label className="mr-2" for="roles">
                      Roles
                    </Label>
                  </span>
                  <span>
                    <Input
                      bsSize="sm"
                      onChange={onRolesChange}
                      type="select"
                      name="roles"
                      id="roles"
                      defaultValue={searchParams.get('roles')}
                      disabled
                    >
                      <option value="">MARKET_LEADER</option>
                    </Input>
                  </span>
                </FormGroup>
                <FormGroup>
                  <Label className="mr-2" for="status">
                    Status
                  </Label>
                  <Input
                    bsSize="sm"
                    onChange={onStatusChange}
                    type="select"
                    name="status"
                    id="status"
                    defaultValue={searchParams.get('status')}
                  >
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
                  <Input
                    bsSize="sm"
                    onChange={onFullNameChange}
                    defaultValue={searchParams.get('fullName')}
                    type="text"
                    name="fullName"
                    id="fullName"
                  ></Input>
                </FormGroup>
              </div>
            </div>
          </h2>
          <Table responsive striped>
            {users && users.length > 0 ? (
              <>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Full Name</th>
                    <th>Email</th>
                    <th>Roles</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {users != null
                    ? users
                        .filter(user => user.authorities && user.authorities.includes('MARKET_LEADER'))
                        .map((user, i) => (
                          <tr id={user.login} key={`user-${i}`}>
                            <td>
                              <Button tag={Link} to={user.login} color="link" size="sm">
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
                              <Button color={user.activated ? 'success' : 'danger'}>
                                <span id="status">{user.activated ? 'Activated' : 'Deactivated'}</span>
                              </Button>
                            </td>
                            <td className="text-end">
                              <div className="btn-group flex-btn-group-container">
                                <Button onClick={() => showCurrentPortfolio(user)} color="info" size="sm">
                                  <div>
                                    <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                                  </div>
                                </Button>
                              </div>
                            </td>
                          </tr>
                        ))
                    : null}
                </tbody>
              </>
            ) : (
              !loading && <div className="alert alert-warning">No User found</div>
            )}
          </Table>
          {totalItems ? (
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
      )}
    </div>
  );
};

export default MarketLeaderManagementComponent;
