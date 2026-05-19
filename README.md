# 🎌 VTuber FanHub

**A comprehensive community platform for VTubers and their fans**

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.x-red)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Core Modules](#core-modules)
- [Configuration](#configuration)
- [Business Rules](#business-rules)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)

## 🎯 Overview

VTuber FanHub is a sophisticated community platform designed specifically for VTubers (Virtual YouTubers) and their fans. It provides a complete ecosystem for community management, content sharing[...]

The platform enables VTubers to:
- Create and manage exclusive community spaces (FanHubs)
- Moderate user-generated content with AI assistance
- Engage with fans through posts, comments, and rewards
- Run limited-time gacha/banner events
- Manage virtual items and a points-based economy

## ✨ Features

### 🔐 Authentication & User Management
- **Email OTP Verification**: Secure user registration with one-time password validation
- **Role-Based Access Control**: USER, VTUBER, ADMIN, MODERATOR roles
- **User Profiles**: Customizable profiles with avatars, frames, badges, and "Oshi" (favorite VTuber)
- **OAuth2 Integration**: OAuth2 Resource Server support for third-party authentication

### 👥 Community Management (FanHub)
- **Community Spaces**: VTubers create exclusive fan communities with unique subdomains
- **Membership System**: Owner, Moderator, and Member roles
- **Member Management**: Ban/unban capabilities with history tracking
- **Post Moderation**: Approve/reject user-generated content
- **Content Reporting**: Report problematic posts and members

### 📝 Content System
- **Multi-Format Posts**: Support for TEXT, IMAGE, VIDEO, and POLL post types
- **Post Lifecycle**: PENDING → APPROVED or REJECTED workflow
- **Rich Interactions**: Like, bookmark, comment, share, and translate posts
- **AI-Powered Features**:
  - Automated content validation
  - Post summarization
  - Post translation
  - Smart content filtering
- **Engagement Features**: Hashtags (max 5), voting polls, gift comments

### 🎰 Gacha & Rewards System
- **Limited-Time Banners**: Time-limited gacha events with exclusive items
- **One Active Banner Rule**: Only one banner can be active at any given time
- **Weighted Gacha**: Probability-based item selection using multipliers
- **Good Luck Mechanic**: Bonus luck items with no persistent storage
- **Points-Based**: Users spend points for gacha pulls

### 🛍️ Shop & Item Management
- **Virtual Items**: Badges, frames, avatars, and exclusive merchandise
- **Shop Items**: Purchasable items with categories and pricing
- **Item Reusability**: Items can be used across shop and gacha systems
- **User Inventory**: Track purchased and obtained items

### 💰 Points & Economy
- **Unified Points System**: Single points pool for both gacha and shop purchases
- **Point Balance Management**: Track and display user point balances
- **Transaction Safety**: Validation for insufficient funds
- **Payment Integration**: PayOS integration for real payment processing

### 🔔 Real-Time Notifications
- **Server-Sent Events (SSE)**: Real-time notification delivery
- **Event Types**: Post approvals, rejections, comments, gacha results, reports
- **System Events**: Notifications for important platform events

### 🖼️ Media Management
- **Cloudinary Integration**: Cloud-based image upload and storage
- **Multi-Image Support**: Up to 4 images per post
- **Video Support**: Video hosting for video-type posts

### 🛡️ Moderation & Safety
- **AI Content Validation**: Automated checks for inappropriate content
- **Report System**: User reporting for posts and members
- **Moderation Queue**: Review and resolve reports
- **Bulk Actions**: Resolve multiple reports efficiently

## 🏗️ Tech Stack

### Backend Framework
- **Spring Boot 3.5.10**: Modern Java framework for microservices
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Object-relational mapping with Hibernate
- **Spring Validation**: Input validation and constraints

### Database & Caching
- **MySQL**: Relational database for data persistence
- **Redis**: In-memory caching for performance optimization

### External Services
- **Google Gemini API**: AI-powered content analysis, translation, and summarization
- **Cloudinary**: Cloud-based image management
- **PayOS**: Vietnamese payment gateway integration

### Documentation & Testing
- **SpringDoc OpenAPI**: API documentation with Swagger UI
- **Spring Boot Test**: Unit and integration testing
- **Spring Security Test**: Security testing utilities

### Build & Dependency Management
- **Maven 3.x**: Project build and dependency management
- **Lombok**: Boilerplate code reduction

### Additional Libraries
- **OAuth2 Resource Server**: Secure API authentication
- **Mail Starter**: Email notification support

## 🏛️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                         │
├─────────────────────────────────────────────────────────┤
│              REST API / WebSocket (SSE)                 │
├─────────────────────────────────────────────────────────┤
│                   Controller Layer                       │
│     (Request routing, validation, response handling)    │
├─────────────────────────────────────────────────────────┤
│                    Service Layer                        │
│  (Business logic, transactions, orchestration)          │
├─────────────────────────────────────────────────────────┤
│                  Repository Layer                       │
│        (Data access, ORM, database queries)             │
├─────────────────────────────────────────────────────────┤
│              Data & External Services                   │
│  MySQL | Redis | Google Gemini | Cloudinary | PayOS   │
└─────────────────────────────────────────────────────────┘
```

**Layered Architecture Benefits:**
- Clear separation of concerns
- Easy testing and maintenance
- Scalability and flexibility
- Reusable components

## 📁 Project Structure

```
SEP490-Vtuber-Fanhub/
├── src/
│   ├── main/
│   │   ├── java/com/sep490/vtuber/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access
│   │   │   ├── model/entity/        # JPA entities
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── security/            # Security configuration
│   │   │   ├── exception/           # Exception handling
│   │   │   ├── util/                # Utility classes
│   │   │   └── VtuberFanhubApplication.java
│   │   └── resources/
│   │       ├── application.properties      # Application configuration
│   │       └── application-*.properties    # Profile-specific configs
│   └── test/                        # Unit and integration tests
├── pom.xml                          # Maven configuration
├── BUSINESS_RULES.txt               # Detailed business rules
├── sequence-diagrams.md             # Architecture diagrams
└── README.md                        # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java 17+** ([Download](https://www.oracle.com/java/technologies/downloads/#java17))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://www.mysql.com/downloads/))
- **Redis 6.0+** ([Download](https://redis.io/download))

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/RandomNameGo/SEP490-Vtuber-Fanhub.git
   cd SEP490-Vtuber-Fanhub
   ```

2. **Configure application properties**
   
   Create `src/main/resources/application-local.properties`:
   ```properties
   # Server Configuration
   server.port=8080
   server.servlet.context-path=/api
   
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/vtuber_fanhub
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   
   # JPA/Hibernate Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
   
   # Redis Configuration
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   spring.data.redis.database=0
   
   # Mail Configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   
   # Google Gemini API
   google.genai.api-key=your_gemini_api_key
   
   # Cloudinary Configuration
   cloudinary.cloud-name=your_cloud_name
   cloudinary.api-key=your_api_key
   cloudinary.api-secret=your_api_secret
   
   # PayOS Configuration
   payos.client-id=your_client_id
   payos.api-key=your_api_key
   payos.checksum-key=your_checksum_key
   ```

3. **Create database**
   ```bash
   mysql -u root -p
   CREATE DATABASE vtuber_fanhub;
   EXIT;
   ```

4. **Start Redis server**
   ```bash
   redis-server
   ```

5. **Build and run the application**
   ```bash
   # Build the project
   mvn clean package
   
   # Run the application
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
   
   # Or run the JAR directly
   java -jar target/vtuber-fanhub-0.0.1-SNAPSHOT.jar
   ```

6. **Access the application**
   - **API Base URL**: `http://localhost:8080`
   - **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
   - **API Docs**: `http://localhost:8080/v3/api-docs`

## 📦 Core Modules

### User & Authentication Module
Manages user registration, login, role-based access control, and profile management.

**Key Components:**
- User registration with email OTP verification
- JWT token generation and validation
- Profile customization (avatars, frames, badges)
- Role assignment and permission management

### Community (FanHub) Module
Enables VTubers to create and manage exclusive communities.

**Key Components:**
- FanHub creation and management
- Member management (invite, ban, unban)
- Subdomain configuration
- Community settings and rules

### Content Management Module
Handles all content creation, moderation, and engagement.

**Key Components:**
- Post creation (TEXT, IMAGE, VIDEO, POLL)
- AI-powered content validation
- Post approval workflow
- Comment system with gift support
- Like, bookmark, and share functionality

### Gacha & Shop Module
Implements the gamification and reward system.

**Key Components:**
- Banner management with time-limited events
- Gacha pull mechanics with weighted randomness
- Shop item catalog
- User inventory management
- Points deduction and balance tracking

### Notification Module
Delivers real-time updates to users.

**Key Components:**
- Server-Sent Events (SSE) implementation
- Event publishing and subscription
- Notification persistence
- Multi-channel delivery (in-app, email)

### Moderation & Reporting Module
Ensures community safety and content quality.

**Key Components:**
- Report submission system
- Moderation queue management
- Report resolution workflow
- Ban management and history
- AI content filtering

## ⚙️ Configuration

### Application Profiles

The application supports multiple profiles:

- **local**: Development environment with MySQL and Redis locally
- **dev**: Development server environment
- **prod**: Production environment with optimizations

Run with profile:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Key Configuration Files

- **application.properties**: Base configuration
- **application-local.properties**: Local development settings
- **application-dev.properties**: Development server settings
- **application-prod.properties**: Production settings

### Important Configuration Properties

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# JWT Security
jwt.secret=your-secret-key
jwt.expiration=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587

# Database Connection Pool
spring.datasource.hikari.maximum-pool-size=20
```

## 📖 API Documentation

### Interactive API Docs

Once the application is running, access Swagger UI:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

### API Endpoints Overview

**Authentication**
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-otp` - Verify email OTP
- `POST /api/auth/refresh-token` - Refresh JWT token

**User Profile**
- `GET /api/users/{userId}` - Get user profile
- `PUT /api/users/{userId}` - Update user profile
- `GET /api/users/{userId}/items` - Get user inventory

**FanHub Management**
- `POST /api/fanhubs` - Create FanHub
- `GET /api/fanhubs` - List FanHubs
- `PUT /api/fanhubs/{fanhubId}` - Update FanHub
- `POST /api/fanhubs/{fanhubId}/members` - Add member
- `DELETE /api/fanhubs/{fanhubId}/members/{memberId}` - Remove member

**Posts**
- `POST /api/posts` - Create post
- `GET /api/posts/{postId}` - Get post
- `PUT /api/posts/{postId}` - Update post
- `DELETE /api/posts/{postId}` - Delete post
- `POST /api/posts/{postId}/approve` - Approve post
- `POST /api/posts/{postId}/reject` - Reject post

**Gacha & Shop**
- `POST /api/gacha/pull` - Perform gacha pull
- `GET /api/shop/items` - List shop items
- `POST /api/shop/purchase` - Purchase item

**Moderation**
- `POST /api/reports` - Submit report
- `GET /api/reports` - List reports
- `PUT /api/reports/{reportId}/resolve` - Resolve report

### Example API Call

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "fan@example.com",
    "username": "vtuber_fan",
    "password": "SecurePassword123!"
  }'

# Create a FanHub
curl -X POST http://localhost:8080/api/fanhubs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "My VTuber Community",
    "description": "Welcome to my fan community!",
    "subdomain": "my-vtuber"
  }'
```

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
   ```bash
   git clone https://github.com/RandomNameGo/SEP490-Vtuber-Fanhub.git
   cd SEP490-Vtuber-Fanhub
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. **Follow code style guidelines**
   - Use the project's code structure and naming conventions
   - Follow Spring Boot best practices
   - Add proper documentation and comments
   - Write unit tests for new features

4. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```

5. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```

6. **Open a Pull Request**
   - Provide a clear description of changes
   - Reference any related issues
   - Ensure tests pass

## 📋 Development Guidelines

### Code Structure
- Follow the layered architecture pattern
- Separate concerns (Controllers, Services, Repositories)
- Use DTOs for API communication
- Implement proper exception handling

### Naming Conventions
- Classes: `PascalCase` (e.g., `UserService`)
- Methods: `camelCase` (e.g., `getUserById`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PAGE_SIZE`)
- Database tables: `snake_case` (e.g., `user_accounts`)

### Testing
- Write unit tests for service layer
- Use MockMvc for controller testing
- Aim for >80% code coverage
- Use @DataJpaTest for repository tests

### Documentation
- Add JavaDoc comments for public methods
- Document complex business logic
- Update API documentation with new endpoints
- Maintain README.md for major changes

## 📊 System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Java      | 17      | 17+        |
| MySQL     | 8.0     | 8.0+       |
| Redis     | 6.0     | 6.0+       |
| RAM       | 2GB     | 4GB+       |
| Storage   | 1GB     | 5GB+       |

