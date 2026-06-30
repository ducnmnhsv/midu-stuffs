import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity, rejectEntity } from './chat-room.reducer';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { getSession } from 'app/shared/reducers/authentication';

export const ChatRoomRejectDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();
  const [loadModal, setLoadModal] = useState(false);
  useEffect(() => {
    dispatch(getSession());
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const chatRoomEntity = useAppSelector(state => state.chatRoom.entity);
  const updateSuccess = useAppSelector(state => state.chatRoom.updateSuccess);
  const updating = useAppSelector(state => state.chatRoom.updating);
  
  const searchParams = new URLSearchParams(history.state.usr).toString();
  const handleClose = () => {
    navigate('/admin/chat-room', { state: searchParams });
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = (value: any) => {
    const entity = {
      ...chatRoomEntity
    };
    entity.id = id;
    entity.rejectReason = value.reason;
    dispatch(rejectEntity(entity));
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="chatRoomDeleteDialogHeading">
        Confirm reject operation
      </ModalHeader>
      <ModalBody id="nhsvAdminApp.chatRoom.delete.question">
        <ValidatedForm onSubmit={confirmDelete}>
          <ValidatedField
            name='reason'
            label='Reason'
            id='chat-room-reason'
            data-cy='reason'
            type='text'
            placeholder='Reason'
            validate={{
              required: { value: true, message: 'This field is required.' },
            }}
          >
          </ValidatedField>
          <ModalFooter>
            <Button color="secondary" onClick={handleClose} disabled={updating}>
              <FontAwesomeIcon icon="ban" />
              &nbsp; Cancel 
            </Button>
            <Button id="jhi-confirm-delete-chatRoom" disabled={updating} data-cy="entityConfirmDeleteButton" color="danger" type='submit'>
              <FontAwesomeIcon icon="trash" />
              &nbsp; Reject
            </Button>
          </ModalFooter>
        </ValidatedForm>
        Are you sure you want to reject Chat Room {chatRoomEntity.id}?</ModalBody>

    </Modal>
  );
};

export default ChatRoomRejectDialog;
