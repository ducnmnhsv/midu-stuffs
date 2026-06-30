//package com.difisoft.nhsv.admin.web.rest;
//
//import static com.difisoft.nhsv.admin.web.rest.TestUtil.sameInstant;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasItem;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import com.difisoft.nhsv.admin.IntegrationTest;
//import com.difisoft.nhsv.admin.domain.MarketHistoryJobResult;
//import com.difisoft.nhsv.admin.repository.MarketHistoryJobResultRepository;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.atomic.AtomicLong;
//import javax.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
///**
// * Integration tests for the {@link MarketHistoryJobResultResource} REST controller.
// */
//@IntegrationTest
//@ExtendWith(MockitoExtension.class)
//@AutoConfigureMockMvc
//@WithMockUser
//class MarketHistoryJobResultResourceIT {
//
//    private static final Boolean DEFAULT_IS_SUCCESS = false;
//    private static final Boolean UPDATED_IS_SUCCESS = true;
//
//    private static final ZonedDateTime DEFAULT_TIME_START = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
//    private static final ZonedDateTime UPDATED_TIME_START = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
//
//    private static final ZonedDateTime DEFAULT_TIME_END = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
//    private static final ZonedDateTime UPDATED_TIME_END = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
//
//    private static final String DEFAULT_ERROR = "AAAAAAAAAA";
//    private static final String UPDATED_ERROR = "BBBBBBBBBB";
//
//    private static final String DEFAULT_SYMBOLS = "AAAAAAAAAA";
//    private static final String UPDATED_SYMBOLS = "BBBBBBBBBB";
//
//    private static final String ENTITY_API_URL = "/api/market-history-job-results";
//    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
//
//    private static Random random = new Random();
//    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
//
//    @Autowired
//    private MarketHistoryJobResultRepository marketHistoryJobResultRepository;
//
//    @Mock
//    private MarketHistoryJobResultRepository marketHistoryJobResultRepositoryMock;
//
//    @Autowired
//    private EntityManager em;
//
//    @Autowired
//    private MockMvc restMarketHistoryJobResultMockMvc;
//
//    private MarketHistoryJobResult marketHistoryJobResult;
//
//    /**
//     * Create an entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static MarketHistoryJobResult createEntity(EntityManager em) {
//        MarketHistoryJobResult marketHistoryJobResult = new MarketHistoryJobResult()
//            .is_success(DEFAULT_IS_SUCCESS)
//            .time_start(DEFAULT_TIME_START)
//            .time_end(DEFAULT_TIME_END)
//            .error(DEFAULT_ERROR)
//            .symbols(DEFAULT_SYMBOLS);
//        return marketHistoryJobResult;
//    }
//
//    /**
//     * Create an updated entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static MarketHistoryJobResult createUpdatedEntity(EntityManager em) {
//        MarketHistoryJobResult marketHistoryJobResult = new MarketHistoryJobResult()
//            .is_success(UPDATED_IS_SUCCESS)
//            .time_start(UPDATED_TIME_START)
//            .time_end(UPDATED_TIME_END)
//            .error(UPDATED_ERROR)
//            .symbols(UPDATED_SYMBOLS);
//        return marketHistoryJobResult;
//    }
//
//    @BeforeEach
//    public void initTest() {
//        marketHistoryJobResult = createEntity(em);
//    }
//
//    @Test
//    @Transactional
//    void createMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeCreate = marketHistoryJobResultRepository.findAll().size();
//        // Create the MarketHistoryJobResult
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                post(ENTITY_API_URL)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isCreated());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeCreate + 1);
//        MarketHistoryJobResult testMarketHistoryJobResult = marketHistoryJobResultList.get(marketHistoryJobResultList.size() - 1);
//        assertThat(testMarketHistoryJobResult.getIs_success()).isEqualTo(DEFAULT_IS_SUCCESS);
//        assertThat(testMarketHistoryJobResult.getTime_start()).isEqualTo(DEFAULT_TIME_START);
//        assertThat(testMarketHistoryJobResult.getTime_end()).isEqualTo(DEFAULT_TIME_END);
//        assertThat(testMarketHistoryJobResult.getError()).isEqualTo(DEFAULT_ERROR);
//        assertThat(testMarketHistoryJobResult.getSymbols()).isEqualTo(DEFAULT_SYMBOLS);
//    }
//
//    @Test
//    @Transactional
//    void createMarketHistoryJobResultWithExistingId() throws Exception {
//        // Create the MarketHistoryJobResult with an existing ID
//        marketHistoryJobResult.setId(1L);
//
//        int databaseSizeBeforeCreate = marketHistoryJobResultRepository.findAll().size();
//
//        // An entity with an existing ID cannot be created, so this API call must fail
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                post(ENTITY_API_URL)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeCreate);
//    }
//
//    @Test
//    @Transactional
//    void getAllMarketHistoryJobResults() throws Exception {
//        // Initialize the database
//        marketHistoryJobResultRepository.saveAndFlush(marketHistoryJobResult);
//
//        // Get all the marketHistoryJobResultList
//        restMarketHistoryJobResultMockMvc
//            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(marketHistoryJobResult.getId().intValue())))
//            .andExpect(jsonPath("$.[*].is_success").value(hasItem(DEFAULT_IS_SUCCESS.booleanValue())))
//            .andExpect(jsonPath("$.[*].time_start").value(hasItem(sameInstant(DEFAULT_TIME_START))))
//            .andExpect(jsonPath("$.[*].time_end").value(hasItem(sameInstant(DEFAULT_TIME_END))))
//            .andExpect(jsonPath("$.[*].error").value(hasItem(DEFAULT_ERROR)))
//            .andExpect(jsonPath("$.[*].symbols").value(hasItem(DEFAULT_SYMBOLS)));
//    }
//
//    @SuppressWarnings({ "unchecked" })
//    void getAllMarketHistoryJobResultsWithEagerRelationshipsIsEnabled() throws Exception {
//        when(marketHistoryJobResultRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
//
//        restMarketHistoryJobResultMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());
//
//        verify(marketHistoryJobResultRepositoryMock, times(1)).findAllWithEagerRelationships(any());
//    }
//
//    @SuppressWarnings({ "unchecked" })
//    void getAllMarketHistoryJobResultsWithEagerRelationshipsIsNotEnabled() throws Exception {
//        when(marketHistoryJobResultRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
//
//        restMarketHistoryJobResultMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
//        verify(marketHistoryJobResultRepositoryMock, times(1)).findAll(any(Pageable.class));
//    }
//
//    @Test
//    @Transactional
//    void getMarketHistoryJobResult() throws Exception {
//        // Initialize the database
//        marketHistoryJobResultRepository.saveAndFlush(marketHistoryJobResult);
//
//        // Get the marketHistoryJobResult
//        restMarketHistoryJobResultMockMvc
//            .perform(get(ENTITY_API_URL_ID, marketHistoryJobResult.getId()))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.id").value(marketHistoryJobResult.getId().intValue()))
//            .andExpect(jsonPath("$.is_success").value(DEFAULT_IS_SUCCESS.booleanValue()))
//            .andExpect(jsonPath("$.time_start").value(sameInstant(DEFAULT_TIME_START)))
//            .andExpect(jsonPath("$.time_end").value(sameInstant(DEFAULT_TIME_END)))
//            .andExpect(jsonPath("$.error").value(DEFAULT_ERROR))
//            .andExpect(jsonPath("$.symbols").value(DEFAULT_SYMBOLS));
//    }
//
//    @Test
//    @Transactional
//    void getNonExistingMarketHistoryJobResult() throws Exception {
//        // Get the marketHistoryJobResult
//        restMarketHistoryJobResultMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
//    }
//
//    @Test
//    @Transactional
//    void putExistingMarketHistoryJobResult() throws Exception {
//        // Initialize the database
//        marketHistoryJobResultRepository.saveAndFlush(marketHistoryJobResult);
//
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//
//        // Update the marketHistoryJobResult
//        MarketHistoryJobResult updatedMarketHistoryJobResult = marketHistoryJobResultRepository
//            .findById(marketHistoryJobResult.getId())
//            .get();
//        // Disconnect from session so that the updates on updatedMarketHistoryJobResult are not directly saved in db
//        em.detach(updatedMarketHistoryJobResult);
//        updatedMarketHistoryJobResult
//            .is_success(UPDATED_IS_SUCCESS)
//            .time_start(UPDATED_TIME_START)
//            .time_end(UPDATED_TIME_END)
//            .error(UPDATED_ERROR)
//            .symbols(UPDATED_SYMBOLS);
//
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                put(ENTITY_API_URL_ID, updatedMarketHistoryJobResult.getId())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(updatedMarketHistoryJobResult))
//            )
//            .andExpect(status().isOk());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//        MarketHistoryJobResult testMarketHistoryJobResult = marketHistoryJobResultList.get(marketHistoryJobResultList.size() - 1);
//        assertThat(testMarketHistoryJobResult.getIs_success()).isEqualTo(UPDATED_IS_SUCCESS);
//        assertThat(testMarketHistoryJobResult.getTime_start()).isEqualTo(UPDATED_TIME_START);
//        assertThat(testMarketHistoryJobResult.getTime_end()).isEqualTo(UPDATED_TIME_END);
//        assertThat(testMarketHistoryJobResult.getError()).isEqualTo(UPDATED_ERROR);
//        assertThat(testMarketHistoryJobResult.getSymbols()).isEqualTo(UPDATED_SYMBOLS);
//    }
//
//    @Test
//    @Transactional
//    void putNonExistingMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//        marketHistoryJobResult.setId(count.incrementAndGet());
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                put(ENTITY_API_URL_ID, marketHistoryJobResult.getId())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void putWithIdMismatchMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//        marketHistoryJobResult.setId(count.incrementAndGet());
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                put(ENTITY_API_URL_ID, count.incrementAndGet())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void putWithMissingIdPathParamMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//        marketHistoryJobResult.setId(count.incrementAndGet());
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                put(ENTITY_API_URL)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isMethodNotAllowed());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void partialUpdateMarketHistoryJobResultWithPatch() throws Exception {
//        // Initialize the database
//        marketHistoryJobResultRepository.saveAndFlush(marketHistoryJobResult);
//
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//
//        // Update the marketHistoryJobResult using partial update
//        MarketHistoryJobResult partialUpdatedMarketHistoryJobResult = new MarketHistoryJobResult();
//        partialUpdatedMarketHistoryJobResult.setId(marketHistoryJobResult.getId());
//
//        partialUpdatedMarketHistoryJobResult
//            .is_success(UPDATED_IS_SUCCESS)
//            .time_start(UPDATED_TIME_START)
//            .time_end(UPDATED_TIME_END)
//            .error(UPDATED_ERROR)
//            .symbols(UPDATED_SYMBOLS);
//
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, partialUpdatedMarketHistoryJobResult.getId())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMarketHistoryJobResult))
//            )
//            .andExpect(status().isOk());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//        MarketHistoryJobResult testMarketHistoryJobResult = marketHistoryJobResultList.get(marketHistoryJobResultList.size() - 1);
//        assertThat(testMarketHistoryJobResult.getIs_success()).isEqualTo(UPDATED_IS_SUCCESS);
//        assertThat(testMarketHistoryJobResult.getTime_start()).isEqualTo(UPDATED_TIME_START);
//        assertThat(testMarketHistoryJobResult.getTime_end()).isEqualTo(UPDATED_TIME_END);
//        assertThat(testMarketHistoryJobResult.getError()).isEqualTo(UPDATED_ERROR);
//        assertThat(testMarketHistoryJobResult.getSymbols()).isEqualTo(UPDATED_SYMBOLS);
//    }
//
//    @Test
//    @Transactional
//    void fullUpdateMarketHistoryJobResultWithPatch() throws Exception {
//        // Initialize the database
//        marketHistoryJobResultRepository.saveAndFlush(marketHistoryJobResult);
//
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//
//        // Update the marketHistoryJobResult using partial update
//        MarketHistoryJobResult partialUpdatedMarketHistoryJobResult = new MarketHistoryJobResult();
//        partialUpdatedMarketHistoryJobResult.setId(marketHistoryJobResult.getId());
//
//        partialUpdatedMarketHistoryJobResult
//            .is_success(UPDATED_IS_SUCCESS)
//            .time_start(UPDATED_TIME_START)
//            .time_end(UPDATED_TIME_END)
//            .error(UPDATED_ERROR)
//            .symbols(UPDATED_SYMBOLS);
//
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, partialUpdatedMarketHistoryJobResult.getId())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMarketHistoryJobResult))
//            )
//            .andExpect(status().isOk());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//        MarketHistoryJobResult testMarketHistoryJobResult = marketHistoryJobResultList.get(marketHistoryJobResultList.size() - 1);
//        assertThat(testMarketHistoryJobResult.getIs_success()).isEqualTo(UPDATED_IS_SUCCESS);
//        assertThat(testMarketHistoryJobResult.getTime_start()).isEqualTo(UPDATED_TIME_START);
//        assertThat(testMarketHistoryJobResult.getTime_end()).isEqualTo(UPDATED_TIME_END);
//        assertThat(testMarketHistoryJobResult.getError()).isEqualTo(UPDATED_ERROR);
//        assertThat(testMarketHistoryJobResult.getSymbols()).isEqualTo(UPDATED_SYMBOLS);
//    }
//
//    @Test
//    @Transactional
//    void patchNonExistingMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//        marketHistoryJobResult.setId(count.incrementAndGet());
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, marketHistoryJobResult.getId())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void patchWithIdMismatchMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//        marketHistoryJobResult.setId(count.incrementAndGet());
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                patch(ENTITY_API_URL_ID, count.incrementAndGet())
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isBadRequest());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void patchWithMissingIdPathParamMarketHistoryJobResult() throws Exception {
//        int databaseSizeBeforeUpdate = marketHistoryJobResultRepository.findAll().size();
//        marketHistoryJobResult.setId(count.incrementAndGet());
//
//        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
//        restMarketHistoryJobResultMockMvc
//            .perform(
//                patch(ENTITY_API_URL)
//                    .contentType("application/merge-patch+json")
//                    .content(TestUtil.convertObjectToJsonBytes(marketHistoryJobResult))
//            )
//            .andExpect(status().isMethodNotAllowed());
//
//        // Validate the MarketHistoryJobResult in the database
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    void deleteMarketHistoryJobResult() throws Exception {
//        // Initialize the database
//        marketHistoryJobResultRepository.saveAndFlush(marketHistoryJobResult);
//
//        int databaseSizeBeforeDelete = marketHistoryJobResultRepository.findAll().size();
//
//        // Delete the marketHistoryJobResult
//        restMarketHistoryJobResultMockMvc
//            .perform(delete(ENTITY_API_URL_ID, marketHistoryJobResult.getId()).accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isNoContent());
//
//        // Validate the database contains one less item
//        List<MarketHistoryJobResult> marketHistoryJobResultList = marketHistoryJobResultRepository.findAll();
//        assertThat(marketHistoryJobResultList).hasSize(databaseSizeBeforeDelete - 1);
//    }
//}
