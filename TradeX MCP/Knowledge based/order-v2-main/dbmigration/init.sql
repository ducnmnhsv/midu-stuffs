-- ----------------------------
-- Table structure for t_stop_order
-- ----------------------------
DROP TABLE IF EXISTS `t_stop_order`;
CREATE TABLE `t_stop_order`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(45)  NULL DEFAULT '',
  `quantity` int(11) NOT NULL,
  `sell_buy_type` enum('SELL','BUY')  NOT NULL,
  `stop_price` decimal(11, 2) NOT NULL,
  `stop_volume` int(11) NULL DEFAULT NULL,
  `order_price` decimal(11, 2) NULL DEFAULT NULL,
  `order_type` enum('STOP','STOP_LIMIT')  NOT NULL DEFAULT 'STOP',
  `username` varchar(255)  NULL DEFAULT NULL COMMENT 'clientID',
  `trading_acc_seq` varchar(255)  NULL DEFAULT NULL COMMENT 'trading_acc_seq return after login (only for mas)',
  `account_number` varchar(255)  NOT NULL COMMENT 'accountNo',
  `order_number` varchar(255)  NULL DEFAULT NULL COMMENT 'the real order number that placed',
  `status` enum('PENDING','COMPLETED','CANCELLED','FAILED','SENDING')  NOT NULL DEFAULT 'PENDING',
  `ordered_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_by` varchar(255)  NULL DEFAULT NULL,
  `fail_reason` longtext  NULL,
  `securities_type` enum('STOCK','FUND','BOND','ETF','CW','FUTURES')  NULL DEFAULT NULL,
  `from_date` timestamp(0) NOT NULL,
  `to_date` timestamp(0) NOT NULL,
  `header` json NULL DEFAULT NULL,
  `source_ip` varchar(255)  NULL DEFAULT NULL,
  `created_by` bigint(20) NULL DEFAULT NULL,
  `updated_by` bigint(20) NULL DEFAULT NULL,
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  'deviceUniqueId' varchar(255) NULL DEFAULT NULL,
  'remark' varchar(255) NULL DEFAULT NULL,
  'macAddress' varchar(255) NULL DEFAULT NULL,
