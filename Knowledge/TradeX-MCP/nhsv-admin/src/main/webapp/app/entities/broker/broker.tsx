import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IBroker } from 'app/shared/model/broker.model';
import { getEntities } from './broker.reducer';

export const Broker = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const brokerList = useAppSelector(state => state.broker.entities);
  const loading = useAppSelector(state => state.broker.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="broker-heading" data-cy="BrokerHeading">
        Brokers
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/broker/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Broker
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {brokerList && brokerList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>Id</th>
                <th>Username</th>
                <th>Fullname</th>
                <th>Status</th>
                <th>Total Chat Room</th>
                <th>Current Rank</th>
                <th>Is Dynamic</th>
                <th>Email</th>
                <th>Created At</th>
                <th>Updated At</th>
                <th>Deactivated At</th>
                <th>Deactivated By</th>
                <th>Invited By</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {brokerList.map((broker, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/broker/${broker.id}`} color="link" size="sm">
                      {broker.id}
                    </Button>
                  </td>
                  <td>{broker.username}</td>
                  <td>{broker.fullname}</td>
                  <td>{broker.status ? 'true' : 'false'}</td>
                  <td>{broker.totalChatRoom}</td>
                  <td>{broker.currentRank}</td>
                  <td>{broker.isDynamic ? 'true' : 'false'}</td>
                  <td>{broker.email}</td>
                  <td>{broker.createdAt ? <TextFormat type="date" value={broker.createdAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{broker.updatedAt ? <TextFormat type="date" value={broker.updatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{broker.deactivatedAt ? <TextFormat type="date" value={broker.deactivatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{broker.deactivatedBy}</td>
                  <td>{broker.invitedBy}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/broker/${broker.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button tag={Link} to={`/broker/${broker.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button tag={Link} to={`/broker/${broker.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Brokers found</div>
        )}
      </div>
    </div>
  );
};

export default Broker;
