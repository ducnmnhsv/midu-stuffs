export default interface ICommonLoginRes {
  id: number;
  username: string;
  displayName: string;
  email: string;
  avatar: string;
  phoneNumber: string;
  dob: string;
  status: string;
  userRoles: string[];
}
