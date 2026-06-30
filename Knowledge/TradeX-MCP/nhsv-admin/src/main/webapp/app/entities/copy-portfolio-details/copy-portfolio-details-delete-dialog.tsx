import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity, deleteEntity } from './copy-portfolio-details.reducer';

export const CopyPortfolioDetailsDeleteDialog = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const copyPortfolioDetailsEntity = useAppSelector(state => state.copyPortfolioDetails.entity);
  const updateSuccess = useAppSelector(state => state.copyPortfolioDetails.updateSuccess);

  const handleClose = () => {
    navigate('/copy-portfolio-details' + location.search);
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(copyPortfolioDetailsEntity.id));
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="copyPortfolioDetailsDeleteDialogHeading">
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="nhsvAdminApp.copyPortfolioDetails.delete.question">
        Are you sure you want to delete Copy Portfolio Details {copyPortfolioDetailsEntity.id}?
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-copyPortfolioDetails" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default CopyPortfolioDetailsDeleteDialog;
