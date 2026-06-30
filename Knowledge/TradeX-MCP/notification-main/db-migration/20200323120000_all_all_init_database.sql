CREATE DATABASE IF NOT EXISTS `tradex-notification`;

-- ----------------------------
-- Table structure for t_notification_list
-- ----------------------------
DROP TABLE IF EXISTS `tradex-notification`.`t_notification_list`;
CREATE TABLE `tradex-notification`.`t_notification_list`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `message_type` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `to` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `cc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `bcc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_notification_type
-- ----------------------------
DROP TABLE IF EXISTS `tradex-notification`.`t_notification_type`;
CREATE TABLE `tradex-notification`.`t_notification_type`  (
  `id` int(11) NOT NULL,
  `type` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;