import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity, approveEntity } from './chat-room.reducer';
import { getSession } from 'app/shared/reducers/authentication';

export const ChatRoomApproveDialog = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  const navigate = useNavigate();
  const searchParams = new URLSearchParams(history.state.usr).toString();
  const handleClose = () => {
    navigate('/admin/chat-room', { state: searchParams });
  };

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getSession());
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const chatRoomEntity = useAppSelector(state => state.chatRoom.entity);
  const updateSuccess = useAppSelector(state => state.chatRoom.updateSuccess);
  const updating = useAppSelector(state => state.chatRoom.updating);

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(approveEntity(chatRoomEntity.id));
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="chatRoomDeleteDialogHeading">
        Confirm approve operation
      </ModalHeader>
      <ModalBody id="nhsvAdminApp.chatRoom.delete.question">Are you sure you want to approve Chat Room {chatRoomEntity.id}?</ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose} disabled={updating}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-chatRoom" data-cy="entityConfirmDeleteButton" disabled={updating} color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="plus" />
          &nbsp; Approve
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default ChatRoomApproveDialog;
