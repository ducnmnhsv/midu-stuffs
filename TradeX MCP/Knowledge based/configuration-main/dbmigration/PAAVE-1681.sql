ALTER TABLE `tradex-configuration`.`t_client` ADD COLUMN `app_version` VARCHAR(64) NULL AFTER `description`;
ALTER TABLE `tradex-configuration`.`t_client` CHANGE COLUMN `app_version` `app_version` TEXT NULL DEFAULT NULL ;

UPDATE `tradex-configuration`.`t_client` SET `app_version` = '{\"IOS\":\"1.6.0\",\"ANDROID\":\"1.6.0\"}' WHERE (`client_id` = 'paave');
UPDATE `tradex-configuration`.`t_client` SET `app_version` = '{\"IOS\":\"1.6.0\",\"ANDROID\":\"1.6.0\"}' WHERE (`client_id` = 'paavetest');
UPDATE `tradex-configuration`.`t_client` SET `app_version` = '{\"IOS\":\"1.6.0\",\"ANDROID\":\"1.6.0\"}' WHERE (`client_id` = 'paave-non-login');