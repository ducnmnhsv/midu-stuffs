# Docker Setup for JHipster eKyc Admin

This document describes how to build and run the JHipster eKyc Admin application using Docker.

## Prerequisites

- Docker installed and running
- Docker Compose (optional, for easier management)
- **Java 8** (JDK 1.8) - for local Maven build
- **Maven 3.3.9+** - for building the application

## Project Structure

This JHipster project uses:
- **Java 8** (JDK 1.8)
- **Maven 3.3.9** for backend build
- **Node 14.16.0** for frontend build (handled by frontend-maven-plugin)
- **Spring Boot** as the main application framework

## ⚠️ Important Note About Dependencies

This project has some custom dependencies that are not available in public Maven repositories:
- `com.techx.tradex:tradex-common:jar:1.0-SNAPSHOT`
- `com.difisoft:model-common:jar:1.0.0`
- `com.difisoft:redis-dao:jar:1.0.0`

## 🚀 Quick Start

### Option 1: Use the Build Script (Recommended)

```bash
# This will attempt to build locally first, then create Docker image
./build-docker.sh
```

### Option 2: Manual Build Process

1. **Build the application locally:**
   ```bash
   mvn clean package -DskipTests -Pprod
   ```

2. **Build the Docker image:**
   ```bash
   docker build -t ekyc-admin:latest .
   ```

3. **Run the container:**
   ```bash
   docker run -p 8080:8080 ekyc-admin:latest
   ```

## 🔧 Resolving Dependency Issues

If you encounter dependency resolution errors, run:

```bash
./resolve-dependencies.sh
```

This script will help you identify and resolve the missing dependencies.

### Common Solutions:

1. **Check Internal Repositories:**
   - Look for internal Maven repositories in your organization
   - Check with your development team for repository access
   - Verify VPN or network access to internal repositories

2. **Install Dependencies Locally:**
   If you have the JAR files, install them locally:
   ```bash
   mvn install:install-file -Dfile=<path-to-jar> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar
   ```

3. **Check Alternative Profiles:**
   ```bash
   mvn help:all-profiles
   ```

## 🐳 Docker Commands

### Building the Image

```bash
# Build with default tag
docker build -t ekyc-admin:latest .

# Build with custom tag
docker build -t ekyc-admin:v1.0.0 .

# Build without cache
docker build --no-cache -t ekyc-admin:latest .
```

### Running the Container

```bash
# Basic run
docker run -p 8080:8080 ekyc-admin:latest

# Run with custom environment variables
docker run -p 8080:8080 \
  -e JAVA_OPTS="-Xmx1g -XX:+UseG1GC" \
  -e JHIPSTER_SLEEP=5 \
  ekyc-admin:latest

# Run in detached mode
docker run -d -p 8080:8080 --name ekyc-admin ekyc-admin:latest

# Run with custom port mapping
docker run -p 8081:8080 ekyc-admin:latest
```

### Using Docker Compose

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f ekyc-admin

# Stop the application
docker-compose down

# Rebuild and start
docker-compose up --build -d
```

## 🌍 Environment Variables

You can customize the application behavior using environment variables:

- `JAVA_OPTS`: JVM options (default: -Xmx512m -Djava.security.egd=file:/dev/./urandom)
- `JHIPSTER_SLEEP`: Delay before starting the application (default: 0)
- `SPRING_PROFILES_ACTIVE`: Spring profile (default: prod)

### Example with custom settings:
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xmx2g -XX:+UseG1GC" \
  -e JHIPSTER_SLEPS=10 \
  ekyc-admin:latest
```

## 🔍 Health Check

The application includes a health check endpoint at `/management/health`. Docker will automatically monitor this endpoint.

## 📊 Monitoring and Logs

### Viewing Logs

```bash
# Using Docker directly
docker logs -f <container_id>

# Using Docker Compose
docker-compose logs -f ekyc-admin
```

### Container Status

```bash
# Check container health
docker ps

# Check container details
docker inspect <container_id>
```

## 🛠️ Troubleshooting

### Port Already in Use
If port 8080 is already in use, change the port mapping:
```bash
docker run -p 8081:8080 ekyc-admin:latest
```

### Memory Issues
Increase JVM memory:
```bash
docker run -p 8080:8080 -e JAVA_OPTS="-Xmx2g" ekyc-admin:latest
```

### Build Failures

1. **Dependency Issues:**
   - Run `./resolve-dependencies.sh` for guidance
   - Check internal Maven repositories
   - Contact your development team

2. **Maven Build Issues:**
   - Ensure Java 8 is installed and in PATH
   - Ensure Maven 3.3.9+ is installed and in PATH
   - Check network connectivity for Maven dependencies

3. **Docker Build Issues:**
   - Ensure Docker has enough memory (at least 4GB recommended)
   - Clear Docker cache: `docker system prune -a`
   - Check Docker daemon is running

### Application Won't Start

1. **Check if JAR file exists:**
   ```bash
   ls -la target/*.jar
   ```

2. **Check container logs:**
   ```bash
   docker logs <container_id>
   ```

3. **Verify the build was successful:**
   ```bash
   mvn clean package -DskipTests -Pprod
   ```

## 🔄 Development Workflow

### For Development:

1. **Build and run locally:**
   ```bash
   mvn clean package -DskipTests -Pprod
   java -jar target/*.jar
   ```

2. **Build Docker image when ready:**
   ```bash
   ./build-docker.sh
   ```

### For Production:

1. **Build the application:**
   ```bash
   mvn clean package -DskipTests -Pprod
   ```

2. **Build Docker image:**
   ```bash
   docker build -t ekyc-admin:prod .
   ```

3. **Deploy:**
   ```bash
   docker run -d -p 8080:8080 --name ekyc-admin-prod ekyc-admin:prod
   ```

## 📚 Additional Resources

- **JHipster Documentation:** https://www.jhipster.tech/
- **Spring Boot Docker:** https://spring.io/guides/gs/spring-boot-docker/
- **Maven Documentation:** https://maven.apache.org/guides/

## 🆘 Getting Help

If you're still having issues:

1. **Check the logs:** `docker logs <container_id>`
2. **Run dependency resolution:** `./resolve-dependencies.sh`
3. **Review this README** for common solutions
4. **Contact your development team** for internal dependency access
5. **Check internal documentation** for project-specific setup instructions

## 📝 Notes

- The Docker image expects a pre-built JAR file in the `target/` directory
- The frontend is built as part of the Maven build process using the `frontend-maven-plugin`
- The application runs on port 8080 by default
- Health checks are performed against `/management/health` endpoint
- The container runs as a non-root user for security
