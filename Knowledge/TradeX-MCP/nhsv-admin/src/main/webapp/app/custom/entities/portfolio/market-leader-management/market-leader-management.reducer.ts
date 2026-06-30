import axios from 'axios';
import { createAsyncThunk, createSlice, isFulfilled, isPending } from '@reduxjs/toolkit';

import { defaultValue, IUser } from 'app/shared/model/user.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const initialState = {
  loading: false,
  errorMessage: null,
  users: [] as ReadonlyArray<IUser>,
  authorities: [] as any[],
  user: defaultValue,
  updating: false,
  updateSuccess: false,
  totalItems: 0,
  paramString: '',
};

interface IParam {
  param: string;
  user: IUser;
}

const apiUrl = 'api/users';
const adminUrl = 'api/admin/users';

// Async Actions

export const getUsers = createAsyncThunk('userManagement/fetch_users', async ({ page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return axios.get<IUser[]>(requestUrl);
});

export const getEntitiesFilter = createAsyncThunk('userManagement/getFiler', async (paramString: string | null) => {
  return axios.get<IUser[]>(`${adminUrl}` + paramString);
});

export const getUsersAsAdmin = createAsyncThunk(
  'userManagement/fetch_users_as_admin',
  async ({ query, page, size, sort }: IQueryParams) => {
    const requestUrl = `${adminUrl}` + query;
    return axios.get<IUser[]>(requestUrl);
  }
);

export const getRoles = createAsyncThunk('userManagement/fetch_roles', async () => {
  return axios.get<any[]>(`api/authorities`);
});

export const getUser = createAsyncThunk(
  'userManagement/fetch_user',
  async (id: string) => {
    const requestUrl = `${adminUrl}/${id}`;
    return axios.get<IUser>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const updateStatus = createAsyncThunk(
  'userManagement/update_status',
  async (user: IUser) => {
    const res = await axios.put<IUser>(adminUrl + '/updateStatus', user);
    return res;
  },
  { serializeError: serializeAxiosError }
);

export const updateUser = createAsyncThunk(
  'userManagement/update_user',
  async (user: IUser) => {
    const result = await axios.put<IUser>(adminUrl, user);
    return result;
  },
  { serializeError: serializeAxiosError }
);

export type UserManagementState = Readonly<typeof initialState>;

export const UserManagementSlice = createSlice({
  name: 'userManagement',
  initialState: initialState as UserManagementState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getRoles.fulfilled, (state, action) => {
        state.authorities = action.payload.data;
      })
      .addCase(getUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.data;
      })
      .addMatcher(isFulfilled(getUsers, getUsersAsAdmin, getEntitiesFilter), (state, action) => {
        state.loading = false;
        state.users = action.payload.data;
        state.totalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(updateUser, updateStatus), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.user = action.payload.data;
      })
      .addMatcher(isPending(getUsers, getUsersAsAdmin, getUser, getEntitiesFilter), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(updateUser, updateStatus), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = UserManagementSlice.actions;

// Reducer
export default UserManagementSlice.reducer;
