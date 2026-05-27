# Flight Booking Automation (UcakBiletOtamasyonu)

UcakBiletOtamasyonu is a flight booking automation project built with Spring Boot.

Swagger UI: `http://localhost:8080/swagger-ui/index.html#/`

## Highlights
- Stripe payments
- OAuth2 (Google Login)
- Pub/Sub (Spring Events)
- Refresh token cookie: during login/refresh flows, the refresh token is set as an `HttpOnly` cookie (safer against XSS).
- Voice Assistant (STT + Chat + TTS): upload audio -> transcribe -> chat -> generate mp3. No conversation memory is stored on the backend.

## Technologies
- Java 17
- Spring Boot
- Spring Security (JWT + OAuth2 Client)
- Spring Data JPA
- PostgreSQL
- Stripe
- Spring AI (Chat + STT + TTS)

## Quick Start

### 1) PostgreSQL (Docker)
```bash
docker run --name my-postgres -e POSTGRES_PASSWORD=gizlisifrem -p 5432:5432 -d postgres
```

Optional pgAdmin:
```bash
docker run --name my-pgadmin -p 5050:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=admin -d dpage/pgadmin4
```

### 2) DB schema
Default schema used by the project:
```sql
CREATE SCHEMA "UcakBiletOtamasyonu";
```

### 3) Environment Variables
`.env` is imported from `src/main/resources/application.yml`:
- `./.env`

Example `.env`:
```properties
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=gizlisifrem

OPENAI_API_KEY=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=...
# Gmail App Password is typically provided without spaces (16 chars).
MAIL_PASSWORD=...
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true

STRIPE_SECRET_KEY=...
STRIPE_SUCCESS_URL=http://localhost:8080/api/payments/success
STRIPE_CANCEL_URL=http://localhost:8080/api/payments/cancel

COOKIE_SECURE=false
```

### 4) Run
```bash
./mvnw spring-boot:run
```

Windows:
```bash
mvnw.cmd spring-boot:run
```

## Auth Endpoints

Base path:
```text
/api/v1/auth
```

### Register
```http
POST /api/v1/auth/register
```

Body:
```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

### Login
```http
POST /api/v1/auth/login
```

On success:
- Response body includes `accessToken`
- `refreshToken` is set as an HttpOnly cookie

### Email Verification
After registration, the user is created with `enabled=false`.
`OnRegistrationCompleteEvent` is published; the listener creates a `VerificationToken` and sends the verification code via email.

Verify:
```http
POST /api/v1/auth/verify-email
```

Body:
```json
{
  "email": "test@example.com",
  "verificationCode": "123456"
}
```

Resend:
```http
POST /api/v1/auth/resend-verification-email
```

### Google Login
To start the Google login flow:
```http
GET /oauth2/authorization/google-login
```

Callback endpoint:
```text
/login/oauth2/code/google-login
```

In Google Cloud Console, you must register this callback as an authorized redirect URI.

### Refresh Token
```http
POST /api/v1/auth/refresh-token
```
No body required. Uses cookie-based refresh token.

### Logout
```http
POST /api/v1/auth/logout
```
Clears the refresh token cookie and deletes the DB record.

## Flight Endpoints (Summary)
- `GET /api/flights/search`
- `GET /api/flights/getAll`
- `GET /api/flights/getFlight/{id}`
- `GET /api/flights/airports`
- `POST /api/flights/save` (auth)
- `PUT /api/flights/update/{id}` (auth)
- `DELETE /api/flights/delete/{id}` (auth)

## Payments (Stripe)
Typical flow:
1. Create a reservation
2. Create a checkout session -> returns `sessionUrl`
3. Frontend redirects to Stripe Checkout using `sessionUrl`
4. Stripe redirects to success/cancel URLs

## Voice Assistant
Backend flow:
- `POST /api/v1/voice/process` (multipart/form-data)
  - `audio` is required
- `GET /api/v1/voice/audio/{conversationId}` (mp3 stream)

Note: This project does not store "conversation memory" on the backend. Each voice request is independent.

## Spring AI (Function Calling / Tool)
This project uses Spring AI "tools" (function-calling-like approach):
- `ChatClient` is used to extract intent/slots and generate responses.
- For flight search, there is a tool called `FlightSearchTool`.
  - Tool methods are annotated with `@Tool`: `searchFlights(...)` and `searchFlightsDetailed(...)`
  - The tool calls the backend flight search endpoint (`GET /api/flights/search`) and returns the raw JSON response.

In short: Chat -> (if needed) Tool -> API -> result -> assistant response.
 
## Spring Events
Spring Boot Events work roughly in 4 steps:
1. Registration (Startup): When the app starts, Spring discovers all `@EventListener` methods and caches subscriptions.
2. Publish: When an event is published, it is passed to `ApplicationEventMulticaster`.
3. Match & Dispatch: The multicaster matches event type to listeners and triggers them in order.
4. Default behavior (Synchronous): By default, dispatch is synchronous. Use `@Async` for async listeners.

Example in this project:
- `AuthenticationServiceImpl` -> publishes `OnRegistrationCompleteEvent`
- `RegistrationListener` -> generates a verification code and sends an email

## Exception Architecture

The project uses a custom error handling structure:

- `BaseException`: Custom exception thrown at the application/service layer.
- `ErrorMessage`: Holds the error type and detail.
- `MessageType`: Contains standard error codes/messages.
- `GlobalExceptionHandler`: Catches exceptions from the controller layer and returns a single response format.
- `ApiError`: Top-level error response model.

Flow:

1. If a problem occurs in the service/config layer, `BaseException` is thrown.
2. `GlobalExceptionHandler` catches it.
3. The response is returned in a standard `ApiError` format.
4. This ensures the frontend can rely on a single error schema.

Example error response:

```json
{
  "status": 500,
  "exception": {
    "path": "/api/v1/auth/login",
    "createTime": "2026-04-30T12:00:00",
    "hostName": "server-name",
    "message": "email or password is invalid : test@example.com"
  }
}
```
