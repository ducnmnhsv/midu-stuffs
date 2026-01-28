# Market Collector Lotte

A comprehensive market data collection service designed to gather real-time and historical financial market data from Vietnamese stock exchanges (HOSE, HNX, UPCOM) through Lotte Securities' trading system and APIs.

## 🎯 Overview

Market Collector Lotte is a Spring Boot-based microservice that serves as a data collection hub for financial market information. It integrates with multiple data sources including:

- **Lotte Securities Trading System (HTS)** - Real-time market data feeds
- **Lotte Securities APIs** - Historical data, symbol information, and market quotes
- **Vietnamese Stock Exchanges** - HOSE, HNX, UPCOM markets
- **Multiple Asset Types** - Stocks, ETFs, Funds, Indices, Covered Warrants

## 🏗️ Architecture

### Core Components

- **Real-time Data Collection**: Connects to HTS system for live market feeds
- **API Integration**: RESTful API client for Lotte Securities endpoints
- **Data Processing**: Handles market data transformation and normalization
- **Storage**: MongoDB for persistent data, Redis for caching
- **Message Queue**: Kafka integration for data distribution
- **File Storage**: MinIO/S3 for market data files and resources

### Technology Stack

- **Framework**: Spring Boot 3.1.1
- **Language**: Java 17
- **Database**: MongoDB, Redis
- **Message Broker**: Apache Kafka
- **File Storage**: MinIO/AWS S3
- **Build Tool**: Maven
- **Native Support**: GraalVM native image compilation

## 🚀 Features

### Market Data Collection
- **Real-time Quotes**: Live stock prices, bid/ask spreads, trading volumes
- **Historical Data**: Daily OHLCV data, index performance
- **Symbol Information**: Stock listings, market classifications, corporate actions
- **Market Status**: Trading hours, market open/close status

### Supported Markets
- **HOSE (Ho Chi Minh Stock Exchange)**: Main board stocks and ETFs
- **HNX (Hanoi Stock Exchange)**: Small-cap and growth stocks
- **UPCOM**: Unlisted Public Company Market
- **Funds**: Investment fund listings
- **Covered Warrants**: Derivative instruments

### Data Types
- Stock quotes and daily data
- Index quotes and performance
- ETF quotes and NAV
- Covered warrant quotes
- Market depth (bid/ask)
- Corporate actions and dividends

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB 4.4+
- Redis 6.0+
- Apache Kafka 2.3+
- MinIO or AWS S3 access

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd market-collector-lotte
```

### 2. Configure Environment
Set the following environment variables:
```bash
export TECHX_ENV=production
export TRADEX_ENV_DOMAIN=tradex
export TRADEX_ENV_NODE_ID=node1
export TRADEX_ENV_INSTANCE_ID=0
export HTS_USER=your_hts_username
export HTS_PASSWORD=your_hts_password
```

### 3. Update Configuration
Modify `src/main/resources/application.yaml`:
- MongoDB connection string
- Redis host and port
- Kafka broker URLs
- Lotte API credentials
- MinIO/S3 configuration

### 4. Build the Project
```bash
# Standard JAR build
mvn clean package

# Native image build (requires GraalVM)
mvn clean package -Pnative
```

### 5. Run the Service
```bash
# JAR execution
java -jar target/market-collector-lotte-1.0.jar

# Native execution
./target/entrypoint
```

## ⚙️ Configuration

### Application Properties

Key configuration sections in `application.yaml`:

```yaml
app:
  apiConnection:
    baseUrl: https://tnhsvpro.nhsv.vn/lotte/
    apiKey: your_api_key
  
  marketConf:
    symbolStaticBucket: market
    fileConfig:
      defaultType: MINIO
      minio:
        baseUrl: https://file.nhsv-dev.tradex.vn
        accessKey: your_access_key
        privateKey: your_private_key
  
  realtime:
    workingTime:
      from: "09:00"
      to: "15:00"
      weekDays: [2, 3, 4, 5, 6]  # Monday to Friday
```

### Market Configuration
- **Working Hours**: Configurable trading session times
- **Week Days**: Market operation days
- **Connection Settings**: HTS connection parameters
- **API Endpoints**: Lotte Securities API URLs

## 🔌 API Integration

### Lotte Securities APIs

The service integrates with various Lotte Securities endpoints:

- **Market Data**: Stock quotes, index quotes, ETF quotes
- **Symbol Information**: Security names, types, classifications
- **Historical Data**: Daily OHLCV, index performance
- **Market Depth**: Best bid/ask prices and volumes

### HTS Connection

Real-time data collection through HTS trading system:
- **Market Feeds**: Live price updates, trades, market depth
- **Connection Management**: Automatic reconnection, health monitoring
- **Data Processing**: Real-time transformation and distribution

## 📊 Data Flow

1. **Initialization**: Download symbol lists and market information
2. **Real-time Collection**: Establish HTS connections for live data
3. **Data Processing**: Transform and normalize incoming market data
4. **Storage**: Persist data to MongoDB, cache in Redis
5. **Distribution**: Publish processed data to Kafka topics
6. **Monitoring**: Health checks and performance monitoring

## 🧪 Development

### Project Structure
```
src/main/java/com/difisoft/marketcollector/
├── Application.java                 # Main application class
├── configurations/                  # Configuration classes
├── constants/                       # Market constants and enums
├── consumers/                       # Kafka message consumers
├── job/                            # Scheduled job services
├── model/                          # Data models and entities
├── repositories/                    # Data access layer
├── services/                       # Business logic services
├── socket/                         # Socket connection handlers
├── utils/                          # Utility classes
└── ws/                            # WebSocket services
```

### Key Services
- **RealTimeService**: Manages real-time data collection
- **LotteApiService**: Handles API communication
- **DownloadSymbolListService**: Downloads market symbol information
- **CacheService**: Manages data caching
- **MonitorService**: System health monitoring

## 🚨 Monitoring & Health Checks

The service includes comprehensive monitoring:
- **Connection Health**: HTS connection status monitoring
- **Data Quality**: Market data validation and quality checks
- **Performance Metrics**: Response times, throughput monitoring
- **Error Handling**: Automatic retry and recovery mechanisms

## 🔒 Security

- **API Authentication**: Secure API key management
- **HTS Authentication**: Secure trading system access
- **Data Encryption**: Secure data transmission and storage
- **Access Control**: Role-based access management

## 📈 Performance

- **High Throughput**: Optimized for high-frequency market data
- **Low Latency**: Real-time data processing pipeline
- **Scalability**: Horizontal scaling support
- **Native Compilation**: GraalVM native image for optimal performance

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is proprietary software owned by Difisoft.

## 🆘 Support

For technical support and questions:
- Create an issue in the repository
- Contact the development team
- Check the application logs for debugging information

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Redis Documentation](https://redis.io/documentation)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [GraalVM Native Image](https://www.graalvm.org/reference-manual/native-image/)