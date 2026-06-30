import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ISocialLink } from 'app/shared/model/social-link.model';
import { getEntities } from './social-link.reducer';

export const SocialLink = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const socialLinkList = useAppSelector(state => state.socialLink.entities);
  const loading = useAppSelector(state => state.socialLink.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="social-link-heading" data-cy="SocialLinkHeading">
        Social Links
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/social-link/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Social Link
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {socialLinkList && socialLinkList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Link</th>
                <th>Chat Room</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {socialLinkList.map((socialLink, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/social-link/${socialLink.id}`} color="link" size="sm">
                      {socialLink.id}
                    </Button>
                  </td>
                  <td>{socialLink.type}</td>
                  <td>{socialLink.link}</td>
                  <td>{socialLink.chatRoom ? <Link to={`/chat-room/${socialLink.chatRoom.id}`}>{socialLink.chatRoom.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/social-link/${socialLink.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button tag={Link} to={`/social-link/${socialLink.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button tag={Link} to={`/social-link/${socialLink.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Social Links found</div>
        )}
      </div>
    </div>
  );
};

export default SocialLink;