+  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_trailing_order
-- ----------------------------
DROP TABLE IF EXISTS `t_trailing_order`;
CREATE TABLE `t_trailing_order`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(45)  NOT NULL DEFAULT '',
  `quantity` int(11) NOT NULL,
  `sell_buy_type` enum('SELL','BUY')  NOT NULL,
  `trailing_amount` int(255) NOT NULL,
  `limit_offset` decimal(11, 2) NOT NULL,
  `current_price` decimal(11, 2) NOT NULL,
  `stop_price` decimal(11, 2) NULL DEFAULT NULL,
  `username` varchar(255)  NULL DEFAULT NULL COMMENT 'clientID',
  `trading_acc_seq` varchar(255)  NULL DEFAULT NULL COMMENT 'trading_acc_seq return after login (only for mas)',
  `account_number` varchar(255)  NOT NULL COMMENT 'accountNo',
  `sub_number` varchar(255)  NULL DEFAULT '' COMMENT 'sub number to place the order',
  `order_number` varchar(255)  NULL DEFAULT NULL COMMENT 'the real order number that placed',
  `status` enum('PENDING','COMPLETED','CANCELLED','FAILED','SENDING')  NOT NULL DEFAULT 'PENDING',
  `ordered_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_by` varchar(255)  NULL DEFAULT NULL,
  `fail_reason` longtext  NULL,
  `bank_code` varchar(10)  NULL DEFAULT NULL,
  `bank_account` varchar(255)  NULL DEFAULT NULL,
  `bank_name` varchar(255)  NULL DEFAULT NULL,
  `securities_type` enum('STOCK','FUND','BOND','ETF','CW','FUTURES')  NULL DEFAULT NULL,
  `source_ip` varchar(25)  NULL DEFAULT NULL,
  `error_code` varchar(25)  NULL DEFAULT NULL,
  `header` json NULL DEFAULT NULL,
  `created_by` bigint(20) NULL DEFAULT NULL,
  `updated_by` bigint(20) NULL DEFAULT NULL,
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bull_bear_order
-- ----------------------------
DROP TABLE IF EXISTS `t_bull_bear_order`;
CREATE TABLE `t_bull_bear_order`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(45)  NOT NULL DEFAULT '',
  `quantity` int(11) NOT NULL,
  `match_quantity` int(11) NOT NULL,
  `sell_buy_type` enum('SELL','BUY')  NOT NULL,
  `order_price` decimal(11, 2) NOT NULL COMMENT 'giá đặt cho lệnh gốc',
  `profit_price` decimal(11, 2) NOT NULL COMMENT 'giá chốt lãi',
  `trigger_loss_price` decimal(11, 2) NOT NULL COMMENT 'giá kích hoạt cắt lỗ',
  `toler` decimal(11, 2) NOT NULL COMMENT 'biên trượt để thiết lập giá cắt lỗ',
  `username` varchar(255)  NULL DEFAULT NULL COMMENT 'clientID',
  `trading_acc_seq` varchar(255)  NULL DEFAULT NULL COMMENT 'trading_acc_seq return after login (only for mas)',
  `account_number` varchar(255)  NOT NULL COMMENT 'accountNo',
  `sub_number` varchar(255)  NULL DEFAULT '' COMMENT 'sub number to place the order',
  `order_number` varchar(255)  NULL DEFAULT NULL COMMENT 'the real order number that placed',
  `status` enum('PENDING','ACTIVATED','CANCELLED','FAILED','SENDING')  NOT NULL DEFAULT 'PENDING',
  `ordered_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_by` varchar(255)  NULL DEFAULT NULL,
  `fail_reason` longtext  NULL,
  `error_code` varchar(25)  NULL DEFAULT NULL,
  `securities_type` enum('STOCK','FUND','BOND','ETF','CW','FUTURES')  NULL DEFAULT NULL,
  `source_ip` varchar(255)  NULL DEFAULT NULL,
  `header` json NULL DEFAULT NULL,
  `created_by` bigint(20) NULL DEFAULT NULL,
  `updated_by` bigint(20) NULL DEFAULT NULL,
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_oco_order
-- ----------------------------
DROP TABLE IF EXISTS `t_oco_order`;
CREATE TABLE `t_oco_order`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bull_bear_id` int(11) NULL,
  `code` varchar(45)  NOT NULL DEFAULT '',
  `quantity` int(11) NOT NULL,
  `match_quantity` int(11) NOT NULL,
  `unmatch_quantity` int(11) NOT NULL,
  `sell_buy_type` enum('SELL','BUY')  NOT NULL,
  `current_price` decimal(11, 2) NOT NULL COMMENT 'giá đặt cho lệnh gốc',
  `profit_price` decimal(11, 2) NOT NULL COMMENT 'giá chốt lãi',
  `trigger_loss_price` decimal(11, 2) NOT NULL COMMENT 'giá kích hoạt cắt lỗ',
  `toler` decimal(11, 2) NOT NULL COMMENT 'biên trượt để thiết lập giá cắt lỗ',
  `source_ip` varchar(255)  NULL DEFAULT NULL,
  `securities_type` enum('STOCK','FUND','BOND','ETF','CW','FUTURES')  NULL DEFAULT NULL,
  `username` varchar(255)  NULL DEFAULT NULL COMMENT 'clientID',
  `trading_acc_seq` varchar(255)  NULL DEFAULT NULL COMMENT 'trading_acc_seq return after login (only for mas)',
  `account_number` varchar(255)  NOT NULL COMMENT 'accountNo',
  `sub_number` varchar(255)  NULL DEFAULT '' COMMENT 'sub number to place the order',
  `order_number` varchar(255)  NULL DEFAULT NULL COMMENT 'the real order number that placed',
  `status` enum('PENDING','ACTIVATED','COMPLETED','CANCELLED','FAILED','EXPIRED')  NOT NULL DEFAULT 'PENDING',
  `ordered_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_by` varchar(255)  NULL DEFAULT NULL,
  `fail_reason` longtext  NULL,
  `error_code` varchar(25)  NULL DEFAULT NULL,
  `header` json NULL DEFAULT NULL,
  `created_by` bigint(20) NULL DEFAULT NULL,
  `updated_by` bigint(20) NULL DEFAULT NULL,
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_profit_loss_order (using to open position / cut loss/ take profit)
-- ----------------------------
DROP TABLE IF EXISTS `t_profit_loss_order`;
CREATE TABLE `t_profit_loss_order`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bull_bear_id` int(11) NULL,
  `oco_id` int(11) NULL,
  `code` varchar(45)  NOT NULL DEFAULT '',
  `quantity` int(11) NOT NULL,
  `match_quantity` int(11) NOT NULL,
  `cancelled_quantity` int(11) NOT NULL,
  `sell_buy_type` enum('SELL','BUY')  NOT NULL,
  `order_price` decimal(11, 2) NOT NULL COMMENT 'giá đặt',
  `username` varchar(255)  NULL DEFAULT NULL,
  `securities_type` enum('STOCK','FUND','BOND','ETF','CW','FUTURES')  NULL DEFAULT NULL,
  `source_ip` varchar(255)  NULL DEFAULT NULL,
  `account_number` varchar(255)  NOT NULL COMMENT 'account number to place the order',
  `sub_number` varchar(255)  NULL DEFAULT '' COMMENT 'sub number to place the order',
  `order_number` varchar(255)  NULL DEFAULT NULL COMMENT 'the real order number that placed',
  `order_group_number` varchar(255)  NULL DEFAULT NULL COMMENT 'the real group order number that placed (only for mas)',
  `trading_acc_seq` varchar(255)  NULL DEFAULT NULL COMMENT 'trading_acc_seq return after login (only for mas)',
  `status` enum('PENDING','COMPLETED','CANCELLED','FAILED','SENDING')  NOT NULL DEFAULT 'PENDING',
  `profit_loss_type` enum('OPEN_POSITION','CUT_LOSS','TAKE_PROFIT')  NOT NULL DEFAULT 'OPEN_POSITION',
  `ordered_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_at` timestamp(0) NULL DEFAULT NULL,
  `cancelled_by` varchar(255)  NULL DEFAULT NULL,
  `fail_reason` longtext  NULL,
  `error_code` varchar(25)  NULL DEFAULT NULL,
  `bank_name` varchar(255)  NULL DEFAULT NULL,
  `bank_code` varchar(10)  NULL DEFAULT NULL,
  `bank_account` varchar(255)  NULL DEFAULT NULL,
  `header` json NULL DEFAULT NULL,
  `created_by` bigint(20) NULL DEFAULT NULL,
  `updated_by` bigint(20) NULL DEFAULT NULL,
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  ROW_FORMAT = Dynamic;