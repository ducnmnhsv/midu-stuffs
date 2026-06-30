import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { IQueryParams, createEntitySlice, EntityState, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IMarketHistoryJobResult, defaultValue } from 'app/shared/model/market-history-job-result.model';

const initialState: EntityState<IMarketHistoryJobResult> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
  totalItems: 0,
};

const apiUrl = 'api/latest-job-result';

// Actions

export const getEntities = createAsyncThunk('marketHistoryJobResult/fetch_entity_list', async ({ page, size, sort }: IQueryParams) => {
  const requestUrl = `api/job-result${sort ? `?page=${page}&size=${size}&sort=${sort}&` : '?'}cacheBuster=${new Date().getTime()}`;
  return axios.get<any>(requestUrl);
});

export const getEntity = createAsyncThunk(
  'marketHistoryJobResult/fetch_entity',
  async (id) => {
    const requestUrl = `${apiUrl}`;
    return axios.get<IMarketHistoryJobResult>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  'marketHistoryJobResult/create_entity',
  async (entity: IMarketHistoryJobResult, thunkAPI) => {
    const result = await axios.post<IMarketHistoryJobResult>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntity = createAsyncThunk(
  'marketHistoryJobResult/update_entity',
  async (entity: IMarketHistoryJobResult, thunkAPI) => {
    const result = await axios.put<IMarketHistoryJobResult>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const partialUpdateEntity = createAsyncThunk(
  'marketHistoryJobResult/partial_update_entity',
  async (entity: IMarketHistoryJobResult, thunkAPI) => {
    const result = await axios.patch<IMarketHistoryJobResult>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const deleteEntity = createAsyncThunk(
  'marketHistoryJobResult/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IMarketHistoryJobResult>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

// slice

export const LatestJobResultSlice = createEntitySlice({
  name: 'marketHistoryJobResult',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        console.log("action", action)
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        return {
          ...state,
          loading: false,
          entities: action.payload.data.content,
          totalItems: parseInt(action.payload.headers['x-total-count'], 10)
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, getEntity, createEntity, updateEntity, partialUpdateEntity, deleteEntity), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message;
      });
  },
});

export const { reset } = LatestJobResultSlice.actions;

// Reducer
export default LatestJobResultSlice.reducer;
