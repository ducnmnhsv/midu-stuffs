export default interface IChangeInfoAccessGrantedRequest{
  partnerId: string;
  userId: number;
  changeInfoAccessGranted?: boolean;
}