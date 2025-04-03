# Dalhousie Marketplace

An online marketplace application designed for Dalhousie University community members to buy, sell, and trade items. This platform supports various e-commerce features including regular listings, bidding, reviews, messaging between users, and secure payments.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Dependencies](#dependencies)
- [Build and Deployment Instructions](#build-and-deployment-instructions)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [Database Setup](#database-setup)
  - [External Services Configuration](#external-services-configuration)
- [Test Driven Development](./Documentation/TDD#test-driven-development)
- [Usage Scenarios](./Documentation/#usage-scenarios)
  - [User Authentication](./Documentation/UsageScenario.md#user-authentication)
  - [Listing Management](./Documentation/UsageScenario#listing-management)
  - [Shopping Features](./Documentation/UsageScenario#shopping-features)
  - [Bidding System](./Documentation/UsageScenario#bidding-system)
  - [Messaging and Notifications](./Documentation/UsageScenario#messaging-and-notifications)
  - [Reviews and Ratings](./Documentation/UsageScenario#reviews-and-ratings)
  - [User Preferences](./Documentation/UsageScenario#user-preferences)

## Features

- **User Authentication**: Secure email-based registration and login with email verification
- **Listing Management**: Create, view, search, and manage product listings with images
- **Shopping Features**: Shopping cart, wishlist, and secure checkout process
- **Bidding System**: Place bids on items, counteroffer, and finalize auctions
- **Messaging**: Real-time messaging between buyers and sellers
- **Reviews & Ratings**: Post and view reviews after completing purchases
- **Notifications**: Real-time notifications for various system events
- **User Profiles**: View and manage user profiles with statistics
- **Payment Processing**: Secure payment through Stripe integration
- **Responsive Design**: Works on desktop and mobile devices

## Technology Stack

- **Backend**: Java with Spring Boot
- **Frontend**: React 
- **Database**: MySQL
- **Security**: Spring Security with JWT authentication
- **Real-time Communication**: WebSocket with STOMP
- **Payment Processing**: Stripe API
- **Email Service**: Spring Mail

## Dependencies

### Backend Dependencies

The project uses Maven for dependency management. The main dependencies include:

- **Spring Boot**: Web, Security, Data JPA, WebSocket, Mail
- **Database**: MySQL Connector
- **Security**: JWT (JSON Web Token)
- **Payment Processing**: Stripe Java SDK
- **Utilities**: Lombok, Jackson

### System Requirements

- Java JDK 17 or higher
- Maven 3.8 or higher
- MySQL 8.0 or higher
- Node.js 14.x or higher (for frontend)
- npm 6.x or higher (for frontend)

### Installing Dependencies

#### Java and Maven
```bash
# For Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk maven

# For macOS (using Homebrew)
brew install openjdk@17 maven

# Verify installation
java -version
mvn -version
```

#### MySQL
```bash
# For Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# For macOS (using Homebrew)
brew install mysql

# Start MySQL service
sudo systemctl start mysql   # Linux
brew services start mysql    # macOS

# Secure MySQL installation
sudo mysql_secure_installation
```

#### Node.js and npm (for frontend)
```bash
# For Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt install nodejs

# For windows using Docker image

# Pull the Node.js Docker image:
docker pull node:22-alpine
# Create a Node.js container and start a Shell session:
docker run -it --rm --entrypoint sh node:22-alpine
# Verify the Node.js version:
node -v # Should print "v22.14.0".
# Verify npm version:
npm -v # Should print "10.9.2".


# For macOS (using Homebrew)
brew install node

# Verify installation
node -v
npm -v
```

## Build and Deployment Instructions

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/dalhousie-marketplace-backend.git
   cd dalhousie-marketplace-backend
   ```

2. **Configure application properties**
   
   Create or modify `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/dalhousie_marketplace?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
   
   # JWT Configuration
   jwt.secret=your_jwt_secret_key
   jwt.expiration=3600000
   
   # Stripe API Keys
   stripe.api.key=your_stripe_secret_key
   stripe.webhook.secret=your_stripe_webhook_secret
   
   # Email Configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   
   # File Upload Configuration
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   
   # Application Base URL (for email links and Stripe redirects) 
   Kindly note that if you deployed the application on a local server like 172.*.** you should point to your specific url
   app.base-url=http://localhost:3000

   
   ```

3. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```

4. **Run the application**
   ```bash
   java -jar target/dalhousie-marketplace-backend-0.0.1-SNAPSHOT.jar
   ```

   Alternatively, you can run using Maven:
   ```bash
   mvn spring-boot:run
   ```

### Database Setup

1. **Create MySQL database**
   ```sql
   CREATE DATABASE dalhousie_marketplace;
   CREATE USER 'marketplace_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON dalhousie_marketplace.* TO 'marketplace_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Database schema initialization**
   
   The application will automatically create the database schema using JPA/Hibernate when started for the first time with `spring.jpa.hibernate.ddl-auto=update` in application.properties.

### External Services Configuration

#### Setting up Stripe

1. Create a Stripe account at https://stripe.com
2. Get your API keys from the Stripe Dashboard
3. Add your Stripe secret key to `application.properties`
4. Set up webhook endpoints (for payment notifications)
   ```
   Webhook endpoint: ********************
   Events to listen for: payment_intent.succeeded, payment_intent.payment_failed, checkout.session.completed
   ```

#### Setting up Email Service

1. If using Gmail:
   - Enable 2-Step Verification for your Google account
   - Generate an App Password: Google Account > Security > App Passwords
   - Use this app password in your `application.properties`

### Frontend Setup

The frontend code is  in the dalhousie-marketplace-frontend sub-folder 

1. **Clone the frontend repository**
   ```bash
   git clone https://git.cs.dal.ca/courses/2025-winter/csci-5308/group03/-/tree/Main/dalhousie-marketplace-frontend
   cd dalhousie-marketplace-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   
   Create a `.env` file:
   ```
   REACT_APP_API_URL=http://localhost:8080/api
   REACT_APP_WEBSOCKET_URL=ws://localhost:8080/ws
   ```

4. **Run the development server**
   ```bash
   npm start
   ```

# Test Driven Development

The project follows Test-Driven Development (TDD) principles. For details, see our [TDD Documentation](./Documentation/TDD.md).

- Backend unit tests are written using JUnit and Mockito
- Integration tests verify component interactions

   To run tests:
   ```bash
   # Backend tests
   cd dalhousie-marketplace-backend
   mvn test


## Usage Scenarios

We fulfilled our clients requirements. For details, see our [Usage Scenarios](./Documentation/UsageScenario.md).