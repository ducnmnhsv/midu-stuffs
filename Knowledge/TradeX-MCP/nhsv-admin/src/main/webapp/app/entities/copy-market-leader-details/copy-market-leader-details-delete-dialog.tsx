import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity, deleteEntity } from './copy-market-leader-details.reducer';

export const CopyMarketLeaderDetailsDeleteDialog = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const copyMarketLeaderDetailsEntity = useAppSelector(state => state.copyMarketLeaderDetails.entity);
  const updateSuccess = useAppSelector(state => state.copyMarketLeaderDetails.updateSuccess);

  const handleClose = () => {
    navigate('/copy-market-leader-details' + location.search);
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(copyMarketLeaderDetailsEntity.id));
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="copyMarketLeaderDetailsDeleteDialogHeading">
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="nhsvAdminApp.copyMarketLeaderDetails.delete.question">
        Are you sure you want to delete Copy Market Leader Details {copyMarketLeaderDetailsEntity.id}?
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-copyMarketLeaderDetails" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default CopyMarketLeaderDetailsDeleteDialog;
