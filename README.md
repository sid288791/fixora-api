# Fixora API - Backend Service

A Spring Boot application for managing application onboarding and user management with PostgreSQL database.

## Features

- **Application Onboarding**: Register applications with name, alias, owner email, and AD group mapping
- **User Management**: Create and manage users with email and AD group information
- **RESTful API**: Complete REST API for all operations
- **Docker Support**: Full Docker Compose setup with PostgreSQL

## Prerequisites

- Docker and Docker Compose installed (for PostgreSQL)
- JDK 17+ (JDK 21 recommended) for running the Spring Boot app locally
- Gradle wrapper (bundled, no separate install needed)

## Quick Start (current setup)

For now, **PostgreSQL runs in Docker** and the **Spring Boot API runs locally**
via Gradle. As we add more infrastructure (Redis, Kafka, etc.) we'll extend
`docker-compose.yml` and can containerize the API too.

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

This will automatically:
- Create the `fixora_db` database
- Initialize tables for `users` and `applications`
- Seed sample data (see `scripts/init.sql`)

### 2. Run the API locally

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=fixora_db
export DB_USER=fixora_user
export DB_PASSWORD=fixora_password

./gradlew bootRun
```

The API will be available at `http://localhost:8080/api`.

### Stopping

```bash
# Stop the app: Ctrl+C (or kill the bootRun process)
# Stop Postgres:
docker compose down       # keep data
docker compose down -v    # remove data volume too
```

## Project Structure

```
fixora-api/
├── src/main/java/org/simulynx/fixora/
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic
│   ├── entity/              # JPA entities
│   ├── repository/          # Data access layer
│   ├── dto/                 # Data transfer objects
│   └── FixoraApplication.java
├── src/main/resources/
│   └── application.yml      # Application configuration
├── scripts/
│   └── init.sql             # Database initialization
├── docker-compose.yml       # Docker Compose configuration (Postgres)
└── build.gradle             # Gradle build configuration
```

## API Endpoints

### Dashboard

```
GET    /api/dashboard                          # Get aggregated dashboard stats
```

### Application Onboarding

```
POST   /api/applications/onboard              # Register new application
GET    /api/applications                       # Get all applications
GET    /api/applications/{id}                 # Get application by ID
GET    /api/applications/alias/{alias}        # Get application by alias
GET    /api/applications/active/list          # Get active applications
GET    /api/applications/ad-group/{adGroup}   # Get applications mapped to an AD group
PUT    /api/applications/{id}                 # Update application
DELETE /api/applications/{id}                 # Delete application
```

### User Management

```
POST   /api/users                             # Create new user
GET    /api/users                             # Get all users
GET    /api/users/{id}                        # Get user by ID
GET    /api/users/email/{email}               # Get user by email
PUT    /api/users/{id}                        # Update user
DELETE /api/users/{id}                        # Delete user
```

## Example Requests

### Get Dashboard Stats

```bash
curl http://localhost:8080/api/dashboard | jq
```

Response includes:
- `totalApplicationCount`: Total count of all applications
- `activeApplicationCount`: Count of applications with status = ACTIVE
- `uniqueUserCount`: Count of unique users (distinct emails)
- `registeredApplications`: Complete list of all application objects

### Create Application

```bash
curl -X POST http://localhost:8080/api/applications/onboard \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Application",
    "alias": "my-app",
    "ownerEmail": "owner@example.com",
    "adGroupMapping": "dev-team,qa-team",
    "description": "Application description"
  }'
```

### Create User

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "adGroup": "dev-team"
  }'
```

### Get All Applications

```bash
curl http://localhost:8080/api/applications
```

### Get All Users

```bash
curl http://localhost:8080/api/users
```

### Get Applications By AD Group

```bash
curl http://localhost:8080/api/applications/ad-group/dev-team
```

Returns every application whose `ad_group_mapping` (comma-separated list, e.g.
`dev-team,qa-team`) contains the given AD group as an exact entry.

## Postman Collection

A Postman collection is maintained at `postman/Fixora-API.postman_collection.json`
with a request for every endpoint, organized into `Applications` and `Users`
folders. Import it into Postman and it will use the `baseUrl` collection
variable (defaults to `http://localhost:8080/api`).

Keep adding new requests to this file as new endpoints are introduced.

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    ad_group VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Applications Table
```sql
CREATE TABLE applications (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    alias VARCHAR(255) NOT NULL UNIQUE,
    owner_email VARCHAR(255) NOT NULL,
    ad_group_mapping VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Environment Variables

```
DB_HOST=postgres                    # Database host
DB_PORT=5432                        # Database port
DB_NAME=fixora_db                   # Database name
DB_USER=fixora_user                 # Database user
DB_PASSWORD=fixora_password         # Database password
```

## Stopping the Application

```bash
# Stop the local Spring Boot app: Ctrl+C in its terminal (or kill its PID)

# Stop Postgres container
docker compose down

# Stop Postgres and remove its data volume
docker compose down -v
```

## Logs

```bash
# Postgres container logs
docker compose logs -f postgres
```

## Building JAR Locally

```bash
./gradlew clean build
# JAR will be available at: build/libs/fixora-api-1.0-SNAPSHOT.jar
```

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Build Tool**: Gradle 7.6
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Bean Validation

## Support

For issues and questions, please refer to the project documentation.
