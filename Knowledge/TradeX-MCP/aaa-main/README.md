# AAA Service - Authentication, Authorization, and Accounting Server

A comprehensive OAuth2-compliant authentication and authorization server built with Node.js, TypeScript, and Kafka for microservice communication.

## 🚀 Overview

The AAA Service is a robust authentication and authorization server that follows OAuth2 specifications. It provides secure user authentication through multiple methods including password-based login, social login (Google, Facebook, Apple), biometric authentication, OTP verification, and more. The service is designed to work in a microservices architecture using Kafka for message-based communication.

## ✨ Features

### 🔐 Authentication Methods
- **Password-based authentication** with RSA encryption
- **Social login integration** (Google, Facebook, Apple, TechX)
- **Biometric authentication** (Face ID, fingerprint)
- **OTP verification** (SMS, mobile app)
- **Domain-based access** for enterprise users
- **Client credentials** for service-to-service authentication
- **Demo account** support for testing
- **Link account** functionality for multi-provider authentication

### 🛡️ Security Features
- JWT token management with configurable expiration
- RSA encryption for sensitive data
- Scope-based access control
- Client secret management
- Rate limiting for OTP requests
- App version validation
- Secure token refresh and revocation

### 🔄 Integration Capabilities
- **Kafka-based messaging** for microservice communication
- **MySQL database** for persistent storage
- **Redis caching** for performance optimization
- **Zookeeper** for service discovery
- **RESTful API** endpoints
- **Multi-tenant support** with domain-based configuration

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │   Kafka Topics  │    │   AAA Service   │
│                 │◄──►│                 │◄──►│                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Zookeeper     │    │   MySQL/Redis   │
                       │  (Service Disc) │    │   (Storage)     │
                       └─────────────────┘    └─────────────────┘
```

### Core Components

- **Authentication Service**: Handles all login methods and user validation
- **Token Service**: Manages JWT tokens, refresh tokens, and token lifecycle
- **Scope Service**: Handles permission and scope management
- **Client Service**: Manages OAuth2 client applications
- **User Service**: Handles user profile and account management
- **OTP Service**: Manages one-time password generation and verification
- **Biometric Service**: Handles biometric authentication flows
- **Link Account Service**: Manages multi-provider account linking

## 🛠️ Technology Stack

- **Runtime**: Node.js 16+ (Alpine Linux)
- **Language**: TypeScript 5.2+
- **Framework**: Custom microservice framework
- **Database**: MySQL 8.0+
- **Cache**: Redis 3.0+
- **Message Broker**: Apache Kafka
- **Service Discovery**: Apache Zookeeper
- **Container**: Docker with multi-stage builds
- **Authentication**: JWT, RSA encryption
- **Package Manager**: npm

## 📋 Prerequisites

- Node.js 16+ (LTS recommended)
- MySQL 8.0+
- Redis 6.0+
- Apache Kafka 2.8+
- Apache Zookeeper 3.6+
- Docker (for containerized deployment)

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd aaa
```

### 2. Install Dependencies
```bash
nvm install
nvm use
npm install
```

### 3. Environment Configuration
Create an `env.js` file in the `src` directory with your configuration:

```javascript
// src/env.js
conf.domain = "your-domain";
conf.db.host = "your-mysql-host";
conf.db.user = "your-mysql-user";
conf.db.password = "your-mysql-password";
conf.db.database = "tradex-aaa";
conf.redis.host = "your-redis-host";
conf.redis.port = 6379;
conf.kafkaUrls = ["your-kafka-broker:9092"];
```

### 4. Database Setup
Create the MySQL database and run migrations:
```sql
CREATE DATABASE `tradex-aaa`;
```

### 5. Build the Project
```bash
npm run build
```

### 6. Run the Service
```bash
npm run run-local
```

## 🐳 Docker Deployment

### Build the Image
```bash
docker build -t aaa-service .
```

### Run the Container
```bash
docker run -d \
  --name aaa-service \
  -p 3000:3000 \
  -v /path/to/keys:/app/keys \
  -v /path/to/logs:/logs \
  aaa-service
```

