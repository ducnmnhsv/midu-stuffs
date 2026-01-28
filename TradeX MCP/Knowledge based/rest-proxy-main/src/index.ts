import { Container } from 'typedi';
import ScopeService from './services/ScopeService';
import {Logger} from "tradex-common";
import initServer from './server';

Logger.info('staring...');
const init = async () => {
  const scopeService = Container.get(ScopeService);
  await scopeService.init();

  await initServer()
};

init()
  .then()
  .catch((error: any) => Logger.error(error));
