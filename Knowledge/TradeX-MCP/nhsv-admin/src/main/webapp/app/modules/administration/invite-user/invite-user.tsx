import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Badge, Button, FormGroup, Input, Label, Table } from 'reactstrap';
import { getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntitiesFilter, resendInvite, reset } from './invite-user.reducer';
import { InviteStatusEnum } from 'app/shared/model/enumerations/invite-status-enum.model';
import { getSession } from 'app/shared/reducers/authentication';

const status = ['PENDING', 'EXPIRED', 'ACCOUNT DEACTIVATED', 'ACCOUNT CREATED'];
let roles = ['ADMINISTRATOR', 'BROKER', 'MARKET LEADER'];

export const InviteUser = () => {
  const dispatch = useAppDispatch();
  const location = useLocation();
  const navigate = useNavigate();
  const inviteUserList = useAppSelector(state => state.inviteUser.entities);
  const loading = useAppSelector(state => state.inviteUser.loading);
  const totalItems = useAppSelector(state => state.inviteUser.totalItems);
  const updating = useAppSelector(state => state.inviteUser.updating);
  const [paramQuery, setParamQuery] = useState('');
  const [filterData, setFilterData] = useState({
    roles: '',
    status: '',
    fullName: ''
  });
  const searchParams = new URLSearchParams(history.state.usr);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  useEffect(() => {
    dispatch(getSession());
    let paramString = ``;
    if (searchParams.get('page')) {
      setPaginationState({
        ...paginationState,
        activePage: +searchParams.get('page') + 1,
      });
      setFilterData({
        ...filterData,
        status: searchParams.get('status.equals'),
        roles: searchParams.get('authorities.contains'),
        fullName: searchParams.get('search.contains')
      });
      setParamQuery("?" + searchParams.toString());
      paramString = "?" + searchParams.toString();
      history.state.usr = '';
    } else {
      const paramArray = [];
      if (filterData.status) {
        paramArray.push(`status.equals=${filterData.status}`);
      }
      if (filterData.roles) {
        paramArray.push(`authorities.contains=${filterData.roles}`);
      }
      if (filterData.fullName) {
        paramArray.push(`search.contains=${filterData.fullName}`);
      }
      paramArray.push(`page=${paginationState.activePage - 1}`);
      paramArray.push(`size=${paginationState.itemsPerPage}`);
      if (paramArray.length > 0) {
        for (let i = 0; i < paramArray.length; i++) {
          if (i === 0) {
            paramString += `?${paramArray[i]}`;
          } else
            paramString += `&${paramArray[i]}`;
        }
      }
      setParamQuery(paramString);
    }
    dispatch(
      getEntitiesFilter(paramString)
    );
  }, [filterData, paginationState.activePage]);

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    }
    );

  const handleSyncList = () => {
    dispatch(getEntitiesFilter(paramQuery));
  };

  const handleResend = (id: number) => {
    dispatch(resendInvite(id));
  }

  const handleView = login => () => {
    navigate(`${login}`, { state: paramQuery });
  }

  const handleCreate = () => {
    let param = '';
    param = paramQuery + `&invite=true`;
    navigate(`/admin/invite-user/new`, { state: param });
  }

  window.addEventListener('beforeunload', e => {
    e.preventDefault();
    history.replaceState(null, '', window.location.href);
    return '';
  });

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


  return (
    <div>
      <h2 id="invite-user-heading" data-cy="InviteUserHeading">
        User Invitations
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
                  defaultValue={searchParams.get("authorities.contains")}
                  value={filterData.roles}>
                  <option value="">ALL</option>
                  {roles.map((value, key) => (
                    <option key={key} value={value.replace(" ","_")}>
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
                defaultValue={searchParams.get("status.equals")}
                value={filterData.status}>
                <option value="">ALL</option>
                {status.map((value, key) => (
                  <option key={key} value={value.replace(" ", "_")}>
                    {value}
                  </option>
                ))}
              </Input>
            </FormGroup>
            <FormGroup>
              <Label className="mr-2" for="fullName">
                Search
              </Label>
              <Input bsSize="sm" onChange={onFullNameChange}
                defaultValue={searchParams.get('search.contains')}
                type="text" name="fullName" id="fullName"
                value={filterData.fullName}>
              </Input>
            </FormGroup>
          </div>
        </div>
      </h2>
      <div className="table-responsive">
        {loading ? <p>Loading...</p> : null}
        {!loading && inviteUserList && inviteUserList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  ID
                </th>
                <th>
                  Username
                </th>
                <th>
                  Email
                </th>
                <th>
                  Roles
                </th>
                <th>
                  Status
                </th>
                <th>
                  Created Id
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {inviteUserList.map((inviteUser, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button onClick={handleView(inviteUser.id)} color="link" size="sm">
                      {inviteUser.id}
                    </Button>
                  </td>
                  <td>{inviteUser.login}</td>
                  <td>{inviteUser.email}</td>
                  <td>{inviteUser.authorities
                    ? inviteUser.authorities.split(',').map((authority, j) => (
                      <div key={`user-auth-${i}-${j}`}>
                        <Badge color="info">{authority.replace("_"," ")}</Badge>
                      </div>
                    ))
                    : null}
                  </td>
                  <td>{inviteUser.status.replace("_", " ")}</td>
                  <td>{inviteUser.createdId ? inviteUser.createdId : ''}</td>
                  <td className="text-center">
                    <div className="btn-group flex-btn-group-container">
                      <Button onClick={handleView(inviteUser.id)} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        onClick={() => handleResend(inviteUser.id)}
                        color="primary"
                        size="sm"
                        disabled={inviteUser.status === InviteStatusEnum.ACCOUNT_CREATED || updating}
                      >
                        <FontAwesomeIcon icon="sync" spin={updating} /> Resend
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Invite Users found</div>
        )}
      </div>
      {!loading && totalItems ? (
        <div className={inviteUserList && inviteUserList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
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

export default InviteUser;
