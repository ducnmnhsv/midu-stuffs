import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Badge, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { getUser } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import './UserManagementDetailPopup.css';
import { getSession } from 'app/shared/reducers/authentication';

export const UserManagementDetail = () => {
  const dispatch = useAppDispatch();

  const { login } = useParams<'login'>();
  const navigate = useNavigate();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getUser(login));
  }, []);

  const searchParams = new URLSearchParams(history.state.usr).toString();
  const loading = useAppSelector(state => state.userManagement.loading);
  const handleClose = () => {
    navigate('/admin/user-management', { state: searchParams });
  };

  const user = useAppSelector(state => state.userManagement.user);

  return (
    <div className="popup">
      <div className="popup-content">
        <div>
          {loading ? <p>Loading...</p> :
            <Row size="md">
              <Col md="10">
                <table className="jh-entity-details">
                  <tr>
                    <td className='td1'><b>Id:</b></td>
                    <td>{user.id}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Username:</b></td>
                    <td>{user.login} <span> </span> {user.activated ? <Badge color="success">Activated</Badge> : <Badge color="danger">Deactivated</Badge>}
                    </td>

                  </tr>
                  <tr>
                    <td className='td1'><b>Full Name:</b></td>
                    <td>{user.fullName}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Roles:</b></td>
                    <td className="list-unstyled">
                      {user.authorities
                        ? user.authorities.map((authority, i) => (
                          <li key={`user-auth-${i}`}>
                            <Badge color="info">{authority}</Badge>
                          </li>
                        ))
                        : null}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Introduction:</b></td>
                    <td className='mutiLine'>{user.introduction}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Email:</b></td>
                    <td>{user.email}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Created At:</b></td>
                    <td>{user.createdDate ? <TextFormat value={user.createdDate} type="date" format={APP_DATE_FORMAT} blankOnInvalid /> : null}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Updated At:</b></td>
                    <td>{user.lastModifiedDate ? <TextFormat value={user.lastModifiedDate} type="date" format={APP_DATE_FORMAT} blankOnInvalid /> : null}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Deactivated At:</b></td>
                    <td>{user.deactivatedAt ? <TextFormat value={user.deactivatedAt} type="date" format={APP_DATE_FORMAT} blankOnInvalid /> : null}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Deactivated By:</b></td>
                    <td>{user.deactivatedBy}</td>
                  </tr>
                  <tr>
                    <td className='td1'><b>Invited By:</b></td>
                    <td>{user.invitedBy}</td>
                  </tr>
                </table>

                <Button onClick={handleClose} replace color="info" data-cy="entityDetailsBackButton">
                  <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
                </Button>
              </Col>
              <Col md="2">
                <img src={user.photoLink} alt="" width={200} height={200} />
              </Col>
            </Row>
          }
        </div>
      </div>
    </div>
  );
};


export default UserManagementDetail;
