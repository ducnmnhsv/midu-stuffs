import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { IQueryParams, createEntitySlice, EntityState, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { ICreatedChatRoom, defaultValue } from 'app/shared/model/created-chat-room.model';

const initialState: EntityState<ICreatedChatRoom> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/v2/created-chat-rooms';

export const getEntities = createAsyncThunk('createdChatRoom/fetch_entity_list', async ({ page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}&` : '?'}cacheBuster=${new Date().getTime()}`;
  return axios.get<ICreatedChatRoom[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  'createdChatRoom/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<ICreatedChatRoom>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  'createdChatRoom/create_entity',
  async (entity: ICreatedChatRoom) => {
    const result = await axios.post<ICreatedChatRoom>(apiUrl, cleanEntity(entity));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntity = createAsyncThunk(
  'createdChatRoom/update_entity',
  async (entity: ICreatedChatRoom) => {
    const file = entity.file;
    entity.file = null;
    const result = await axios.put<ICreatedChatRoom>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    if (file) {
      entity.file = file;
      await axios.post<ICreatedChatRoom>(apiUrl + "/upload", entity, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      entity.file = null;
    }
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const partialUpdateEntity = createAsyncThunk(
  'createdChatRoom/partial_update_entity',
  async (entity: ICreatedChatRoom) => {
    const result = await axios.patch<ICreatedChatRoom>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const deleteEntity = createAsyncThunk(
  'createdChatRoom/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<ICreatedChatRoom>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const getEntitiesFilter = createAsyncThunk('createdChatRoom/fetch_entity_list', async (paramString: string | null) => {
  const requestUrl = `${apiUrl}${paramString ? `${paramString}&` : '?'}cacheBuster=${new Date().getTime()}`;
  return axios.get<ICreatedChatRoom[]>(requestUrl);
});

// slice

export const CreatedChatRoomSlice = createEntitySlice({
  name: 'createdChatRoom',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
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
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntitiesFilter, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = CreatedChatRoomSlice.actions;

// Reducer
export default CreatedChatRoomSlice.reducer;
