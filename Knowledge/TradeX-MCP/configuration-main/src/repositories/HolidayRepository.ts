import Holiday from "../models/db/Holiday";
import { AppDataSource } from "../AppDataSource";

export const HolidayRepository = AppDataSource.getRepository(Holiday).extend(
  {},
);
