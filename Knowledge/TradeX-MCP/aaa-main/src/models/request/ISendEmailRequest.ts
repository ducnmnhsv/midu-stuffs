export default interface ISendEmailRequest{
    to: string;
    emailtemplate: string;
    subject: string;
    fullname?: string;
}