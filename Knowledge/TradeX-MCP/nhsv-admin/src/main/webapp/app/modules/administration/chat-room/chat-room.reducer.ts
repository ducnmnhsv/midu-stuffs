import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IChatRoom } from 'app/shared/model/chat-room.model';

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: {},
  updating: false,
  totalItems: 0,
  updateSuccess: false,
  paramQuery: '',
};

const apiUrl = 'api/v2/chat-rooms';

// Actions

export const getEntities = createAsyncThunk('chatRoom/fetch_entity_list', async ({ page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}&` : '?'}cacheBuster=${new Date().getTime()}`;
  return axios.get<IChatRoom[]>(requestUrl);
});

export const getEntitiesFilter = createAsyncThunk('chatRoom/fetch_entity_list', async (paramString: string | null) => {
  const requestUrl = `${apiUrl}${paramString ? `${paramString}&` : '?'}cacheBuster=${new Date().getTime()}`;
  return axios.get<IChatRoom[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  'chatRoom/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IChatRoom>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  'chatRoom/create_entity',
  async (entity: IChatRoom) => {
    const file = entity.file;
    entity.file = null;
    const result = await axios.post<IChatRoom>(apiUrl, cleanEntity(entity));
    if (file) {
      entity.id = result.data.id;
      entity.file = file;
      await axios.post<IChatRoom>(apiUrl + "/upload", entity, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    }
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntity = createAsyncThunk(
  'chatRoom/update_entity',
  async (entity: IChatRoom, thunkAPI) => {
    const result = await axios.put<IChatRoom>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const partialUpdateEntity = createAsyncThunk(
  'chatRoom/partial_update_entity',
  async (entity: IChatRoom, thunkAPI) => {
    const result = await axios.patch<IChatRoom>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const approveEntity = createAsyncThunk(
  'chatRoom/approve_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.post<IChatRoom>(requestUrl + '/approve', {});
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const rejectEntity = createAsyncThunk(
  'chatRoom/reject_entity',
  async (entity: IChatRoom) => {
    const id = entity.id;
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.post<IChatRoom>(requestUrl + '/reject', cleanEntity(entity));
    return result;
  },
  { serializeError: serializeAxiosError }
);


export const deleteEntity = createAsyncThunk(
  'chatRoom/delete_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IChatRoom>(requestUrl);
    return result;
  },
  { serializeError: serializeAxiosError }
);

// slice
export type ChatRoomState = Readonly<typeof initialState>;

export const ChatRoomSlice = createEntitySlice({
  name: 'chatRoom',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
        state.socialLinks = action.payload.data.socialLinks;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        const { data, headers } = action.payload;
        return {
          ...state,
          loading: false,
          entities: data,
          entity: {},
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(approveEntity), (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(rejectEntity), (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity, getEntitiesFilter), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity, approveEntity, rejectEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = ChatRoomSlice.actions;

// Reducer
export default ChatRoomSlice.reducer;
