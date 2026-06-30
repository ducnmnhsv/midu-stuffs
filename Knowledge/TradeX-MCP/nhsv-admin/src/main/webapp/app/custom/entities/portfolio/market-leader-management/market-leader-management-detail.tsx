import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Badge, Button, Col, Row } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';

import { getUser } from './market-leader-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import './UserManagementDetailPopup.css'; // Import CSS file for styling the popup

export const UserManagementDetail = () => {
  const dispatch = useAppDispatch();

  const { login } = useParams<'login'>();

  useEffect(() => {
    dispatch(getUser(login));
  }, []);

  const user = useAppSelector(state => state.userManagement.user);

  return (
    <div className="popup">
      <div className="popup-content">
        <div>
          <Row size="md">
            <Col md="7">
              <table className="jh-entity-details">
                <tr>
                  <td>
                    <b>Id:</b>
                  </td>
                  <td>{user.id}</td>
                </tr>
                <tr>
                  <td>
                    <b>Username:</b>
                  </td>
                  <td>
                    {user.login} <span> </span>{' '}
                    {user.activated ? <Badge color="success">Activated</Badge> : <Badge color="danger">Deactivated</Badge>}
                  </td>
                </tr>
                <tr>
                  <td>
                    <b>Full Name:</b>
                  </td>
                  <td>{user.fullName}</td>
                </tr>
                <tr>
                  <td>
                    <b>Roles:</b>
                  </td>
                  <td className="list-unstyled">
                    {user.authorities
                      ? user.authorities.map((authority, i) => (
                          <li key={`user-auth-${i}`}>
                            <Badge color="info">{authority}</Badge>
                          </li>
                        ))
                      : null}
                  </td>
                </tr>
                <tr>
                  <td>
                    <b>Introduction:</b>
                  </td>
                  <td>{user.introduction}</td>
                </tr>
                <tr>
                  <td>
                    <b>Email:</b>
                  </td>
                  <td>{user.email}</td>
                </tr>
                <tr>
                  <td>
                    <b>Created At:</b>
                  </td>
                  <td>
                    {user.createdDate ? <TextFormat value={user.createdDate} type="date" format={APP_DATE_FORMAT} blankOnInvalid /> : null}
                  </td>
                </tr>
                <tr>
                  <td>
                    <b>Updated At:</b>
                  </td>
                  <td>
                    {user.lastModifiedDate ? (
                      <TextFormat value={user.lastModifiedDate} type="date" format={APP_DATE_FORMAT} blankOnInvalid />
                    ) : null}
                  </td>
                </tr>
                <tr>
                  <td>
                    <b>Deactivate At:</b>
                  </td>
                  <td>
                    {user.deactivatedAt ? (
                      <TextFormat value={user.deactivatedAt} type="date" format={APP_DATE_FORMAT} blankOnInvalid />
                    ) : null}
                  </td>
                </tr>
                <tr>
                  <td>
                    <b>Deactivate By:</b>
                  </td>
                  <td>{user.deactivatedBy}</td>
                </tr>
                <tr>
                  <td>
                    <b>Invite By:</b>
                  </td>
                  <td>{user.invitedBy}</td>
                </tr>
              </table>
              <Button tag={Link} to="/admin/user-management" replace color="info" data-cy="entityDetailsBackButton">
                <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
              </Button>
            </Col>
            <Col md="3">
              <img src={user.photo} alt="" width={200} height={200} />
            </Col>
          </Row>
        </div>
      </div>
    </div>
  );
};

export default UserManagementDetail;
