import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getUser, reset, updateStatus } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';

export const UserManagementDeactivateDialog = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();
  const { login } = useParams<'login'>();
  const updateSuccess = useAppSelector(state => state.userManagement.updateSuccess);

  useEffect(() => {
    dispatch(getSession());
    dispatch(getUser(login));
  }, []);

  const searchParams = new URLSearchParams(history.state.usr).toString();
  
  const handleClose = () => {
    navigate('/admin/user-management', {state:searchParams});
  };

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
      dispatch(reset());
    }
  }, [updateSuccess]);

  const user = useAppSelector(state => state.userManagement.user);

  const confirmdDactivate = event => {
    dispatch(
      updateStatus({
        ...user,
        activated: !user.activated,
        deactivatedBy: user.activated ? account.login : null,
      }));
  };

  const account = useAppSelector(state => state.authentication.account);

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose}>
        Confirm deactivate operation
      </ModalHeader>
      <ModalBody>
        Are you sure you want to deactivate this User?
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp;
          Cancel
        </Button>
        <Button color="danger" onClick={confirmdDactivate}>
          <FontAwesomeIcon icon="trash" />
          &nbsp;
          Deactivate
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default UserManagementDeactivateDialog;
