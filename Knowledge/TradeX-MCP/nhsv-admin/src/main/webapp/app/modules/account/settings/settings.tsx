import React, { useEffect, useState } from 'react';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { toast } from 'react-toastify';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';
import { saveAccountSettings, reset, getRoles } from './settings.reducer';
import '../../administration/user-management/UserManagementDetailPopup.css';

export const SettingsPage = () => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const successMessage = useAppSelector(state => state.settings.successMessage);
  const authorities = useAppSelector(state => state.userManagement.authorities);
  const [imageValue, setImageValue] = useState('');

  const [selectedFile, setSelectedFile] = useState(null);

  const imageChange = (e) => {
    setSelectedFile(e.target.files[0]);
    const [file] = e.target.files;
    if (file) {
      setImageValue(URL.createObjectURL(file));
    }
  };

  useEffect(() => {
    dispatch(getSession());
    dispatch(getRoles());
    return () => {
      dispatch(reset());
    };
  }, []);

  useEffect(() => {
    if (successMessage) {
      toast.success(successMessage);
      dispatch(reset());
      dispatch(getSession());
      dispatch(getRoles());
    }
  }, [successMessage]);

  const handleValidSubmit = values => {
    values.photo = selectedFile;
    dispatch(
      saveAccountSettings({
        ...account,
        ...values,
      })
    );
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="settings-title">
            <strong>My Profile</strong>
          </h2>
          <ValidatedForm id="settings-form" onSubmit={handleValidSubmit} defaultValues={account}>
            <ValidatedField
              name="id"
              label="Id"
              id="id"
              data-cy="id"
              disabled
            />
            <ValidatedField
              name="login"
              label="Username"
              id="login"
              placeholder="Username"
              data-cy="login"
              disabled
            />
            <ValidatedField
              name="fullName"
              label="Full Name"
              placeholder="fullName"
              validate={{
                required: { value: true, message: 'Your Full Name is required.' },
                maxLength: { value: 50, message: 'Your Full Name cannot be longer than 50 characters.' },
              }}
              data-cy="fullName"
            />
            <ValidatedField
              name="email"
              label="Email"
              placeholder="Email"
              data-cy="email"
              disabled
            />
            <ValidatedField
              name="introduction"
              label="Introduction"
              placeholder="Introduction"
              data-cy="introduction"
              type="textarea"
              rows={5}
            />
            <ValidatedField disabled type="select" name="authorities" multiple label="Role" defaultValue={account.authorities}>
              {authorities.map(role => (account.authorities && account.authorities.includes(role) ?
                <option value={role} key={role}>
                  {role.replace('_', ' ')}
                </option> : null
              ))}
            </ValidatedField>
            <Button color="primary" type="submit" data-cy="submit">
              Save
            </Button>
          </ValidatedForm>
        </Col>
        <Col>
          <div className='upload-contener'>
            <img src={imageValue ? imageValue : account.photoLink} alt="" width={200} height={200} className='user-image' />
            <input id="image" type="file" name="photo" accept="image/*" onChange={imageChange} hidden />
            <div className='upload-contener'>
              <label htmlFor="image" className='upload'>Upload</label>
            </div>
          </div>
        </Col>
      </Row>
    </div >
  );
};

export default SettingsPage;
