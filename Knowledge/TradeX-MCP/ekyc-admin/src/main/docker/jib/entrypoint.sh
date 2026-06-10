#!/bin/sh

echo "The application will start in ${JHIPSTER_SLEEP}s..." && sleep ${JHIPSTER_SLEEP}

# Find the JAR file in the target directory
JAR_FILE=$(find /app/target -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ No JAR file found in /app/target directory!"
    echo ""
    echo "This could be due to:"
    echo "1. Maven build failed during Docker build (dependency issues)"
    echo "2. JAR file not copied correctly"
    echo ""
    echo "Solutions:"
    echo "1. Build the application locally first: mvn clean package -DskipTests -Pprod"
    echo "2. Check if custom dependencies are available:"
    echo "   - com.techx.tradex:tradex-common:jar:1.0-SNAPSHOT"
    echo "   - com.difisoft:model-common:jar:1.0.0"
    echo "   - com.difisoft:redis-dao:jar:1.0.0"
    echo "3. Run: ./resolve-dependencies.sh for more help"
    echo ""
    echo "For now, you can:"
    echo "- Build locally and run: java -jar target/*.jar"
    echo "- Or resolve dependencies and rebuild Docker image"
    exit 1
fi

echo "✅ Found JAR file: $JAR_FILE"
echo "Starting Spring Boot application..."

exec java ${JAVA_OPTS} -jar "$JAR_FILE" "$@"
