import axios from 'axios';
import { createAsyncThunk, createSlice, isPending, isRejected } from '@reduxjs/toolkit';

import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const initialState = {
  loading: false,
  resetPasswordSuccess: false,
  resetPasswordFailure: false,
  successMessage: null,
};

export type PasswordResetState = Readonly<typeof initialState>;

const apiUrl = 'api/account/reset-password';
// Actions

export const handlePasswordResetInit = createAsyncThunk(
  'passwordReset/reset_password_init',
  // If the content-type isn't set that way, axios will try to encode the body and thus modify the data sent to the server.
  async (mail: string) => axios.post(`${apiUrl}/init`, mail, { headers: { ['Content-Type']: 'text/plain' } }),
  { serializeError: serializeAxiosError }
);

export const handlePasswordResetFinish = createAsyncThunk(
  'passwordReset/reset_password_finish',
  async (data: { key: string; newPassword: string }) => axios.post(`${apiUrl}/finish`, data),
  { serializeError: serializeAxiosError }
);

export const getAccount = createAsyncThunk(
  'passwordReset/getAccount',
  async ( key: string ) => axios.get(`${apiUrl}/getAccount?key=`+ key),
  { serializeError: serializeAxiosError }
);

export const getKey = createAsyncThunk(
  'passwordReset/getAccount',
  async ( key: string ) => axios.get(`${apiUrl}/getKey?key=`+ key),
  { serializeError: serializeAxiosError }
);

export const handleCreateFinish = createAsyncThunk(
  'passwordReset/create_finish',
  async (data: { key: string; newPassword: string,fullName: string}) => axios.post(`${apiUrl}/create`, data),
  { serializeError: serializeAxiosError }
);


export const PasswordResetSlice = createSlice({
  name: 'passwordReset',
  initialState: initialState as PasswordResetState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(handlePasswordResetInit.fulfilled, () => ({
        ...initialState,
        loading: false,
        resetPasswordSuccess: true,
        successMessage: 'Check your emails for details on how to reset your password.',
      }))
      .addCase(handlePasswordResetFinish.fulfilled, () => ({
        ...initialState,
        loading: false,
        resetPasswordSuccess: true,
        successMessage: "Reset Password Successfully",
      }))
      .addCase(handleCreateFinish.fulfilled, () => ({
        ...initialState,
        loading: false,
        resetPasswordSuccess: true,
        successMessage: "Your account has been created.",
      }))
      .addMatcher(isPending(handlePasswordResetInit, handlePasswordResetFinish,getAccount,getKey,handleCreateFinish), state => {
        state.loading = true;
      })
      .addMatcher(isRejected(handlePasswordResetInit, handlePasswordResetFinish,getAccount,getKey,handleCreateFinish), () => ({
        ...initialState,
        loading: false,
        resetPasswordFailure: true,
      }));
  },
});

export const { reset } = PasswordResetSlice.actions;

// Reducer
export default PasswordResetSlice.reducer;
