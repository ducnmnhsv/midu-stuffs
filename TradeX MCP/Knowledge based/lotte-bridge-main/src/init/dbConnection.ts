import config from '../config';
import { createConnection, useContainer } from 'typeorm';
import { Container } from 'typedi';

useContainer(Container);

const connection = createConnection({
  ...config.db.mysql,
  type: 'mysql',
  entities: [`${__dirname}/../../**/models/db/**/*.js`, `${__dirname}/models/*.js`],
});

export default connection;
