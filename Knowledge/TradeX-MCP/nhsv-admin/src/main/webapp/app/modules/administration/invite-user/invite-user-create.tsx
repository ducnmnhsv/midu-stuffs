import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { ValidatedField, ValidatedForm, isEmail } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { locales, languages } from 'app/config/translation';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import Select, { components } from 'react-select';
import { getRoles } from 'app/modules/administration/user-management/user-management.reducer';
import { inviteUser, reset } from './invite-user.reducer';
import { getSession } from 'app/shared/reducers/authentication';

export const UserManagementInvite = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getRoles());
  }, []);

  const navigate = useNavigate();

  const searchParams = new URLSearchParams(history.state.usr);

  const handleClose = () => {
    if (searchParams.get('invite')) {
      searchParams.delete('invite');
      navigate('/admin/invite-user', { state: searchParams.toString() });
    } else {
      navigate('/admin/user-management', { state: searchParams.toString() });
    }
  };

  const loading = useAppSelector(state => state.inviteUser.loading);
  const updating = useAppSelector(state => state.inviteUser.updating);
  const authorities = useAppSelector(state => state.userManagement.authorities);
  const [selectedOptions, setSelectedOptions] = useState('');
  const [isSelectValid, setIsSelectValid] = useState(true);
  const updateSuccess = useAppSelector(state => state.inviteUser.updateSuccess);
  const account = useAppSelector(state => state.authentication.account);

  const saveUser = values => {
    dispatch(getSession());
    values = { ...values, authorities: selectedOptions };
    if (selectedOptions.length === 0) {
      setIsSelectValid(false);
    } else {
      setIsSelectValid(true);
      if (account.authorities.includes('SUPER_ADMINISTRATOR') || account.authorities.includes('ADMINISTRATOR')) {
        dispatch(inviteUser(values));
      } else {
        handleClose();
      }
    }
  };

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
      dispatch(reset());
    }
  }, [updateSuccess]);

  const MultiValueRemove = (props) => {
    if (props.data.isFixed) {
      return null;
    }
    return <components.MultiValueRemove {...props} />;
  };

  const listOfClients =
    authorities !== null &&
    authorities.map(authoritie => ({
      value: authoritie,
      label: authoritie.replace('_', ' '),
    }));

  const handleCheckboxChange = (selectedItems) => {
    setSelectedOptions(selectedItems.map((item) => item.value).join(','));
    if (selectedItems.length === 0) {
      setIsSelectValid(false);
    } else {
      setIsSelectValid(true);
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h1>Invite User</h1>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm onSubmit={saveUser}>
              <ValidatedField
                type="text"
                name="login"
                label="Username"
                validate={{
                  maxLength: {
                    value: 50,
                    message: 'This field cannot be longer than 50 characters.',
                  },
                  required: {
                    value: true,
                    message: 'This field is required.',
                  },
                }}
              />
              <ValidatedField
                name="email"
                label="Email"
                placeholder="Your email"
                type="email"
                validate={{
                  required: {
                    value: true,
                    message: 'This field is required.',
                  },
                  minLength: {
                    value: 5,
                    message: 'Email is required to be at least 5 characters.',
                  },
                  maxLength: {
                    value: 254,
                    message: 'Email cannot be longer than 50 characters.',
                  },
                  validate: v => isEmail(v) || 'Your email is invalid.',
                }}
              />
              <ValidatedField type="select" name="langKey" label="Language">
                {locales.map(locale => (
                  <option value={locale} key={locale}>
                    {languages[locale].name}
                  </option>
                ))}
              </ValidatedField>
              <Label>
                <span>Roles</span>
              </Label>
              <Select
                name="authorities"
                isMulti={true}
                options={authorities.includes('SUPER_ADMINISTRATOR') ? listOfClients.filter((item) => item.value !== "SUPER_ADMINISTRATOR")
                  : listOfClients.filter((item) => item.value !== "SUPER_ADMINISTRATOR" && item.value !== "ADMINISTRATOR")}
                onChange={handleCheckboxChange}
                onMenuClose={() =>
                  selectedOptions.length === 0 ? setIsSelectValid(false) :
                    setIsSelectValid(true)}
                isClearable={false}
                components={{ MultiValueRemove }}
                className={!isSelectValid ? 'role form-control is-invalid is-touched' : ''}
              />
              {!isSelectValid && <div className="invalid-feedback">This field is required.</div>}
              <p></p>
              <Button onClick={handleClose} replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" type="submit" onSubmit={saveUser} disabled={updating}>
                <FontAwesomeIcon icon="plus" />
                &nbsp; Invite
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default UserManagementInvite;
