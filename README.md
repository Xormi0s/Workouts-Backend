# Workouts Backend

Personal project for a backend that supports a React Native mobile application for tracking gym workouts.

## Tech stack

- **Java 25**, **Spring Boot 4.1.0**
- **Maven** multi-module build
- **Spring Security** + **JWT** (access tokens) and opaque, hashed refresh tokens (via [jjwt](https://github.com/jwtk/jjwt) 0.12.x)
- **Spring Data JPA** / **Hibernate** on **MySQL 8.4**
- **Bucket4j** for in-memory rate limiting
- **Lombok**

## Project structure

The project is a two-module Maven reactor build:

```
workouts-backend/
├── workouts-common/     # Shared persistence layer: entities, repositories, seeders
└── workouts-v1-api/     # The runnable Spring Boot application: security, controllers, config
```

`workouts-common` intentionally has no web/security dependencies (only `spring-boot-starter-data-jpa` + Lombok) so it stays a pure, reusable persistence layer that any future API module could depend on. `workouts-v1-api` depends on `workouts-common` and contains everything specific to this particular REST API — JWT handling, filters, controllers.

---

## `workouts-common`

### `entity` / `entity.auth`
| Class | Purpose |
|---|---|
| `ApplicationUser` | The user account. Holds credentials (`username`, hashed `password`), account-status flags (`enabled`, `locked`, `expired`, `credentialsExpired`) that Spring Security enforces directly, its `roles`, and account-lockout bookkeeping (`failedLoginAttempts`, `lockedAt`). |
| `Role` | A named role (e.g. `ROLE_USER`), many-to-many with `ApplicationUser`. |
| `RefreshToken` | A single refresh token session for a user. Stores a **SHA-256 hash** of the token (never the raw value), its expiry, and whether it has been revoked. |

### `repository`
| Interface | Purpose |
|---|---|
| `UserRepository` | `findByUsername` (eagerly fetches roles via `JOIN FETCH` to avoid lazy-loading issues outside a transaction), `existsByUsername`. |
| `RoleRepository` | `findByName`. |
| `RefreshTokenRepository` | `findByTokenHash`, plus a bulk `revokeAllTokensByApplicationUser` update used for session revocation. |

### `constant`
| Class | Purpose |
|---|---|
| `RoleNames` | Well-known role name constants (`ROLE_USER`, `ROLE_ADMIN`), shared by both modules. |

### `seeder`
| Class | Purpose |
|---|---|
| `Seeder` | Simple `seed()` contract implemented by anything that needs to populate startup data. |
| `RoleSeeder` | Idempotently ensures `ROLE_USER` exists. |
| `DatabaseSeeder` | `ApplicationRunner` that autowires every `Seeder` bean and runs them after the application context (and schema) is ready — adding a new seeder later requires no wiring beyond implementing `Seeder`. |

---

## `workouts-v1-api`

### `security.config`
| Class | Purpose |
|---|---|
| `SecurityConfig` | The `SecurityFilterChain`: stateless sessions, CSRF disabled, `register`/`login`/`refresh` are public, everything else requires a valid JWT. Wires the custom filters in (`RateLimitFilter` → `JwtAuthFilter`) and registers the `BCryptPasswordEncoder`/`AuthenticationManager` beans. |
| `JwtProperties` | Configurable JWT settings (`jwt.secret`, access/refresh token lifetimes), bound from `application.properties`. |
| `RateLimiterProperties` | Configurable rate-limit capacity/refill window for `/login` and `/register`. |
| `AccountLockoutProperties` | Configurable failed-attempt threshold and lockout duration. |
| `JwtAuthEntryPoint` | Writes a consistent JSON `401` response whenever an unauthenticated request hits a protected endpoint. |

### `security.filter`
| Class | Purpose |
|---|---|
| `JwtAuthFilter` | Reads the `Authorization: Bearer` header, validates the JWT, and populates Spring Security's context for the rest of the request. |
| `RateLimitFilter` | Applies a per-IP token bucket to `POST /login` and `POST /register` only; everything else passes through untouched. Returns `429` + `Retry-After` when exceeded. |

### `security.service`
| Class | Purpose |
|---|---|
| `AuthService` | Orchestrates the whole auth flow: registration, login (with account-lockout bookkeeping), token refresh (with rotation + reuse-detection), logout, logout-everywhere, and password changes. |
| `JwtService` | Issues and validates access tokens (subject, roles, issuer, expiry), signed HS256. |
| `RefreshTokenService` | Generates opaque refresh tokens, hashes them (SHA-256) before persisting, and resolves an incoming raw token back to its stored record by hashing it again. |
| `CustomUserDetailsService` | Bridges `ApplicationUser` to Spring Security's `UserDetails`, mapping roles to granted authorities and account-status flags. |
| `RateLimiterService` | Holds the in-memory Bucket4j buckets (one map per protected endpoint, keyed by client IP). |

### `security.controller`
| Class | Purpose |
|---|---|
| `AuthController` | The public REST surface — see [API endpoints](#api-endpoints) below. |

### `security.dto`
Request/response records: `RegisterRequest`, `LoginRequest`, `RefreshRequest`, `LogoutRequest`, `ChangePasswordRequest`, `AuthResponse`, `ErrorResponse`.

### `security.exception`
| Class | Purpose |
|---|---|
| `GlobalExceptionHandler` | Maps all auth-related exceptions to consistent JSON error responses with proper HTTP status codes. |
| `TokenRefreshException`, `UsernameAlreadyExistsException` | Domain-specific exceptions used by `AuthService`. |

### `controller`
| Class | Purpose |
|---|---|
| `WorkoutController` | Placeholder for the actual workout-tracking API (currently just a protected test endpoint). |

---

## Authentication & security features

- **JWT access tokens** (15 min default) + **opaque, rotating refresh tokens** (7 days default), returned in the JSON response body (no cookies — the client is a native mobile app).
- **Refresh token rotation with reuse-detection**: every refresh consumes the old token and issues a new one; presenting an already-used token revokes *all* of that user's sessions as a precaution.
- **Refresh tokens are stored hashed** (SHA-256), not in plaintext — a database leak alone doesn't yield usable tokens.
- **Rate limiting** on `/login` and `/register`, per client IP, via Bucket4j.
- **Account lockout**: after repeated failed login attempts, an account locks for a configurable duration and auto-unlocks on the next attempt once the cooldown has passed.
- **Logout-everywhere** and **change password** (which also revokes all existing sessions) endpoints.
- Passwords hashed with **BCrypt**.

Deliberately out of scope for now: a full "forgot password" flow (would require adding email + mail infrastructure), CORS (native client, not a browser), and HTTPS termination (a deployment concern, not application code).

## API endpoints

| Method | Path | Auth required | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | No | Create an account, returns tokens immediately. |
| `POST` | `/api/v1/auth/login` | No | Authenticate, returns tokens. |
| `POST` | `/api/v1/auth/refresh` | No (valid refresh token) | Rotates a refresh token for a new token pair. |
| `POST` | `/api/v1/auth/logout` | Yes | Revokes one specific refresh token. |
| `POST` | `/api/v1/auth/logout-all` | Yes | Revokes every refresh token for the current user. |
| `POST` | `/api/v1/auth/change-password` | Yes | Changes the password and revokes all existing sessions. |

## Running locally

```bash
# 1. Start MySQL + phpMyAdmin (http://localhost:8081)
docker compose up -d

# 2. Provide secrets/config as environment variables (see database.env / jwt.env)
#    Required: MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, JWT_SECRET
#    (JWT_SECRET must be a base64-encoded value of at least 32 raw bytes, e.g. `openssl rand -base64 32`)

# 3. Run the app (builds workouts-common first)
./mvnw -pl workouts-v1-api -am spring-boot:run
```

The app starts on `http://localhost:8080`.
