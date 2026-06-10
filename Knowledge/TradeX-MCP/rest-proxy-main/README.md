# TradeX REST Proxy

A high-performance REST proxy server for the TradeX trading system, built with Node.js, TypeScript, and Express. This service acts as a gateway between client applications and various backend services, providing authentication, request routing, and message forwarding capabilities.

## 🚀 Features

- **REST API Gateway**: Centralized entry point for all TradeX system APIs
- **Multi-Instance Support**: Supports both Admin API and Client API instances
- **Authentication & Authorization**: JWT-based authentication with RSA key verification
- **Request Routing**: Intelligent routing to backend services via HTTP proxy or Kafka
- **Scope Management**: Dynamic API scope configuration and management
- **Multi-Language Support**: Internati onalization with language code detection
- **Device Detection**: Express device middleware for client identification
- **CORS Support**: Configurable Cross-Origin Resource Sharing
- **Logging & Monitoring**: Comprehensive logging with file rotation
- **Docker Support**: Containerized deployment ready

## 🏗️ Architecture

The REST Proxy serves as a middleware layer that:

1. **Receives** HTTP requests from client applications
2. **Authenticates** requests using JWT tokens
3. **Routes** requests to appropriate backend services
4. **Forwards** messages via HTTP proxy or Kafka messaging
5. **Manages** API scopes and access control
6. **Returns** responses to clients

### Core Components

- **Middleware Stack**: Body parsing, format verification, device detection, message handling
- **Route Handlers**: API endpoints for equity, derivatives, market data, and admin operations
- **Proxy Management**: HTTP proxy and Kafka forwarding capabilities
- **Scope Service**: Dynamic API scope configuration management
- **Authentication**: JWT verification and API key validation

## 📋 Prerequisites

- **Node.js**: Version 16.13.0 (specified in .nvmrc)
- **Yarn**: Package manager (preferred over npm)
- **Kafka**: Message broker for asynchronous communication
- **Redis**: For session management (if configured)
- **JWT Keys**: RSA public/private key pairs for authentication

## 🛠️ Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd rest-proxy
```

### 2. Install Dependencies

```bash
# Install node version with nvm
nvm install
# select correct node version
nvm use
# use npm
npm install
```

### 3. Environment Setup

Copy and configure the environment file:

```bash
cp src/env.js.example src/env.js
# Edit src/env.js with your configuration
```

### 4. Build the Project

```bash
# Build TypeScript to JavaScript
npm run build
```

## 🚀 Running Locally

### Development Mode

```bash
# In another terminal, run the built application
npm run run-local
```

### Available Scripts

- `yarn build` - Compile TypeScript and run linting
- `yarn build-local` - Build and run locally with environment file
- `yarn run-local` - Run locally with copied resources
- `yarn watch` - Watch mode for development
- `yarn start` - Production build and start
- `yarn compile` - Compile TypeScript only
- `yarn lint` - Run ESLint with auto-fix

## 🐳 Docker Deployment

### Build Docker Image

```bash
# Build the image
docker build -t tradex-rest-proxy .

# Or with custom tag
docker build -t tradex-rest-proxy:latest .
```

### Run Docker Container

```bash
# Run with default configuration
docker run -p 3000:3000 tradex-rest-proxy

# Run with environment variables
docker run -p 3000:3000 \
  -e TRADEX_ENV_KAFKA_URLS=kafka1:9092,kafka2:9092 \
  -e TRADEX_ENV_DOMAIN=yourdomain.com \
  tradex-rest-proxy
```

### Docker Compose

Create a `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  rest-proxy:
    build: .
    ports:
      - "3000:3000"
    environment:
      - TRADEX_ENV_KAFKA_URLS=kafka:9092
      - TRADEX_ENV_DOMAIN=yourdomain.com
      - TRADEX_ENV_NODE_ID=rest-proxy-1
    volumes:
      - ./logs:/logs
      - ./data:/data
      - ./keys:/keys
    depends_on:
      - kafka
      - redis

  kafka:
    image: confluentinc/cp-kafka:latest
    # ... kafka configuration

  redis:
    image: redis:alpine
    # ... redis configuration
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `TRADEX_ENV_NODE_ID` | Unique node identifier | Auto-generated UUID |
| `TRADEX_ENV_DOMAIN` | Primary domain | `tradex` |
| `TRADEX_ENV_KAFKA_URLS` | Kafka broker URLs | `localhost:9092` |
| `TRADEX_ENV_INSTANCE` | Instance type (admin/api) | `api` |
| `TRADEX_ENV_ENABLE_ENCRYPT_PASSWORD` | Enable password encryption | `false` |

