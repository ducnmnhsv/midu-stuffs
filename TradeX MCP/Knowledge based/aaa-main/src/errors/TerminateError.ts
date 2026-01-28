export default class TerminateError extends Error {
  constructor(public readonly message: any, public readonly source: Error) {
    super();
  }
}