## 🔧 Configuration

### Key Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `domain` | Service domain | `tradex` |
| `enableBiometric` | Enable biometric auth | `false` |
| `enableHandleOtp` | Handle OTP verification | `true` |
| `accessToken.expiredInSeconds` | Access token TTL | `900` (15 min) |
| `refreshToken.expiredInSeconds` | Refresh token TTL | `86400` (24 hours) |
| `otpToken.expiredInSeconds` | OTP token TTL | `90` (1.5 min) |

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `TRADEX_ENV_DOMAIN` | Service domain | Yes |
| `TRADEX_ENV_MYSQL_HOST` | MySQL host | Yes |
| `TRADEX_ENV_MYSQL_USER` | MySQL username | Yes |
| `TRADEX_ENV_MYSQL_PASSWORD` | MySQL password | Yes |
| `TRADEX_ENV_KAFKA_URLS` | Kafka broker URLs | Yes |
| `TRADEX_ENV_NODE_ID` | Node identifier | Yes |
| `TRADEX_ENV_INSTANCE_ID` | Instance identifier | Yes |

## 📡 API Endpoints

### Authentication Endpoints
- `POST /api/v1/login` - Standard login
- `POST /api/v1/login/sec` - Secure login with OTP
- `POST /api/v1/login/social` - Social media login
- `POST /api/v1/login/biometric` - Biometric authentication
- `POST /api/v1/login/organization` - Organization login

### Token Management
- `POST /api/v1/refreshToken` - Refresh access token
- `POST /api/v1/revokeToken` - Revoke token

### User Management
- `POST /api/v1/registerMobileOtp` - Register mobile OTP
- `POST /api/v1/biometricRegister` - Register biometric
- `PUT /api/v1/user/profile` - Update user profile

### Account Linking
- `GET /api/v1/linkAccounts` - List linked accounts
- `POST /api/v1/linkAccounts` - Create account link
- `DELETE /api/v1/linkAccounts/{partnerId}` - Remove account link

### Scope Management
- `GET /api/v1/scopes/search` - Search scopes by group

## 🔐 Supported Grant Types

- `password` - Username/password authentication
- `demo` - Demo account access
- `password_otp` - Password + OTP verification
- `password_faceid` - Password + Face ID
- `access_google` - Google OAuth
- `access_facebook` - Facebook OAuth
- `access_apple` - Apple Sign-In
- `client_credentials` - Service-to-service auth
- `biometric` - Biometric-only authentication
- `link_account` - Multi-provider account linking

## 🗄️ Database Schema

The service uses MySQL with the following key tables:

- **Users**: User accounts and profiles
- **Clients**: OAuth2 client applications
- **LoginMethods**: Available authentication methods
- **Scopes**: Permission definitions
- **ScopeGroups**: Permission groupings
- **Tokens**: Access and refresh tokens
- **OTPs**: One-time password records
- **Biometrics**: Biometric authentication data
- **LinkAccounts**: Multi-provider account links

## 📊 Monitoring & Health Checks

The service includes built-in health checks and monitoring:

- **Health Check Endpoint**: `/currentTime`
- **Logging**: Structured logging with file rotation
- **Metrics**: Kafka consumer/producer metrics
- **Error Tracking**: Comprehensive error handling and logging

## 🔍 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL credentials and network connectivity
   - Check database exists and is accessible

2. **Kafka Connection Issues**
   - Verify Kafka broker URLs and network access
   - Check Zookeeper connectivity

3. **Authentication Failures**
   - Verify client credentials in database
   - Check RSA key files exist and are readable

4. **OTP Not Working**
   - Verify SMS service configuration
   - Check rate limiting settings

### Debug Mode

Enable debug logging by setting the log level to `debug` in the configuration.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is proprietary and unlicensed. All rights reserved.

## 👥 Team

- **Author**: tuanhiep1232
- **Organization**: TradeX

## 📞 Support

For support and questions, please contact the development team or create an issue in the repository.

---

**Note**: This service is designed for production use in enterprise environments. Ensure proper security measures and monitoring are in place before deployment.