### Configuration Files

- **`src/config.ts`**: Main configuration with defaults
- **`src/env.js`**: Environment-specific overrides
- **`src/data/scopeData.json`**: API scope definitions
- **`src/data/openApi.json`**: OpenAPI specifications

### Key Configuration Areas

- **Port**: Default 3000, configurable via environment
- **CORS**: Configurable cross-origin settings
- **JWT**: RSA key file paths and verification settings
- **Kafka**: Broker URLs, topics, and client configuration
- **Logging**: File paths, rotation, and log levels

## 🔐 Authentication

### JWT Authentication

The system uses RSA-signed JWT tokens for authentication:

```typescript
// JWT configuration
jwt: {
  publicKeyFile: `keys/aaa/${domain}/jwt-public.key`,
  domains: {
    [domain]: {
      publicKeyFile: `keys/aaa/${domain}/jwt-public.key`,
    },
  },
}
```

### API Key Authentication

For admin operations, API key validation is required:

```typescript
// API key check middleware
app.use("/reInitScope", checkApiKey);
```

## 📡 API Endpoints

### Public Endpoints

- `GET /currentTime` - Get current server time
- `GET /api/v1/clearCookie/:key` - Clear specific cookies

### Admin Endpoints

- `GET /reInitScope` - Reinitialize API scopes
- `POST /api/v1/admin/*` - Admin operations

### Client API Endpoints

- `POST /api/v1/*` - Client API operations
- Various equity, derivatives, and market data endpoints

## 🔄 Message Forwarding

### HTTP Proxy Forwarding

```typescript
// Forward HTTP requests to backend services
config.forwards.forEach((forward: IForwardConfig) => {
  if (forward.type === 'http') {
    // HTTP proxy configuration
  }
});
```

### Kafka Forwarding

```typescript
// Forward messages via Kafka
if (forward.type === 'kafka') {
  // Kafka message forwarding
}
```

## 📊 Monitoring & Logging

### Log Configuration

```typescript
logger: {
  config: {
    appenders: {
      application: { type: 'console' },
      file: {
        type: 'file',
        filename: '/logs/application.log',
        compression: true,
        maxLogSize: 104857600, // 100MB
        backups: 10,
      },
    },
    categories: {
      default: { appenders: ['application', 'file'], level: 'info' },
    },
  },
}
```

### Health Checks

- **Endpoint**: `/health` (if implemented)
- **Metrics**: Request/response logging
- **Error Tracking**: Comprehensive error logging

## 🧪 Development

### Code Structure

```
src/
├── app/
│   ├── middlewares/     # Express middleware
│   ├── models/          # Data models and interfaces
│   └── routes/          # API route definitions
├── config.ts            # Configuration
├── server.ts            # Server initialization
└── index.ts             # Application entry point
```

### Adding New Routes

1. Create route file in `src/app/routes/`
2. Define endpoints and handlers
3. Import and register in main router
4. Add to scope configuration if needed

### Adding New Middleware

1. Create middleware file in `src/app/middlewares/`
2. Export middleware function
3. Register in `server.ts` middleware stack

## 🚨 Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using port 3000
   lsof -i :3000
   # Kill process or change port
   ```

2. **Kafka Connection Issues**
   ```bash
   # Verify Kafka is running
   nc -zv kafka-host 9092
   # Check environment variables
   echo $TRADEX_ENV_KAFKA_URLS
   ```

3. **JWT Key Issues**
   ```bash
   # Verify key files exist
   ls -la keys/aaa/yourdomain/
   # Check file permissions
   chmod 600 keys/aaa/yourdomain/*.key
   ```

### Debug Mode

```bash
# Enable debug logging
DEBUG=* yarn run-local

# Or set log level
TRADEX_ENV_LOG_LEVEL=debug yarn run-local
```

## 📝 License

This project is licensed under the ISC License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📞 Support

For support and questions:
- **Email**: admin@difisoft.com
- **Issues**: Create an issue in the repository
- **Documentation**: Check the inline code comments

---

**Note**: This is a production system component. Please ensure proper testing and validation before deploying to production environments.
