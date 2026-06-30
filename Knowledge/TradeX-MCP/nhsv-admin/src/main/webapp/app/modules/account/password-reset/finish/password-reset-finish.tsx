import React, { useState, useEffect } from 'react';
import { Col, Row, Button } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { getAccount, handlePasswordResetFinish, reset } from '../password-reset.reducer';
import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const PasswordResetFinishPage = () => {
  const dispatch = useAppDispatch();

  const [searchParams] = useSearchParams();
  const key = searchParams.get('key');

  const [password, setPassword] = useState('');

  useEffect(() => {
    dispatch(reset());
    dispatch(getAccount(key));
  }, []);

  const resetPasswordFailure = useAppSelector(state => state.passwordReset.resetPasswordFailure);
  const navigate = useNavigate();

  const handleValidSubmit = ({ newPassword }) => {
    dispatch(handlePasswordResetFinish({ key, newPassword }));
  }

  const handleClose = () => {
    navigate('/login');
  };

  const updatePassword = event => setPassword(event.target.value);

  const getResetForm = () => {
    return (
      <ValidatedForm onSubmit={handleValidSubmit}>
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
          Validate new password
        </Button>
      </ValidatedForm>
    );
  };

  const successMessage = useAppSelector(state => state.passwordReset.successMessage);

  useEffect(() => {
    if (successMessage) {
      toast.success(successMessage);
      dispatch(reset());
      handleClose();
    }
  }, [successMessage]);

  return (
    <div>
      <Row className="justify-content-center">
        {!resetPasswordFailure ? (
          <Col md="4">
            <h1>Reset password</h1>
            <div>{key ? getResetForm() : null}</div>
          </Col>
        ): <h1>
          Reset Password link is invalid. Please contact NHSV for support via admin@nhsv.com</h1>}
      </Row>
    </div>
  );
};

export default PasswordResetFinishPage;
