import Service from "../models/db/Service";
import { AppDataSource } from "../AppDataSource";

export const ServiceRepository = AppDataSource.getRepository(Service).extend(
  {},
);
