import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT} from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity } from './chat-room.reducer';
import { StatusEnum } from 'app/shared/model/enumerations/status-enum.model';
import React from 'react';
import { getSession } from 'app/shared/reducers/authentication';

export const ChatRoomDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getEntity(id));
  }, []);

  const navigate = useNavigate();
  const searchParams = new URLSearchParams(history.state.usr).toString();
  const loading = useAppSelector(state => state.chatRoom.loading);
  const handleClose = () => {
    navigate('/admin/chat-room', { state: searchParams });
  };
  const handleApprove = login => () => {
    navigate(`/admin/chat-room/${login}/approve`, { state: searchParams });
  }
  const handleReject = login => () => {
    navigate(`/admin/chat-room/${login}/reject`, { state: searchParams });
  }

  const chatRoomEntity = useAppSelector(state => state.chatRoom.entity);
  return (
    <div className="popup">
      <div className="popup-content">
        {loading ? <p>Loading...</p> : 
        <Row>
          <Col md="10">
            <table className='jh-entity-details' >
              <tr>
                <td className='td1'>Action:</td>
                <td>{chatRoomEntity.action}</td>
              </tr>
              <tr>
                <td className='td1'>Id:</td>
                <td>{chatRoomEntity.id}</td>
              </tr>
              <tr>
                <td className='td1'>Group Name:</td>
                <td>{chatRoomEntity.groupName}</td>
              </tr>
              <tr>
                <td className='td1'>Introduction:</td>
                <td className='mutiLine'>{chatRoomEntity.introduction}</td>
              </tr>
              <tr>
                <td className='td1'>Group Owner:</td>
                <td>{chatRoomEntity.groupOwner}</td>
              </tr>
              <tr>
                <td className='td1'>Broker Contact:</td>
                <td>{chatRoomEntity.brokerContact}</td>
              </tr>
              <tr>
                <td className='td1'>Social Link:</td>
              </tr>
              {chatRoomEntity.socialLinks ? chatRoomEntity.socialLinks.map((socialLink, i) => (
                <><tr>
                  <td className='td1'>
                    Type:
                  </td>
                  <td>
                    {socialLink.type}
                  </td>
                </tr><tr>
                    <td className='td1'>
                      Link:
                    </td>
                    <td>
                      {socialLink.link}
                    </td>
                  </tr></>
              )) : null}
              <tr>
                <td className='td1'>Status:</td>
                <td>{chatRoomEntity.status}</td>
              </tr>
              <tr>
                <td className='td1'>Created By:</td>
                <td>{chatRoomEntity.createdBy}</td>
              </tr>
              <tr>
                <td className='td1'>Created At:</td>
                <td>{chatRoomEntity.createdAt ? <TextFormat value={chatRoomEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</td>
              </tr>
              <tr>
                <td className='td1'>Updated At:</td>
                <td>{chatRoomEntity.updatedAt ? <TextFormat value={chatRoomEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</td>
              </tr>
              {chatRoomEntity.approvedAt ? <tr>
                <td className='td1'>Approved At:</td>
                <td>{chatRoomEntity.approvedAt ? <TextFormat value={chatRoomEntity.approvedAt} type="date" format={APP_DATE_FORMAT} /> : null}</td>
              </tr> : null}
              {chatRoomEntity.rejectedAt ? <tr>
                <td className='td1'>Rejected At:</td>
                <td>{chatRoomEntity.rejectedAt ? <TextFormat value={chatRoomEntity.rejectedAt} type="date" format={APP_DATE_FORMAT} /> : null}</td>
              </tr> : null}
              {chatRoomEntity.approvedBy ? <tr>
                <td className='td1'>Approved By:</td>
                <td>{chatRoomEntity.approvedBy}</td>
              </tr> : null}
              {chatRoomEntity.rejectedBy ? <tr>
                <td className='td1'>Rejected By:</td>
                <td>{chatRoomEntity.rejectedBy}</td>
              </tr> : null}
              {chatRoomEntity.rejectReason ? <tr>
                <td className='td1'>Rejected Reason:</td>
                <td>{chatRoomEntity.rejectReason}</td>
              </tr> : null}
            </table>
            <Button onClick={handleClose} size='sm' replace color="info" data-cy="entityDetailsBackButton">
              <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
            </Button>
            <Button
              onClick={handleApprove(id)}
              color="danger"
              size="sm"
              disabled={chatRoomEntity.status === StatusEnum.REJECTED || chatRoomEntity.status === StatusEnum.APPROVED}  >
              <FontAwesomeIcon icon="plus" /> <span className="d-none d-md-inline">Approve</span>
            </Button>
            <Button
              onClick={handleReject(id)}
              color="danger"
              size="sm"
              disabled={chatRoomEntity.status === StatusEnum.REJECTED || chatRoomEntity.status === StatusEnum.APPROVED}  >
              <FontAwesomeIcon icon="ban" /> <span className="d-none d-md-inline">Reject</span>
            </Button>
          </Col>
          <Col md="2">
            <img src={chatRoomEntity.photo} alt="" width={200} height={200} />
          </Col>
        </Row>
        }
      </div>
    </div>
  );
};

export default ChatRoomDetail;
