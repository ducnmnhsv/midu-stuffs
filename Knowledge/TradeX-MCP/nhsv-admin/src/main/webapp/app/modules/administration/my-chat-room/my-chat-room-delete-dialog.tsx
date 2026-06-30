import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { deleteEntity, getEntity, reset } from '../chat-room/chat-room.reducer';
import { getSession } from 'app/shared/reducers/authentication';

export const MyChatRoomDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();
  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getSession());
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const entity = useAppSelector(state => state.chatRoom.entity);

  const updateSuccess = useAppSelector(state => state.chatRoom.updateSuccess);

  const searchParams = new URLSearchParams(history.state.usr).toString();

  const handleClose = () => {
    navigate('/admin/my-chat-room', { state: searchParams });
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      dispatch(reset());
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(entity.id));
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="socialLinkDeleteDialogHeading">
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="nhsvAdminApp.socialLink.delete.question">Are you sure you want to delete Chat Room {entity.id}?</ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-socialLink" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default MyChatRoomDeleteDialog;
