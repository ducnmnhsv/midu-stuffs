import Service from "../db/Service";

const parse = (service: Service): ServiceResponse => {
  const response: ServiceResponse = new ServiceResponse();
  if (service != null) {
    response.serviceName = service.serviceName;
    response.serviceCode = service.serviceCode;
    response.supportPhone = service.supportPhone;
    response.supportEmail = service.supportEmail;
    response.logoUrl = service.logoUrl;
  }

  return response;
};

class ServiceResponse {
  public serviceName: string;
  public serviceCode: string;
  public supportPhone: string;
  public supportEmail: string;
  public logoUrl: string;
}

export { parse, ServiceResponse };
