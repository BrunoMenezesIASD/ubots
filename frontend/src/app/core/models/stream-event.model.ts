export interface StreamEvent<T = any> {
  type: string;
  at: string;
  payload: T;
}
