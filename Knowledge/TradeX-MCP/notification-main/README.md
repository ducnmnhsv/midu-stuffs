# Notification Service

A comprehensive notification service built with Spring Boot that handles multiple communication channels including email, SMS, push notifications, and messaging platforms.

## 🚀 Overview

The Notification Service is a microservice designed to handle various types of notifications across different platforms. It supports multiple notification channels and integrates with external services like OneSignal, KakaoTalk, Zalo, and SMS providers.

## ✨ Features

- **Multi-channel Notifications**: Email, SMS, Push notifications, In-app messaging
- **Template Engine**: FreeMarker-based templates with multi-language support (EN, KO, VI)
- **External Integrations**: 
  - OneSignal for push notifications
  - KakaoTalk for messaging
  - Zalo for Vietnamese market
  - FPT SMS for SMS delivery
- **Message Queuing**: Kafka integration for reliable message processing
- **Caching**: Redis integration for performance optimization
- **Async Processing**: Asynchronous notification delivery
- **Retry Mechanism**: Built-in retry logic for failed notifications
- **Health Monitoring**: Health check endpoints for monitoring

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Kafka Topics  │    │  Notification   │    │  External      │
│                 │    │     Service     │    │  Services      │
│ • paave-notif   │───▶│                 │───▶│ • OneSignal    │
│ • tradex        │    │ • Email         │    │ • KakaoTalk    │
│ • ws.broadcast  │    │ • SMS           │    │ • Zalo         │
└─────────────────┘    │ • Push Notif    │    │ • FPT SMS      │
                       │ • WebSocket     │    └─────────────────┘
                       └─────────────────┘
```

## 🛠️ Technology Stack

- **Java**: 8
- **Framework**: Spring Boot 2.0.0.RELEASE
- **Build Tool**: Maven
- **Database**: MySQL
- **Cache**: Redis
- **Message Queue**: Apache Kafka 2.3.1
- **Template Engine**: FreeMarker 2.3.26
- **Web Services**: Spring Web Services (SOAP)
- **Testing**: JUnit 4 & 5

## 📋 Prerequisites

- Java 8 (JDK 1.8)
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+
- Apache Kafka 2.3.1
- Docker (optional)

## 🚀 Quick Start

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd notification
   ```

2. **Build dependencies first**
   ```bash
   # Build tradex-common-java and compile notification service
   ./build-deps.sh
   
   # Or build everything including Docker image
   ./build-and-docker.sh
   ```

3. **Configure environment variables**
   ```bash
   export ER_ENV=dev
   export TRADEX_ENV_DOMAIN=tradex
   export TRADEX_ENV_NODE_ID=1
   ```

4. **Update database configuration**
   Edit `src/main/resources/application.yaml` with your database credentials

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Docker Deployment

1. **Build the Docker image (includes dependency building)**
   ```bash
   ./build-and-docker.sh
   ```

2. **Or build manually**
   ```bash
   # First build dependencies
   ./build-deps.sh
   
   # Then build Docker image
   docker build -t notification-service .
   ```

3. **Run the container**
   ```bash
   docker run -d \
     --name notification-service \
     -p 8080:8080 \
     -e ER_ENV=prod \
     notification-service:latest
   ```

## 🔧 Build Scripts

### `build-deps.sh`
Builds the `tradex-common-java` dependency and compiles the notification service. Use this for development.

```bash
./build-deps.sh
```

### `build-and-docker.sh`
Complete build process: builds dependencies, compiles the service, and creates a Docker image.

```bash
./build-and-docker.sh
```

### Manual Build Steps
If you prefer to build manually:

```bash
# 1. Build tradex-common-java
cd ../tradex-common-java
mvn clean install -DskipTests

# 2. Build notification service
cd ../notification
mvn clean package -DskipTests

# 3. Build Docker image (optional)
docker build -t notification-service .
```

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.yaml`:

```yaml
app:
  kafkaUrl: 172.31.43.101:9092
  email:
    endpoint: email-smtp.us-west-2.amazonaws.com
    port: 587
    sender: noreply@tradex.vn
  oneSignal:
    appId: your-app-id
    apiKey: your-api-key
  kakao:
    url: https://gw.surem.com/alimtalk/v2/json
    companyCode: your-company-code
  zalo:
    accessToken: your-access-token
    sendMessageUrl: https://openapi.zalo.me/v2.0/oa/message
```

### Environment Variables

- `ER_ENV`: Environment profile (dev, staging, prod)
- `TRADEX_ENV_DOMAIN`: Domain identifier
- `TRADEX_ENV_NODE_ID`: Node identifier for clustering

## 📱 Notification Channels

### Email Notifications
- SMTP integration with AWS SES
- HTML and text email templates
- Multi-language support (EN, KO, VI)
- Template-based content generation

### SMS Notifications
- FPT SMS integration
- OTP and verification messages
- Multi-language SMS templates

### Push Notifications
- OneSignal integration
- Cross-platform push notifications
- Rich media support

### Messaging Platforms
- **KakaoTalk**: Korean market integration
- **Zalo**: Vietnamese market integration
- **WebSocket**: Real-time in-app notifications

## 🔄 Message Processing

### Kafka Topics
- `paave-notification`: Paave-specific notifications
- `tradex`: Tradex platform notifications
- `ws.broadcast`: WebSocket broadcast messages

### Message Flow
1. Message received via Kafka or HTTP
2. Template selection based on notification type
3. Content generation using FreeMarker
4. Delivery to appropriate channel
5. Retry mechanism for failed deliveries

## 📊 Monitoring & Health

### Health Check Endpoint
```
GET /actuator/health
```

### Logging
- Application logs: `application.log`
- Log levels configurable per package
- Structured logging for better monitoring

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestService

# Run with specific profile
mvn test -Dspring.profiles.active=test
```

## 📁 Project Structure

```
src/main/java/com/techx/tradex/notification/
├── Application.java                 # Main application class
├── configurations/                  # Configuration classes
├── constants/                       # Constants and enums
├── controllers/                     # HTTP request handlers
├── dao/                            # Data access objects
├── model/                          # Data models and DTOs
├── repository/                     # JPA repositories
├── services/                       # Business logic services
└── util/                           # Utility classes

src/main/resources/
├── application.yaml                # Application configuration
├── templates/                      # FreeMarker templates
└── wsdl/                          # SOAP service definitions
```

## 🔧 Development

### Adding New Notification Types

1. Create template files in `src/main/resources/templates/`
2. Add template mapping in `application.yaml`
3. Implement service logic in appropriate service class
4. Add configuration if needed

### Adding New Channels

1. Create new service class extending base notification service
2. Implement channel-specific delivery logic
3. Add configuration properties
4. Update template mappings

## 🚨 Troubleshooting

### Common Issues

1. **Dependency Build Failures**: Ensure `tradex-common-java` builds successfully first
2. **Database Connection**: Check MySQL credentials and network access
3. **Kafka Connection**: Verify Kafka broker availability
4. **Template Errors**: Check FreeMarker template syntax
5. **External Service Failures**: Verify API keys and service availability

### Debug Mode

Enable debug logging:
```yaml
logging:
  level:
    com.techx.tradex: DEBUG
```

## 📄 License

This project is proprietary software owned by TechX TradeX.

## 🤝 Contributing

Please contact the development team for contribution guidelines.

## 📞 Support

For technical support and questions:
- Email: admin@tradex.vn
- Support: admin@tradex.vn

---

**Version**: 1.0-SNAPSHOT  
**Last Updated**: 2024
