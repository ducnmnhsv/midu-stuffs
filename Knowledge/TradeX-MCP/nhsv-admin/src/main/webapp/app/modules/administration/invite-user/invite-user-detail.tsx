import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT, APP_TIMESTAMP_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity } from './invite-user.reducer';
import { getSession } from 'app/shared/reducers/authentication';

export const InviteUserDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getEntity(id));
  }, []);

  const addOneDay = (date) => {
    const nextDay = new Date(date);
    nextDay.setDate(nextDay.getDate() + 1);
    return nextDay;
  };

  const navigate = useNavigate();
  const searchParams = new URLSearchParams(history.state.usr).toString();
  const loading = useAppSelector(state => state.inviteUser.loading);
  const handleClose = () => {
    navigate('/admin/invite-user', { state: searchParams });
  };
  const inviteUserEntity = useAppSelector(state => state.inviteUser.entity);

  return (
    <div className="popup">
      <div className="popup-content">
        {loading ? <p>Loading...</p> :
          <Row>
            <Col md="8">
              <table className='jh-entity-details'>
                <tr>
                  <td>Id:</td>
                  <td>{inviteUserEntity.id}</td>
                </tr>
                <tr>
                  <td>Username:</td>
                  <td>{inviteUserEntity.login}</td>
                </tr>
                <tr>
                  <td>Email:</td>
                  <td>{inviteUserEntity.email}</td>
                </tr>
                <tr>
                  <td>Status:</td>
                  <td>{inviteUserEntity.status}</td>
                </tr>
                <tr>
                  <td>Created At:</td>
                  <td>{inviteUserEntity.createdAt ? <TextFormat value={inviteUserEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}
                  </td>
                </tr>
                <tr>
                  <td>Updated At:</td>
                  <td>{inviteUserEntity.updatedAt ? <TextFormat value={inviteUserEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}
                  </td>
                </tr>
                <tr>
                  <td>Created Id:</td>
                  <td>{inviteUserEntity.createdId}</td>
                </tr>
                <tr>
                  <td>Created By:</td>
                  <td>{inviteUserEntity.createdBy}</td>
                </tr>
                <tr>
                  <td>Expired Time:</td>
                  <td>{inviteUserEntity.activationDate ? <TextFormat value={addOneDay(inviteUserEntity.activationDate)} type="date" format={APP_TIMESTAMP_FORMAT} /> : null}
                  </td>
                </tr>
              </table>
              <Button onClick={handleClose} replace color="info" data-cy="entityDetailsBackButton">
                <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
              </Button>
            </Col>
          </Row>
        }
      </div>
    </div>
  );
};

export default InviteUserDetail;
