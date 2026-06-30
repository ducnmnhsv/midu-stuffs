import chatRoom from 'app/entities/chat-room/chat-room.reducer';
import createdChatRoom from 'app/entities/created-chat-room/created-chat-room.reducer';
import socialLink from 'app/entities/social-link/social-link.reducer';
import inviteUser from 'app/entities/invite-user/invite-user.reducer';
import broker from 'app/entities/broker/broker.reducer';
import recentViewChatRoom from 'app/entities/recent-view-chat-room/recent-view-chat-room.reducer';
import copySubscriber from 'app/entities/copy-subscriber/copy-subscriber.reducer';
import copySubscriberHistory from 'app/entities/copy-subscriber-history/copy-subscriber-history.reducer';
import copyPortfolio from 'app/entities/copy-portfolio/copy-portfolio.reducer';
import copyPortfolioHistory from 'app/entities/copy-portfolio-history/copy-portfolio-history.reducer';
import copyPortfolioDetails from 'app/entities/copy-portfolio-details/copy-portfolio-details.reducer';
import copyPortfolioDetailHistory from 'app/entities/copy-portfolio-detail-history/copy-portfolio-detail-history.reducer';
import copyMarketLeaderDetails from 'app/entities/copy-market-leader-details/copy-market-leader-details.reducer';
import copyTradingOrder from 'app/entities/copy-trading-order/copy-trading-order.reducer';
import marketHistoryJobResult from 'app/entities/market-history-job-result/market-history-job-result.reducer';
import customMarketHistoryJobResult from 'app/custom/entities/market-history-job-result/market-history-job-result.reducer';
import copyTradingRegister from 'app/entities/copy-trading-register/copy-trading-register.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  chatRoom,
  createdChatRoom,
  socialLink,
  inviteUser,
  broker,
  recentViewChatRoom,
  copySubscriber,
  copySubscriberHistory,
  copyPortfolio,
  copyPortfolioHistory,
  copyPortfolioDetails,
  copyPortfolioDetailHistory,
  copyMarketLeaderDetails,
  copyTradingOrder,
  marketHistoryJobResult,
  customMarketHistoryJobResult,
  copyTradingRegister,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
