import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { createEntitySlice, EntityState, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IInviteUser, defaultValue } from 'app/shared/model/invite-user.model';

const initialState: EntityState<IInviteUser> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/v2/invite-users';

// Actions
export const getEntitiesFilter = createAsyncThunk('inviteUser/getFiler',
  async (paramString: string | null) => {
    return axios.get<IInviteUser[]>(`${apiUrl}` + paramString);
  });

export const getEntity = createAsyncThunk(
  'inviteUser/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IInviteUser>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const inviteUser = createAsyncThunk(
  'inviteUser/invite_user',
  async (user: IInviteUser) => {
    const result = await axios.post<IInviteUser>(`${apiUrl}/invite`, cleanEntity(user));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const resendInvite = createAsyncThunk(
  'inviteUser/resend_invite',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.post<IInviteUser>(requestUrl + '/resend');
    return result;
  },
  { serializeError: serializeAxiosError }
);

// slice

export const InviteUserSlice = createEntitySlice({
  name: 'inviteUser',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(inviteUser.fulfilled, (state,action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(getEntitiesFilter), (state, action) => {
        const { data, headers } = action.payload;
        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(resendInvite), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entities = state.entities.map(entity => {
          if (entity.id === action.payload.data.id) {
            return action.payload.data;
          }
          return entity;
        });
      })
      .addMatcher(isPending(getEntitiesFilter, getEntity,inviteUser,resendInvite), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      });
  },
});

export const { reset } = InviteUserSlice.actions;

// Reducer
export default InviteUserSlice.reducer;
