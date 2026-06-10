import {createPool, Pool} from "mysql";

let pool: Pool;
let isInit: boolean = false;

function init(cfg: any): void {
  if (isInit) {
    return;
  }
  isInit = true;
  pool = createPool({
    ...{
      connectionLimit: 10,
      host: "localhost",
      user: "bob",
      password: "secret",
      database: "my_db",
      typeCast: (field: any, useDefaultTypeCasting: any) => {
        if ((field.type === "BIT") && (field.length === 1)) {
          const bytes = field.buffer();
          return (bytes[0] === 1);
        }
        return (useDefaultTypeCasting());
      }
    }, ...cfg,
  });
}

function getPool(): Pool {
  return pool;
}

export {
  init,
  getPool,
};
