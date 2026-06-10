interface ICustomerProfile {
    email: string,
    address: string,
    userName: string,
    accountNo: string,
    telephone: string,
    branchName: string,
    customerID: string,
    brokersName: string,
    mobilePhone: string,
    brokersEmail: string,
    customerType: string,
    IDNumberPassport: string,
    authorizedPerson: true,
    brokersContactNo: string
}
interface IAuthorizedPerson {
    email: string,
    exist: true,
    address: string,
    telephone: string,
    IDCardPassport: string,
    authorizedPersonsID: string,
    authorizedPersonsName: string
}
export default interface IClientDetailResponse {
    customerProfile: ICustomerProfile
    authorizedPerson: IAuthorizedPerson
}