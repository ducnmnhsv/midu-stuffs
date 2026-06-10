import { Service } from 'typedi';
import { EntityRepository, Repository } from 'typeorm';
import { HeaderTokenUserData } from '../models/db/HeaderTokenUserData';

@Service()
@EntityRepository(HeaderTokenUserData)
export class HeaderTokenUserDataRepository extends Repository<HeaderTokenUserData> {}
