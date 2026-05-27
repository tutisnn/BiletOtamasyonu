# UcakBiletOtamasyonu

UcakBiletOtamasyonu, Spring Boot ile gelistirilmis bir ucak bileti otomasyonu projesidir.

Swagger UI: `http://localhost:8080/swagger-ui/index.html#/`

## Highlights
- Stripe ile odeme
- OAuth2 (Google Login)
- Pub/Sub (Spring Events)
- Refresh token cookie: Login/refresh akisinda refresh token `HttpOnly` cookie olarak set edilir (XSS'e karsi daha guvenli).
- Voice Assistant (STT + Chat + TTS): Ses yukle -> transcribe -> chat -> mp3 uretilir. Backend tarafinda konusma hafizasi tutulmaz.

## Teknolojiler
- Java 17
- Spring Boot
- Spring Security (JWT + OAuth2 Client)
- Spring Data JPA
- PostgreSQL
- Stripe
- Spring AI (Chat + STT + TTS)

## Hizli Baslangic

### 1) PostgreSQL (Docker)
```bash
docker run --name my-postgres -e POSTGRES_PASSWORD=gizlisifrem -p 5432:5432 -d postgres
```

Istersen pgAdmin:
```bash
docker run --name my-pgadmin -p 5050:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=admin -d dpage/pgadmin4
```

### 2) DB schema
Projede varsayilan schema:
```sql
CREATE SCHEMA "UcakBiletOtamasyonu";
```

### 3) Ortam Degiskenleri
`src/main/resources/application.yml` icinde `.env` import ediliyor:
- `./.env`

Ornek `.env`:
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
# Gmail App Password genelde bosluksuz kullanilir (16 karakter). Bosta/space varsa kaldir.
MAIL_PASSWORD=...
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true

STRIPE_SECRET_KEY=...
STRIPE_SUCCESS_URL=http://localhost:8080/api/payments/success
STRIPE_CANCEL_URL=http://localhost:8080/api/payments/cancel

COOKIE_SECURE=false
```

### 4) Calistirma
```bash
./mvnw spring-boot:run
```

Windows:
```bash
mvnw.cmd spring-boot:run
```

## Auth Endpointleri

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

Basarili olursa:
- Response body icinde `accessToken` doner
- `refreshToken` HttpOnly cookie olarak set edilir

### Email Verification
Register sonrasi kullanici `enabled=false` durumda olusur.
`OnRegistrationCompleteEvent` publish edilir, listener `VerificationToken` olusturur ve dogrulama kodunu e-posta ile gonderir.

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
Google giris akisini baslatmak icin:
```http
GET /oauth2/authorization/google-login
```

Callback endpoint:
```text
/login/oauth2/code/google-login
```

Google Cloud Console tarafinda yetkili redirect URI olarak bu callback adresini tanimlaman gerekir.

### Refresh Token
```http
POST /api/v1/auth/refresh-token
```
Body gerekmez. Cookie ile calisir.

### Logout
```http
POST /api/v1/auth/logout
```
Refresh token cookie'sini temizler ve DB kaydini siler.

## Ucus Endpointleri (Ozet)
- `GET /api/flights/search`
- `GET /api/flights/getAll`
- `GET /api/flights/getFlight/{id}`
- `GET /api/flights/airports`
- `POST /api/flights/save` (auth)
- `PUT /api/flights/update/{id}` (auth)
- `DELETE /api/flights/delete/{id}` (auth)

## Odeme (Stripe)
Tipik akis:
1. Reservation olustur
2. Checkout session olustur -> `sessionUrl` doner
3. Frontend `sessionUrl` ile Stripe Checkout'a gider
4. Stripe, success/cancel URL'lerine redirect eder

## Voice Assistant
Backend akis:
- `POST /api/v1/voice/process` (multipart/form-data)
  - `audio` alani zorunludur
- `GET /api/v1/voice/audio/{conversationId}` (mp3 stream)

Not: Bu projede backend tarafinda "konusma hafizasi" tutulmaz. Her sesli istek bagimsizdir.

## Spring AI (Function Calling / Tool)
Bu projede Spring AI ile "tool" (function calling benzeri) yaklasimi kullaniliyor:
- `ChatClient` ile kullanici mesaji alinip niyet/slot cikartma ve cevap uretme yapiliyor.
- Ucus arama gibi islemler icin `FlightSearchTool` adinda bir tool var.
  - Tool metodlari `@Tool` ile isaretli: `searchFlights(...)` ve `searchFlightsDetailed(...)`
  - Tool, backend icinde `GET /api/flights/search` endpointine istek atip JSON sonucu geri donuyor.

Kisaca: Chat -> (gerekirse) Tool -> API -> sonuc -> asistan cevabi akisi var.
 
## Spring Events
Spring Boot Events arka planda kisaca su 4 adimla calisir:
1. Kayit (Startup): Uygulama baslarken Spring, `@EventListener` yazan tum metotlari bulur ve hangi olayi dinlediklerini bir onbellege (cache) kaydeder.
2. Firlatma (Publish): Bir olay firlatildiginda bu olay `ApplicationEventMulticaster`'a iletilir.
3. Eslesme ve Dagitim: Multicaster, gelen olay turune bakar, dinleyicileri bulur ve sirayla tetikler.
4. Varsayilan davranis (Senkron): Dagitim islemi varsayilan olarak senkron calisir. Beklememesi icin `@Async` kullanilabilir.

Bu projede ornek:
- `AuthenticationServiceImpl` -> `OnRegistrationCompleteEvent` publish eder
- `RegistrationListener` -> verification code uretip email gonderir
## Exception Mimarisi

Projede hata yonetimi custom bir yapi ile ele alinir:

- `BaseException`: Uygulama seviyesinde firlatilan ozel exception sinifidir.
- `ErrorMessage`: Hata tipini ve detay bilgisini tasir.
- `MessageType`: Standart hata kodlarini ve mesajlarini tutar.
- `GlobalExceptionHandler`: Controller katmanindan gelen exception'lari yakalayip tek formatta response uretir.
- `ApiError`: Hata response'unun ust seviyedeki tasiyici modelidir.

Akis su sekildedir:

1. Service veya config katmaninda bir problem olusursa `BaseException` firlatilir.
2. `GlobalExceptionHandler` bunu yakalar.
3. Response, standart bir `ApiError` yapisi ile doner.
4. Boylece frontend tarafi tek tip hata formati ile calisir.

Ornek response yaklasimi:

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
