# FlatMate

A REST API for managing shared household expenses, calculating balances, and working out who owes whom.

## Overview

FlatMate helps people living together record shared expenses and understand how costs should be split across a household. It supports household and user management, expense tracking, balance calculations, and settlement suggestions.

The API is built with Java and Spring Boot, persists data in PostgreSQL, and exposes interactive API documentation through Swagger UI.

## Features

- Create and retrieve households
- Create and retrieve users and list household members
- Record, list, update, and delete expenses
- Associate expenses with a household and the user who paid
- Filter household expenses by category and date range, and sort results
- Calculate each household member's net balance
- Generate settlement suggestions showing who should pay whom
- Validate requests and return consistent API errors
- Explore and try endpoints using Swagger UI

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **Maven**
- **JUnit**
- **Swagger UI / OpenAPI**
- **Git and GitHub**

## Getting Started

### Prerequisites

- JDK 21 or compatible Java 21 installation
- Maven (or use the Maven Wrapper if one is included in the repository)
- PostgreSQL

### 1. Clone the repository

```bash
git clone https://github.com/doawnly/flatmate.git
cd flatmate
```

### 2. Configure the database

Create a PostgreSQL database for the application. Configure the database URL, username, and password using the application's existing Spring configuration or environment variables. Do not commit real database credentials to GitHub.

Check `src/main/resources/application.properties` (or the corresponding configuration file in the project) to see which property names the application currently expects.

### 3. Run the application

From the project root, run:

```bash
mvn spring-boot:run
```

The API is served locally at `http://localhost:8080` by default.

## API Documentation

When the application is running, open Swagger UI to browse endpoints and send test requests:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI specification: `http://localhost:8080/v3/api-docs`

The API includes endpoints for expenses, households, and users. Available operations are documented in Swagger UI.

## Running Tests

Run the automated test suite from the project root:

```bash
mvn test
```

The current test suite has **75 passing tests**.

## Project Status

The current version focuses on the backend REST API, including household and user management, expense operations, balance and settlement calculations, validation, error handling, and API documentation.

A frontend interface and additional household tools such as shopping lists or spending summaries may be considered as future enhancements; they are not part of the current implemented feature set.