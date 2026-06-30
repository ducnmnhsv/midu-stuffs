import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { ValidatedField, ValidatedForm, isEmail } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Select, { components } from 'react-select';
import { getUser, getRoles, updateUser, reset } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import './UserManagementDetailPopup.css';
import { getSession } from 'app/shared/reducers/authentication';

export const UserManagementUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { login } = useParams<'login'>();

  useEffect(() => {
    dispatch(getUser(login));
    dispatch(getRoles());
    return () => {
      dispatch(reset());
    };
  }, [login]);

  useEffect(() => {
    dispatch(getSession());
  }, []);

  const [selectedFile, setSelectedFile] = useState(null);

  const imageChange = (e) => {
    setSelectedFile(e.target.files[0]);
    const [file] = e.target.files;
    if (file) {
      setImageValue(URL.createObjectURL(file));
    }
  };

  const searchParams = new URLSearchParams(history.state.usr).toString();

  const handleClose = () => {
    navigate('/admin/user-management', { state: searchParams });
  };

  const saveUser = values => {
    if (selectedOptions.length === 0) {
      setIsSelectValid(false);
    } else {
      setIsSelectValid(true);
      values.photo = selectedFile;
      values.authorities = selectedOptions;
      values = { ...values };

      dispatch(updateUser(values));
    }
  };
  const [isSelectValid, setIsSelectValid] = useState(true);

  const isInvalid = false;
  const account = useAppSelector(state => state.authentication.account);
  const user = useAppSelector(state => state.userManagement.user);
  const loading = useAppSelector(state => state.userManagement.loading);
  const updating = useAppSelector(state => state.userManagement.updating);
  const authorities = useAppSelector(state => state.userManagement.authorities);
  const [selectedOptions, setSelectedOptions] = useState([]);
  const [imageValue, setImageValue] = useState('');
  const updateSuccess = useAppSelector(state => state.userManagement.updateSuccess);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
      dispatch(reset());
    }
  }, [updateSuccess]);

  useEffect(() => {
    if (user.authorities) {
      const autho = user.authorities;
      setSelectedOptions(autho);
    }
  }, [user.authorities]);

  const listOfClients =
    authorities !== null &&
    authorities.map(authoritie => ({
      value: authoritie,
      label: authoritie.replace('_', ' '),
      isDisabled: user.authorities.includes('SUPER_ADMINISTRATOR') && authoritie === 'SUPER_ADMINISTRATOR' ? true : false,
      isFixed: user.authorities.includes('SUPER_ADMINISTRATOR') && authoritie === 'SUPER_ADMINISTRATOR' ? true : false,
    }));

  const MultiValueRemove = (props) => {
    if (props.data.isFixed) {
      return null;
    }
    return <components.MultiValueRemove {...props} />;
  };

  const def = user.authorities.map((authoritie) => ({
    value: authoritie,
    label: authoritie,
    isFixed: (user.authorities.includes('SUPER_ADMINISTRATOR') && authoritie === 'SUPER_ADMINISTRATOR') || (authoritie === "ADMINISTRATOR" && !user.authorities.includes('SUPER_ADMINISTRATOR'))
  }));

  const handleCheckboxChange = (selectedItems) => {
    setSelectedOptions(selectedItems.map((item) => item.value));
    if (selectedItems.length === 0) {
      setIsSelectValid(false);
    } else {
      setIsSelectValid(true);
    }
  };

  const isFixed = (item) => {
    if (account.authorities.includes('SUPER_ADMINISTRATOR') && item === 'SUPER_ADMINISTRATOR') {
      return true;
    }
    if (!account.authorities.includes("SUPER_ADMINISTRATOR") && item === 'ADMINISTRATOR') {
      return true;
    }
    return false;
  }

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h1>Edit User Infomation</h1>
        </Col>
      </Row>
      {loading ? (
        <p>Loading...</p>
      ) : (
        <Row className="justify-content-center">
          <Col md="8">

            <ValidatedForm onSubmit={saveUser} defaultValues={user}>
              <ValidatedField type="text" name="id" required hidden readOnly label="ID" validate={{ required: true }} />
              <ValidatedField
                type="text"
                name="login"
                label="Username"
                validate={{
                  required: {
                    value: true,
                    message: 'Your username is required.',
                  },
                  maxLength: {
                    value: 50,
                    message: 'Your username cannot be longer than 50 characters.',
                  },
                }}
              />
              <ValidatedField
                type="text"
                name="fullName"
                label="Full name"
                validate={{
                  maxLength: {
                    value: 50,
                    message: 'This field cannot be longer than 50 characters.',
                  }, required: {
                    value: true,
                    message: 'Your username is required.',
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
                    message: 'Your email is required.',
                  },
                  minLength: {
                    value: 5,
                    message: 'Your email is required to be at least 5 characters.',
                  },
                  maxLength: {
                    value: 254,
                    message: 'Your email cannot be longer than 50 characters.',
                  },
                  validate: v => isEmail(v) || 'Your email is invalid.',
                }}
              />
              <ValidatedField
                name="introduction"
                label="Introduction"
                placeholder="Your Introduction"
                type="textarea"
              />
              <Label>
                <span>Roles</span>
              </Label>
              <Select
                name="authorities"
                isMulti={true}
                defaultValue={user.authorities.map(authoritie => ({
                  value: authoritie,
                  label: authoritie,
                  isFixed: isFixed(authoritie)
                }))}
                options={account.authorities.includes('SUPER_ADMINISTRATOR') ? listOfClients.filter((item) => item.value !== "SUPER_ADMINISTRATOR")
                  : listOfClients.filter((item) => item.value !== "SUPER_ADMINISTRATOR" && item.value !== "ADMINISTRATOR")}
                onChange={handleCheckboxChange}
                isClearable={false}
                components={{ MultiValueRemove }}
                className={!isSelectValid ? 'role is-touched is-invalid form-control' : ''}
              />
              {!isSelectValid && <div className="invalid-feedback">This field is required.</div>}
              <Button onClick={handleClose} replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" type="submit" disabled={isInvalid || updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          </Col>
          <Col>
            <div className='upload-contener'>
              <img src={imageValue ? imageValue : user.photoLink} alt="" width={200} height={200} className='user-image' />
              <input id="image" type="file" name="photo" accept="image/*" onChange={imageChange} hidden />
              <div className='upload-contener'>
                <label htmlFor="image" className='upload'>Upload</label>
              </div>
            </div>
          </Col>
        </Row>
      )}
    </div>
  );
};

export default UserManagementUpdate;
