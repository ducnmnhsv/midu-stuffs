CREATE TABLE `t_fpt_sms_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `brand_name` varchar(45) DEFAULT NULL,
  `telco` varchar(45) DEFAULT NULL,
  `phone_number` varchar(45) DEFAULT NULL,
  `message_type` varchar(45) DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  `status` varchar(45) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `fail_reason` varchar(5000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
