declare module global {
  namespace NodeJS {
    interface Global {
      [key: string]: any,
    }
  }
}