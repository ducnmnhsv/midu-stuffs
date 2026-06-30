ALTER TABLE `tradex-configuration`.`t_client`
ADD COLUMN `open_api_server` VARCHAR(255) NULL AFTER `domain`,
ADD COLUMN `open_api_url` VARCHAR(255) NULL AFTER `open_api_server`;
