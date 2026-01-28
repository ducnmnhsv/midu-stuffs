# TradeX Common Java Library

A comprehensive Java common library for the TradeX trading platform, providing shared utilities, models, and infrastructure components for building robust trading applications.

## 📋 Overview

TradeX Common is a Maven-based Java library that serves as the foundation for the TradeX trading platform. It provides a collection of reusable components including constants, exceptions, models, utilities, and Kafka integration utilities that can be shared across multiple trading system modules.

## 🏗️ Project Structure

```
src/main/java/com/techx/tradex/common/
├── constants/          # System constants and enums
├── exceptions/         # Custom exception classes
├── handler/           # Request handling utilities
├── kafka/            # Kafka integration components
├── model/            # Data models and DTOs
├── utils/            # Utility classes and helpers
└── validator/        # Validation utilities
```

## 🚀 Key Features

### Core Components

- **Constants & Enums**: Centralized system constants, error codes, and trading-related enumerations
- **Exception Handling**: Comprehensive exception hierarchy with error codes and message parameters
- **Data Models**: Request/response models, market data structures, and trading entities
- **Utility Classes**: Bean utilities, time utilities, and common helper functions
- **Validation**: Input validation and data integrity checking utilities

### Kafka Integration

- **Producer/Consumer**: High-level Kafka client abstractions
- **Message Handling**: Request-response patterns and message routing
- **Partitioning**: Smart message partitioning strategies
- **Error Handling**: Robust error handling and retry mechanisms

### Trading-Specific Features

- **Market Data Models**: ETF, futures, and stock market data structures
- **Order Management**: Order types, statuses, and trading operations
- **Account Management**: Account types and validation
- **Notification System**: Alarm and notification infrastructure

## 🛠️ Technology Stack

- **Java**: 1.8+
- **Maven**: Build and dependency management
- **Lombok**: Code generation and boilerplate reduction
- **RxJava**: Reactive programming support
- **Jackson**: JSON serialization/deserialization
- **Apache Kafka**: Message streaming and event processing
- **SLF4J**: Logging facade
- **Apache Commons**: Utility libraries

## 📦 Dependencies

### Core Dependencies
- `lombok` (1.18.2) - Code generation
- `rxjava` (1.3.8) - Reactive programming
- `jackson-databind` (2.9.4) - JSON processing
- `slf4j-api` (1.7.25) - Logging

### Optional Dependencies
- `kafka_2.12` (2.3.1) - Kafka client (optional)
- `kafka-clients` (2.3.1) - Kafka client library (optional)

### Utility Dependencies
- `commons-math3` (3.2) - Mathematical utilities
- `commons-validator` (1.6) - Validation utilities
- `onesignal` (1.0.10) - Push notification service
- `checker-qual` (3.27.0) - Type annotations

## 🔧 Installation

### Prerequisites
- Java 1.8 or higher
- Maven 3.6 or higher

### Building the Project
```bash
# Clone the repository
git clone <repository-url>
cd tradex-common-java

# Build the project
mvn clean install

# Skip tests (if needed)
mvn clean install -DskipTests
```

### Using as a Dependency
Add the following to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.techx.tradex</groupId>
    <artifactId>tradex-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 📚 Usage Examples

### Exception Handling
```java
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.model.responses.Response;

try {
    // Your business logic
} catch (Exception e) {
    GeneralException ge = new GeneralException("INVALID_PARAMETER", "param1", "param2");
    Response<?> response = Response.fromException(ge);
    // Handle response
}
```

### Kafka Consumer
```java
import com.techx.tradex.common.kafka.KafkaConsumer;
import com.techx.tradex.common.kafka.ConsumerHandler;

ConsumerHandler<String, String> handler = new ConsumerHandler<String, String>() {
    @Override
    public void handle(ConsumerRecord<String, String> record) {
        // Process the message
        System.out.println("Received: " + record.value());
    }
};

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
    "localhost:9092",
    "my-group",
    Arrays.asList("my-topic"),
    new Properties(),
    handler
);

// Start consuming in a separate thread
new Thread(consumer).start();
```

### Response Models
```java
import com.techx.tradex.common.model.responses.Response;
import com.techx.tradex.common.model.responses.Status;

// Create success response
Response<String> successResponse = new Response<>("Data content");

// Create error response
Status errorStatus = new Status("INVALID_PARAMETER", "param1");
Response<?> errorResponse = new Response<>(errorStatus);
```

## 🏛️ Architecture

The library follows a modular architecture with clear separation of concerns:

- **Constants Layer**: Centralized configuration and system constants
- **Model Layer**: Data transfer objects and domain models
- **Exception Layer**: Hierarchical exception handling with error codes
- **Utility Layer**: Reusable helper functions and tools
- **Integration Layer**: External system integrations (Kafka, etc.)

## 🔍 Key Classes

### Constants
- `Common`: System-wide constants and defaults
- `ErrorCodeEnums`: Standardized error codes
- `AccountType`: Account type enumerations
- `OrderTypeEnum`: Trading order type definitions

### Models
- `Response<T>`: Generic response wrapper
- `Status`: Response status information
- `Error`: Error details structure
- `AccessToken`: Authentication token model

### Exceptions
- `GeneralException`: Base exception class
- `FieldRequiredException`: Missing field validation
- `InvalidParameterException`: Parameter validation errors
- `NotFoundException`: Resource not found errors

### Kafka Components
- `KafkaConsumer`: High-level consumer abstraction
- `KafkaProducer`: Message producer utilities
- `ConsumerHandler`: Message processing interface
- `KafkaRequestHandler`: Request-response handling

## 🧪 Testing

The project includes comprehensive test coverage for all major components:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Generate test coverage report
mvn jacoco:report
```

## 📖 API Documentation

For detailed API documentation, refer to the JavaDoc comments in the source code or generate documentation using:

```bash
mvn javadoc:javadoc
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc comments
- Ensure proper exception handling
- Write unit tests for new functionality

## 📄 License

This project is proprietary software owned by TechX. All rights reserved.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Refer to the project documentation

## 🔄 Version History

- **1.0-SNAPSHOT**: Initial development version with core functionality

## 📝 Changelog

### 1.0-SNAPSHOT
- Initial release
- Core exception handling framework
- Kafka integration utilities
- Common data models and utilities
- Trading-specific constants and enums

---

**Note**: This library is designed for internal use within the TradeX platform ecosystem. Ensure compatibility with your specific trading system requirements before integration.
