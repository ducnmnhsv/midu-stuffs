import React, { useState, useEffect } from 'react';
import { Col, Row, Button } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getKey, handleCreateFinish, reset } from './password-reset/password-reset.reducer';

export const AccountFinishPage = () => {
  const dispatch = useAppDispatch();

  const [searchParams] = useSearchParams();
  const key = searchParams.get('key');

  const [password, setPassword] = useState('');

  useEffect(() => {
    dispatch(reset());
    dispatch(getKey(key));
  }, []);

  const navigate = useNavigate();

  const handleClose = () => {
    navigate('/login');
  };
  
  const resetPasswordFailure = useAppSelector(state => state.passwordReset.resetPasswordFailure);

  const handleValidSubmit = ({ fullName, newPassword }) => dispatch(handleCreateFinish({ key, newPassword, fullName }));

  const updatePassword = event => setPassword(event.target.value);

  const getResetForm = () => {
    return (
      <ValidatedForm onSubmit={handleValidSubmit}>
        <ValidatedField
          name="fullName"
          label="Full Name"
          placeholder="Full Name"
          validate={{
            required: { value: true, message: 'Your Full Name is required.' },
            minLength: { value: 4, message: 'Your Full Name is required to be at least 1 characters.' },
            maxLength: { value: 50, message: 'Your Full Name cannot be longer than 50 characters.' },
          }}
          data-cy="fullName"
        >
        </ValidatedField>
        <ValidatedField
          name="newPassword"
          label="New password"
          placeholder="New password"
          type="password"
          validate={{
            required: { value: true, message: 'Your password is required.' },
            minLength: { value: 4, message: 'Your password is required to be at least 4 characters.' },
            maxLength: { value: 50, message: 'Your password cannot be longer than 50 characters.' },
          }}
          onChange={updatePassword}
          data-cy="resetPassword"
        />
        <PasswordStrengthBar password={password} />
        <ValidatedField
          name="confirmPassword"
          label="New password confirmation"
          placeholder="Confirm the new password"
          type="password"
          validate={{
            required: { value: true, message: 'Your confirmation password is required.' },
            minLength: { value: 4, message: 'Your confirmation password is required to be at least 4 characters.' },
            maxLength: { value: 50, message: 'Your confirmation password cannot be longer than 50 characters.' },
            validate: v => v === password || 'The password and its confirmation do not match!',
          }}
          data-cy="confirmResetPassword"
        />
        <Button color="success" type="submit" data-cy="submit">
          Create Account
        </Button>
      </ValidatedForm>
    );
  };

  const successMessage = useAppSelector(state => state.passwordReset.successMessage);

  useEffect(() => {
    if (successMessage) {
      toast.success(successMessage);
      handleClose();
    }
  }, [successMessage]);

  return (
    <div>
      <Row className="justify-content-center">
        {!resetPasswordFailure ? (
          <Col md="4">
            <h1>Create Account</h1>
            <div>{key ? getResetForm() : null}</div>
          </Col>
        ): <h1>
          Invitation link is expired. Please contact NHSV for support via admin@nhsv.com</h1>}
      </Row>
    </div>
  );
};

export default AccountFinishPage;
