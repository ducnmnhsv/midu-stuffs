import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, FormGroup, Input, Label, Table } from 'reactstrap';
import { getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntitiesFilter } from './chat-room.reducer';
import { StatusEnum } from 'app/shared/model/enumerations/status-enum.model';
import { getSession } from 'app/shared/reducers/authentication';

const status = [StatusEnum.PENDING, StatusEnum.APPROVED, StatusEnum.REJECTED]

export const ChatRoom = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const chatRoomList = useAppSelector(state => state.chatRoom.entities);
  const loading = useAppSelector(state => state.chatRoom.loading);
  const totalItems = useAppSelector(state => state.chatRoom.totalItems);

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    getAllEntities();
  };
  const getAllEntities = () => {
    dispatch(
      getEntitiesFilter(paramQuery)
    );
  };

  const [paramQuery, setParamQuery] = useState('');
  const currentDate = new Date().toJSON().slice(0, 10).replace(/-/g, '-');

  const [filterData, setFilterData] = useState({
    fromDate: currentDate,
    toDate: currentDate,
    status: '',
    fullName: ''
  });

  const onStatusChange = e => {
    setFilterData({ ...filterData, status: e.target.value });
  };
  const onFullNameChange = e => {
    setFilterData({ ...filterData, fullName: e.target.value });
  };

  window.addEventListener('beforeunload', e => {
    e.preventDefault();
    history.replaceState(null, '', window.location.href);
    return '';
  });

  function formatDate(date = new Date()) {
    const year = date.toLocaleString('default', { year: 'numeric' });
    const month = date.toLocaleString('default', { month: '2-digit' });
    const day = date.toLocaleString('default', { day: '2-digit' });
    return [year, month, day].join('-');
  }

  useEffect(() => {
    dispatch(getSession());
    if (searchParams.get('page')) {
      setPaginationState({
        ...paginationState,
        activePage: +searchParams.get('page') + 1,
      });
      const fromDate = formatDate(new Date(searchParams.get('updatedAt.greaterThanOrEqual')));
      const toDate = formatDate(new Date(searchParams.get('updatedAt.lessThanOrEqual')));
      const realToDate = new Date(toDate);
      realToDate.setDate(realToDate.getDate() - 1);
      setFilterData({
        ...filterData,
        fromDate: fromDate,
        toDate: formatDate(realToDate),
        status: searchParams.get('status.equals') || '',
        fullName: searchParams.get('search.contains') || ''
      });
      setParamQuery("?" + searchParams.toString());
      dispatch(
        getEntitiesFilter("?" + searchParams.toString())
      );
      history.state.usr = '';
    } else {
      const paramArray = [];
      let paramString = ``;
      if (filterData.fromDate) {
        paramArray.push('updatedAt.greaterThanOrEqual=' + new Date(filterData.fromDate).toISOString());
      }
      if (filterData.toDate) {
        const nextDate = new Date(filterData.toDate);
        nextDate.setDate(nextDate.getDate() + 1);
        paramArray.push('updatedAt.lessThanOrEqual=' + nextDate.toISOString());
      }
      if (filterData.status) {
        paramArray.push('status.equals=' + filterData.status);
      }
      if (filterData.fullName) {
        paramArray.push('search.contains=' + filterData.fullName);
      }
      paramArray.push(`page=${paginationState.activePage - 1}`);
      paramArray.push(`size=${paginationState.itemsPerPage}`);
      if (paramArray.length > 0) {
        for (let i = 0; i < paramArray.length; i++) {
          if (i === 0) {
            paramString += `?${paramArray[i]}`;
          } else {
            if (!paramArray[i].includes('|')) {
              paramString += `&${paramArray[i]}`;
            }
          }
        }
      }
      setParamQuery(paramString);
      dispatch(
        getEntitiesFilter(paramString)
      );
    }
  }, [filterData, paginationState.activePage]);

  const onFromDateChange = e => {
    setFilterData({
      ...filterData,
      fromDate: e.target.value,
      toDate: new Date(e.target.value) > new Date(filterData.toDate) ? e.target.value : filterData.toDate,
    });
  };

  const onToDateChange = e => {
    setFilterData({
      ...filterData,
      toDate: e.target.value,
      fromDate: new Date(e.target.value) < new Date(filterData.fromDate) ? e.target.value : filterData.fromDate,
    });
  };

  const searchParams = new URLSearchParams(history.state.usr);

  const handleView = login => () => {
    navigate(`${login}`, { state: paramQuery });
  }

  const handleApprove = login => () => {
    navigate(`/admin/chat-room/${login}/approve`, { state: paramQuery });
  }

  const handleReject = login => () => {
    navigate(`/admin/chat-room/${login}/reject`, { state: paramQuery });
  }

  const handleClearFilter = () => {
    setFilterData({
      fromDate: currentDate,
      toDate: currentDate,
      status: '',
      fullName: ''
    });
  }

  return (
    <div>
      <h2 id="chat-room-heading" data-cy="ChatRoomHeading">
        Chat Room Management
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="secondary" onClick={handleClearFilter} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Clear Filter
          </Button>
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh List
          </Button>
        </div>
        <div className="filter-group">
          <div className='form-inline'>
            <FormGroup>
              <Label className="mr-2" for="fromDate">
                From Date
              </Label>
              <Input
                onChange={onFromDateChange}
                value={searchParams.get("updatedAt.greaterThanOrEqual") || filterData.fromDate}
                max={currentDate}
                type="date"
                name="fromDate"
                id="fromDate"
                placeholder="date placeholder"
                bsSize="sm"
              />
            </FormGroup>
            <FormGroup>
              <Label className="mr-2" for="toDate">
                To date
              </Label>
              <Input
                onChange={onToDateChange}
                value={searchParams.get("updatedAt.lessThanOrEqual") || filterData.toDate}
                max={currentDate}
                type="date"
                name="toDate"
                id="toDate"
                placeholder="date placeholder"
                bsSize="sm"
              />
            </FormGroup>
            <FormGroup>
              <Label className="mr-2" for="status">
                Status
              </Label>
              <Input bsSize="sm" onChange={onStatusChange} value={filterData.status} defaultValue={searchParams.get("status.equals")} placeholder="Status" type="select" name="status" id="status">
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
                Search
              </Label>
              <Input bsSize="sm" onChange={onFullNameChange} value={filterData.fullName} defaultValue={searchParams.get("groupName.contains")} placeholder="" type="text" name="fullName" id="fullName">
              </Input>
            </FormGroup>
          </div>
        </div>
      </h2>
      <div className="table-responsive">
        {loading ? <p>Loading...</p> : null}
        {!loading && chatRoomList && chatRoomList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  ID
                </th>
                <th>
                  Group Name
                </th>
                <th>
                  Group Owner
                </th>
                <th>
                  Broker Contact
                </th>
                <th>
                  Status
                </th>
                <th>
                  Action
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {chatRoomList.map((chatRoom, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button onClick={handleView(chatRoom.id)}
                      color="link" size="sm" data-cy="entityDetailsButton">
                      {chatRoom.id}
                    </Button>
                  </td>
                  <td>{chatRoom.groupName}</td>
                  <td>{chatRoom.groupOwner}</td>
                  <td>{chatRoom.brokerContact}</td>
                  <td>{chatRoom.status}</td>
                  <td>{chatRoom.action}</td>
                  <td className="text-center">
                    <div className="btn-group flex-btn-group-container">
                      <Button onClick={handleView(chatRoom.id)}
                        color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        onClick={handleApprove(chatRoom.id)}
                        color="danger"
                        size="sm"
                        disabled={chatRoom.status === StatusEnum.REJECTED || chatRoom.status === StatusEnum.APPROVED}  >
                        <FontAwesomeIcon icon="plus" /> <span className="d-none d-md-inline">Approve</span>
                      </Button>
                      <Button
                        onClick={handleReject(chatRoom.id)}
                        color="danger"
                        size="sm"
                        disabled={chatRoom.status === StatusEnum.REJECTED || chatRoom.status === StatusEnum.APPROVED}  >
                        <FontAwesomeIcon icon="ban" /> <span className="d-none d-md-inline">Reject</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Chat Rooms found</div>
        )}
      </div>
      {!loading && totalItems ? (
        <div className={chatRoomList && chatRoomList.length > 0 ? '' : 'd-none'}>
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

export default ChatRoom;
