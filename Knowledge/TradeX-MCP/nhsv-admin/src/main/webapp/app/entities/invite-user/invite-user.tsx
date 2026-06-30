import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat, getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IInviteUser } from 'app/shared/model/invite-user.model';
import { getEntities } from './invite-user.reducer';

export const InviteUser = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const inviteUserList = useAppSelector(state => state.inviteUser.entities);
  const loading = useAppSelector(state => state.inviteUser.loading);
  const totalItems = useAppSelector(state => state.inviteUser.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      })
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [location.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  return (
    <div>
      <h2 id="invite-user-heading" data-cy="InviteUserHeading">
        Invite Users
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/invite-user/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Invite User
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {inviteUserList && inviteUserList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('login')}>
                  Login <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('email')}>
                  Email <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  Created At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('updatedAt')}>
                  Updated At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('createdId')}>
                  Created Id <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('createdBy')}>
                  Created By <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('activationKey')}>
                  Activation Key <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('activationDate')}>
                  Activation Date <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('langKey')}>
                  Lang Key <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('authorities')}>
                  Authorities <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {inviteUserList.map((inviteUser, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/invite-user/${inviteUser.id}`} color="link" size="sm">
                      {inviteUser.id}
                    </Button>
                  </td>
                  <td>{inviteUser.login}</td>
                  <td>{inviteUser.email}</td>
                  <td>{inviteUser.status}</td>
                  <td>{inviteUser.createdAt ? <TextFormat type="date" value={inviteUser.createdAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{inviteUser.updatedAt ? <TextFormat type="date" value={inviteUser.updatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{inviteUser.createdId}</td>
                  <td>{inviteUser.createdBy}</td>
                  <td>{inviteUser.activationKey}</td>
                  <td>
                    {inviteUser.activationDate ? (
                      <TextFormat type="date" value={inviteUser.activationDate} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{inviteUser.langKey}</td>
                  <td>{inviteUser.authorities}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/invite-user/${inviteUser.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/invite-user/${inviteUser.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/invite-user/${inviteUser.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
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
      {totalItems ? (
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
