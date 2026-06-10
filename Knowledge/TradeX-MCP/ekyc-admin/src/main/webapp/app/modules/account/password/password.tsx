import React, { useState, useEffect } from 'react';
import { Translate, translate } from 'react-jhipster';
import { connect } from 'react-redux';
import { AvForm, AvField } from 'availity-reactstrap-validation';
import { Row, Col, Button } from 'reactstrap';

import { IRootState } from 'app/shared/reducers';
import { getSession } from 'app/shared/reducers/authentication';
import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';
import { savePassword, reset } from './password.reducer';

export interface IUserPasswordProps extends StateProps, DispatchProps {}

export const PasswordPage = (props: IUserPasswordProps) => {
  const [password, setPassword] = useState('');

  useEffect(() => {
    props.reset();
    props.getSession();
    return () => {
      props.reset();
    };
  }, []);

  const handleValidSubmit = (event, values) => {
    props.savePassword(values.currentPassword, values.newPassword);
  };

  const updatePassword = event => setPassword(event.target.value);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="password-title">
            <Translate contentKey="password.title" interpolate={{ username: props.account.login }}>
              Password for {props.account.login}
            </Translate>
          </h2>
          <AvForm id="password-form" onValidSubmit={handleValidSubmit}>
            <AvField
              name="currentPassword"
              label={translate('global.form.currentpasswordLabel')}
              placeholder={translate('global.form.currentpasswordPlaceholder')}
              type="password"
              validate={{
                required: { value: true, errorMessage: translate('global.messages.validate.newpassword.required') },
              }}
              data-cy="currentPassword"
            />
            <AvField
              name="newPassword"
              label={translate('global.form.newpasswordLabel')}
              placeholder={translate('global.form.newpasswordPlaceholder')}
              type="password"
              validate={{
                required: { value: true, errorMessage: translate('global.messages.validate.newpassword.required') },
                minLength: { value: 4, errorMessage: translate('global.messages.validate.newpassword.minlength') },
                maxLength: { value: 50, errorMessage: translate('global.messages.validate.newpassword.maxlength') },
              }}
              onChange={updatePassword}
              data-cy="newPassword"
            />
            <PasswordStrengthBar password={password} />
            <AvField
              name="confirmPassword"
              label={translate('global.form.confirmpasswordLabel')}
              placeholder={translate('global.form.confirmpasswordPlaceholder')}
              type="password"
              validate={{
                required: {
                  value: true,
                  errorMessage: translate('global.messages.validate.confirmpassword.required'),
                },
                minLength: {
                  value: 4,
                  errorMessage: translate('global.messages.validate.confirmpassword.minlength'),
                },
                maxLength: {
                  value: 50,
                  errorMessage: translate('global.messages.validate.confirmpassword.maxlength'),
                },
                match: {
                  value: 'newPassword',
                  errorMessage: translate('global.messages.error.dontmatch'),
                },
              }}
              data-cy="confirmPassword"
            />
            <div className="w-100 text-right">
              <Button color="success" type="submit" data-cy="submit">
                <Translate contentKey="password.form.button">Save</Translate>
              </Button>
            </div>
          </AvForm>
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = ({ authentication }: IRootState) => ({
  account: authentication.account,
  isAuthenticated: authentication.isAuthenticated,
});

const mapDispatchToProps = { getSession, savePassword, reset };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(PasswordPage);
