#!/bin/bash

# TradeX REST Proxy Setup Script
# This script helps you set up the development environment

set -e

echo "🚀 TradeX REST Proxy Setup"
echo "=========================="

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16.13.0 or later."
    echo "   You can use nvm to install the correct version:"
    echo "   nvm install 16.13.0"
    echo "   nvm use 16.13.0"
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2)
REQUIRED_VERSION="16.13.0"

if [ "$(printf '%s\n' "$REQUIRED_VERSION" "$NODE_VERSION" | sort -V | head -n1)" != "$REQUIRED_VERSION" ]; then
    echo "❌ Node.js version $NODE_VERSION is too old. Required: $REQUIRED_VERSION or later."
    echo "   Please upgrade Node.js or use nvm to switch versions."
    exit 1
fi

echo "✅ Node.js version $NODE_VERSION detected"

# Check if yarn is installed
if ! command -v yarn &> /dev/null; then
    echo "❌ Yarn is not installed. Installing yarn..."
    npm install -g yarn
fi

echo "✅ Yarn detected"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker is not installed. Docker is required for running dependencies."
    echo "   Please install Docker Desktop or Docker Engine."
    echo "   You can still develop locally without Docker."
else
    echo "✅ Docker detected"
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "⚠️  docker-compose is not available. Some features may not work."
else
    echo "✅ Docker Compose detected"
fi

echo ""
echo "📦 Installing dependencies..."
yarn install

echo ""
echo "🏗️  Building project..."
yarn build

echo ""
echo "📁 Creating necessary directories..."
mkdir -p logs data keys

echo ""
echo "⚙️  Checking configuration..."

# Check if env.js exists
if [ ! -f "env.js" ]; then
    echo "⚠️  env.js not found. Creating a template..."
    cat > env.js << 'EOF'
function config(conf) {
  // Configure your environment here
  conf.kafkaUrls = ["localhost:9092"];
  conf.zkUrl = ["localhost:2181"];
  conf.topic.configuration = 'configuration-1';
  
  // JWT configuration
  conf.jwt.publicKeyFile = path.join(__dirname, "./keys/jwt-public.key");
  conf.jwt.domains = {
    tradex: {
      publicKeyFile: path.join(__dirname, "./keys/jwt-public.key")
    }
  };
  
  console.log("Environment configured:", conf);
  return conf;
}

module.exports = config;
EOF
    echo "   Created env.js template. Please edit it with your configuration."
else
    echo "✅ env.js found"
fi

# Check if keys directory has any key files
if [ ! -f "keys/jwt-public.key" ]; then
    echo "⚠️  JWT public key not found in keys/jwt-public.key"
    echo "   Please add your JWT keys to the keys/ directory."
fi

echo ""
echo "🎯 Setup complete! Here are your next steps:"
echo ""
echo "1. Configure your environment:"
echo "   - Edit env.js with your Kafka, JWT, and other settings"
echo "   - Add your JWT keys to the keys/ directory"
echo ""
echo "2. Start development:"
echo "   - Run 'yarn watch' in one terminal"
echo "   - Run 'yarn run-local' in another terminal"
echo ""
echo "3. Or use Docker:"
echo "   - Run 'make docker-dev' to start with Docker Compose"
echo "   - Run 'make help' to see all available commands"
echo ""
echo "4. Test your setup:"
echo "   - Visit http://localhost:3000/currentTime"
echo "   - Check logs in the logs/ directory"
echo ""
echo "📚 For more information, see README.md"
echo "🐛 For issues, check the troubleshooting section in README.md"

# Make the script executable
chmod +x setup.sh

echo ""
echo "✨ Happy coding!"

