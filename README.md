# Sushi Shop

Online sushi delivery shop with real-time order tracking.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.7-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-6-blue)
![Docker](https://img.shields.io/badge/Docker-✓-blue)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

---

## Overview

Sushi Shop is a full-stack monolith application for online food ordering.
It covers the full order lifecycle: browsing the menu, placing an order,
payment, and real-time delivery tracking.

The system supports three roles:
- **User** — browsing, ordering, tracking
- **Admin** — managing products, promotions, orders, audit logs
- **Courier** — managing assigned deliveries and updating delivery statuses

---

## Demo

[![Sushi Shop Demo](https://img.youtube.com/vi/RCVogRldj4Q/maxresdefault.jpg)](https://www.youtube.com/watch?v=RCVogRldj4Q)

---

## Features

### Storefront
- Product catalog with categories, search, images, reviews, and ratings
- Active promotions with automatic discount calculation
- Shopping cart and checkout
- Order tracking with real-time status updates via WebSocket
- Stripe payment integration
- User profile management
- JWT authentication with email verification
- Google OAuth2 login
- Responsive design

### Admin Panel
- Full product CRUD with image management
- Promotion management with conflict prevention
- Order management with status workflow
- Real-time order updates via WebSocket
- Audit logs with filtering

### Courier Panel
- Order management dashboard
- Delivery status transitions

---

## Key Engineering Highlights

- **Feature-based architecture** — organized the monolith by business domains with clear separation of controllers, services, repositories, DTOs, and mappers
- **Database optimization** — eliminated N+1 queries using `EntityGraph`, batch fetching, and targeted database indexes
- **Dynamic filtering** — implemented composable filtering with Spring Data JPA Specifications for products, orders, and audit logs
- **Promotion conflict prevention** — prevents a product from being assigned to multiple active promotions
- **Real-time order tracking** — WebSocket/STOMP updates order status automatically across clients
- **Cross-cutting audit logging** — implemented with a custom `@Auditable` annotation and AOP
- **Role-based access control** — separate workflows and permissions for User, Admin, and Courier roles
- **API rate limiting** — configurable request throttling using Bucket4j
- **Drag-and-drop image reordering** — admin can reorder product images
- **Service layer separation** — split large services into focused services (OrderCreationService, OrderQueryService, ProductImageService, ReviewReplyService)
- **Validation hierarchy** — custom exceptions for all HTTP error scenarios with centralized handling
- **Token management** — separate TokenService for verification and password reset tokens
- **Scheduled cleanup** — automated cleanup of expired tokens and rate-limit buckets
- **Email verification flow** — async email sending with styled HTML templates

---

## Architecture

```
React (Vite)
    ↓ REST
Spring Boot API
    ├── PostgreSQL (data)
    ├── Redis (caching)
    ├── WebSocket/STOMP (real-time order updates)
    ├── Stripe (payments)
    ├── Google OAuth2 (login)
    ├── Mail (SMTP)
    ├── Rate Limiting (Bucket4j)
    └── Prometheus + Grafana (monitoring)
```

---

## Tech Stack

### Backend

- Java 21
- Spring Boot 4.0.7
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Redis
- Flyway
- JWT
- MapStruct
- WebSocket / STOMP
- Stripe
- Google OAuth2
- Bucket4j
- Testcontainers
- Spring AOP
- Spring Scheduling
- Spring Mail

### Frontend

- React 19
- TypeScript 6
- Vite 8
- React Router 7
- Axios
- STOMP.js / SockJS
- dnd-kit
- Tailwind CSS 4
- CSS Modules

### DevOps

- Docker
- Docker Compose
- Prometheus
- Grafana

---

## Quick Start

```bash
git clone https://github.com/AntonBas/sushi-shop.git
cd sushi-shop
cp .env.example .env
docker compose up -d
```

Fill in the required values in `.env`.
See [`.env.example`](.env.example) for all available variables.


| Service     | URL                                   |
| ----------- | ------------------------------------- |
| Frontend    | http://localhost:5173                 |
| Backend API | http://localhost:8080                 |
| Swagger     | http://localhost:8080/swagger-ui.html |
| Prometheus  | http://localhost:9090                 |
| Grafana     | http://localhost:3000                 |

---

## Testing

### Backend

- **Unit tests:** JUnit 5 + Mockito — services, mappers, validators, aspects
- **Integration tests:** Testcontainers with real PostgreSQL — Flyway migrations, repository queries
- **Controller tests:** MockMvc — REST API endpoints
- **Rate limiting tests:** Bucket4j token bucket behavior
- **Coverage:** Jacoco (~78% instruction coverage). Run `./gradlew jacocoTestReport`
  and open `backend/build/reports/jacoco/test/html/index.html`.

### Frontend

- **Unit/component tests:** Vitest + React Testing Library
- **Coverage:** `npm run test:coverage` (v8 provider), report at
  `frontend/coverage/index.html`. Test suite is a starting baseline, not
  full coverage yet — see `frontend/src/**/*.test.{ts,tsx}` for what's
  covered so far.

---

## Test Users

These accounts are for demonstration purposes only.

| Role    | Email             | Password |
|---------|-------------------|----------|
| User    | user@test.com     | user     |
| Admin   | admin@test.com    | admin    |
| Courier | courier@test.com  | courier  |

---

## License

[MIT](LICENSE)