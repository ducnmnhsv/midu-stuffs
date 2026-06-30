import React, { useState, useEffect } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { login } from 'app/shared/reducers/authentication';
import LoginModal from './login-modal';

export const Login = () => {
  const dispatch = useAppDispatch();
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const loginError = useAppSelector(state => state.authentication.loginError);
  const showModalLogin = useAppSelector(state => state.authentication.showModalLogin);
  const [showModal, setShowModal] = useState(showModalLogin);
  const navigate = useNavigate();
  const location = useLocation();
  const authorities = useAppSelector(state => state.authentication.account.authorities);

  useEffect(() => {
    setShowModal(true);
  }, []);

  const handleLogin = (username, password, rememberMe = false) => dispatch(login(username, password, rememberMe));

  const handleClose = () => {
    setShowModal(false);
    navigate('/');
  };

  if (!loginError && isAuthenticated) {
    if (isAuthenticated && (authorities.includes('ADMINISTRATOR') || authorities.includes('SUPER_ADMINISTRATOR'))) {
      return <Navigate to='/admin/user-management' replace/>;
    } else if (isAuthenticated && authorities.includes('BROKER')) {
      return <Navigate to="/admin/my-chat-room" replace/>;
    } else if (authorities?.includes('MARKET_LEADER') && authorities?.length === 1) {
      return <Navigate to="/admin/portfolio-management" replace/>;
    }
  }
  return <LoginModal showModal={showModal} handleLogin={handleLogin} handleClose={handleClose} loginError={loginError} />;
};

export default Login;
