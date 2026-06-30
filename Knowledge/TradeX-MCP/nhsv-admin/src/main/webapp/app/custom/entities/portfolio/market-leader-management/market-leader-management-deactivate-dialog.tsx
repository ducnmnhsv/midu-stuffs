import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getUser, updateStatus } from './market-leader-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const UserManagementDeactivateDialog = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();
  const { login } = useParams<'login'>();
  const searchParams = new URLSearchParams(document.location.search).toString();
  const updateSuccess = useAppSelector(state => state.userManagement.updateSuccess);

  useEffect(() => {
    dispatch(getUser(login));
  }, []);

  const handleClose = () => {
    navigate('/admin/user-management', { state: searchParams });
  };

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const user = useAppSelector(state => state.userManagement.user);

  const confirmdDactivate = event => {
    dispatch(
      updateStatus({
        ...user,
        activated: !user.activated,
        deactivatedBy: user.activated ? account.login : null,
      })
    );
  };

  const account = useAppSelector(state => state.authentication.account);

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose}>Confirm deactivate operation</ModalHeader>
      <ModalBody>Are you sure you want to deactivate this User?</ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button color="danger" onClick={confirmdDactivate}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Deactivate
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default UserManagementDeactivateDialog;
