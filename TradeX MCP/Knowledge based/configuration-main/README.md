# TradeX Configuration Service

A microservice for managing configuration, localization, and system settings in the TradeX trading platform ecosystem.

## Overview

The TradeX Configuration Service is a Node.js-based microservice that provides centralized configuration management, localization support, and administrative functions for the TradeX system. It handles client management, scope permissions, language resources, FAQ management, and various system configurations.

## Features

### Core Functionality
- **Client Management**: OAuth client registration, secret management, and authentication
- **Scope & Permission Management**: Role-based access control with scope groups and permissions
- **Localization System**: Multi-language support with namespace-based resource management
- **FAQ Management**: Service-specific FAQ handling with review system
- **System Configuration**: Holiday calendars, interest information, and system parameters
- **Menu Management**: Dynamic menu system with role-based access
- **OpenAPI Integration**: Swagger/OpenAPI specification management

### Technical Features
- **Event-Driven Architecture**: Kafka-based message handling for asynchronous operations
- **Database Integration**: MySQL database with TypeORM for data persistence
- **Cloud Storage**: AWS S3 and MinIO integration for file storage
- **Dependency Injection**: TypeDI for service management
- **TypeScript**: Full TypeScript implementation with strict type checking

## Architecture

### Service Structure
```
src/
├── consumers/          # Kafka message handlers
├── models/            # Database entities and request/response models
├── repositories/      # Data access layer
├── services/          # Business logic services
└── utils/            # Utility functions and helpers
```

### Key Components
- **RequestHandler**: Main Kafka message router and API endpoint handler
- **AppDataSource**: TypeORM database connection and entity management
- **Service Layer**: Business logic implementation for each domain
- **Repository Layer**: Data access abstraction for database operations

## Technology Stack

- **Runtime**: Node.js 16 (Alpine)
- **Language**: TypeScript 5.3
- **Framework**: Custom microservice framework
- **Database**: MySQL with TypeORM
- **Message Queue**: Apache Kafka
- **Cloud Storage**: AWS S3 / MinIO
- **Containerization**: Docker with multi-stage builds
- **Dependency Injection**: TypeDI
- **Validation**: AJV

## Prerequisites

- Node.js 16.x
- MySQL 8.0+
- Apache Kafka
- Docker (for containerized deployment)

