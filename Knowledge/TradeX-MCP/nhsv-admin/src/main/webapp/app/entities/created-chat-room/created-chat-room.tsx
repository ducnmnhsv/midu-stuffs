import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat, getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ICreatedChatRoom } from 'app/shared/model/created-chat-room.model';
import { getEntities } from './created-chat-room.reducer';

export const CreatedChatRoom = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const createdChatRoomList = useAppSelector(state => state.createdChatRoom.entities);
  const loading = useAppSelector(state => state.createdChatRoom.loading);
  const totalItems = useAppSelector(state => state.createdChatRoom.totalItems);

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
      <h2 id="created-chat-room-heading" data-cy="CreatedChatRoomHeading">
        Created Chat Rooms
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/created-chat-room/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Created Chat Room
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {createdChatRoomList && createdChatRoomList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('groupName')}>
                  Group Name <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('groupOwner')}>
                  Group Owner <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('introduction')}>
                  Introduction <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('photo')}>
                  Photo <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('brokerName')}>
                  Broker Name <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('brokerContact')}>
                  Broker Contact <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('createdBy')}>
                  Created By <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  Created At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('updatedAt')}>
                  Updated At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('approvedAt')}>
                  Approved At <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('approvedBy')}>
                  Approved By <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('rejectReason')}>
                  Reject Reason <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('totalView')}>
                  Total View <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {createdChatRoomList.map((createdChatRoom, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/created-chat-room/${createdChatRoom.id}`} color="link" size="sm">
                      {createdChatRoom.id}
                    </Button>
                  </td>
                  <td>{createdChatRoom.groupName}</td>
                  <td>{createdChatRoom.groupOwner}</td>
                  <td>{createdChatRoom.introduction}</td>
                  <td>{createdChatRoom.photo}</td>
                  <td>{createdChatRoom.brokerName}</td>
                  <td>{createdChatRoom.brokerContact}</td>
                  <td>{createdChatRoom.status}</td>
                  <td>{createdChatRoom.createdBy}</td>
                  <td>
                    {createdChatRoom.createdAt ? (
                      <TextFormat type="date" value={createdChatRoom.createdAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {createdChatRoom.updatedAt ? (
                      <TextFormat type="date" value={createdChatRoom.updatedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {createdChatRoom.approvedAt ? (
                      <TextFormat type="date" value={createdChatRoom.approvedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{createdChatRoom.approvedBy}</td>
                  <td>{createdChatRoom.rejectReason}</td>
                  <td>{createdChatRoom.totalView}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/created-chat-room/${createdChatRoom.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/created-chat-room/${createdChatRoom.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/created-chat-room/${createdChatRoom.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
          !loading && <div className="alert alert-warning">No Created Chat Rooms found</div>
        )}
      </div>
      {totalItems ? (
        <div className={createdChatRoomList && createdChatRoomList.length > 0 ? '' : 'd-none'}>
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

export default CreatedChatRoom;