## Installation

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd configuration
   ```

2. **Install dependencies**
   ```bash
   yarn install
   ```

3. **Environment Configuration**
   Create an `env.js` file in the root directory with your configuration:
   ```javascript
   conf.db.connection.host = "your-mysql-host";
   conf.db.connection.user = "your-mysql-user";
   conf.db.connection.password = "your-mysql-password";
   conf.db.connection.database = "your-database-name";
   
   conf.kafkaUrls = ["your-kafka-broker:9092"];
   conf.aws.accessKeyId = "your-aws-access-key";
   conf.aws.secretAccessKey = "your-aws-secret-key";
   ```

4. **Build and run**
   ```bash
   yarn build
   yarn start
   ```

### Docker Deployment

1. **Build the image**
   ```bash
   docker build -t tradex-configuration .
   ```

2. **Run the container**
   ```bash
   docker run -p 3000:3000 tradex-configuration
   ```

## Configuration

### Environment Variables

The service uses a combination of hardcoded defaults and external configuration files:

- **Database**: MySQL connection settings
- **Kafka**: Broker URLs and topic configurations
- **AWS**: S3 credentials and bucket configurations
- **Storage**: File upload limits and content type restrictions

### Key Configuration Sections

- **Database**: MySQL connection parameters
- **Kafka**: Message broker settings and topic configurations
- **AWS S3**: Storage bucket configurations and access policies
- **MinIO**: Alternative storage configuration
- **API**: REST endpoint configurations and Swagger settings

## API Endpoints

### System Management
- `GET /api/v1/system/client` - Query clients for updates
- `GET /api/v1/system/loginMethod` - Query login methods
- `GET /api/v1/system/scope` - Query scopes
- `GET /api/v1/system/scopeGroup` - Query scope groups

### Admin Operations
- `GET /api/v1/admin/common/dataview` - Get data by view
- `GET /api/v1/admin/locale/resource` - Get all language resources
- `GET /api/v1/admin/menus` - Get menus by role IDs
- `POST /api/v1/admin/scope/add` - Add new scope
- `PUT /api/v1/admin/scope/{id}/update` - Update scope
- `DELETE /api/v1/admin/scope/{id}/delete` - Delete scope

### Public APIs
- `GET /api/v1/locale` - Get localized resources
- `GET /api/v1/faq/{serviceName}` - Get FAQs for specific service
- `GET /api/v1/holidays` - Get holiday information
- `GET /api/v1/interestInfo` - Get interest rate information

### Client Management
- `GET /api/v1/client` - Get all clients
- `POST /api/v1/client/add` - Add new client
- `PUT /api/v1/client/{id}/changeSecret` - Change client secret
- `PUT /api/v1/client/{id}/update` - Update client
- `DELETE /api/v1/client/{id}/delete` - Delete client

## Database Schema

The service manages several key entities:

- **Client**: OAuth client applications
- **Scope & ScopeGroup**: Permission definitions
- **LangResource**: Localization resources
- **Menu & MenuGroup**: Navigation structure
- **FAQ & FAQGroup**: Help content
- **Holiday**: Trading calendar information
- **Service**: System service definitions

## Development

### Project Structure
```
├── src/
│   ├── consumers/          # Kafka message handlers
│   ├── models/             # Data models and entities
│   │   ├── db/            # Database entities
│   │   ├── request/       # Request DTOs
│   │   └── response/      # Response DTOs
│   ├── repositories/      # Data access layer
│   ├── services/          # Business logic
│   │   └── admin/         # Administrative services
│   └── utils/             # Utility functions
├── dbmigration/           # Database migration scripts
├── dockerfile             # Docker configuration
└── entrypoint.sh          # Container startup script
```

### Available Scripts

- `yarn build` - Compile TypeScript to JavaScript
- `yarn start` - Build and run the application
- `yarn run-local` - Run with local environment
- `yarn check` - Run ESLint checks
- `yarn fix` - Auto-fix ESLint issues

### Code Quality

The project uses:
- **ESLint** for code linting
- **Prettier** for code formatting
- **Husky** for pre-commit hooks
- **TypeScript** for type safety

## Deployment

### Docker Deployment

The service is containerized using a multi-stage Docker build:

1. **Base Stage**: Node.js runtime with dependencies
2. **Builder Stage**: TypeScript compilation
3. **Production Stage**: Optimized runtime image

### Health Checks

The container includes health checks that verify the service is responding on port 3000.

### Environment Configuration

The service supports environment-specific configuration through:
- External `env.js` files
- Service-specific environment scripts
- Docker environment variables

## Monitoring & Logging

- **Logging**: Structured logging with file rotation
- **Health Checks**: HTTP-based health monitoring
- **Metrics**: Service registration and Kafka monitoring

## Security

- **Authentication**: JWT-based authentication
- **Authorization**: Role-based access control
- **Secrets**: Secure client secret management
- **API Security**: OpenAPI security schemes

## Contributing

1. Follow the existing code style and TypeScript conventions
2. Ensure all tests pass before submitting changes
3. Update documentation for any new features
4. Follow the commit message conventions

## License

ISC License - see package.json for details

## Support

For technical support or questions, contact: admin@difisoft.vn

## Related Services

This service integrates with:
- **TradeX Common**: Shared utilities and frameworks
- **TradeX Models**: Data model definitions
- **TradeX Models Configuration**: Configuration-specific models
- **TradeX Models Configuration Validator**: Validation schemas

---

*Last updated: December 2024*
