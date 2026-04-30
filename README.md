# UcakBiletOtamasyonu

## PostgreSQL Docker Kurulumu

```bash
docker run --name my-postgres -e POSTGRES_PASSWORD=gizlisifrem -p 5432:5432 -d postgres
```

## pgAdmin Docker Kurulumu

```bash
docker run --name my-pgadmin -p 5050:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=admin -d dpage/pgadmin4
```

UcakBiletOtamasyonu, Spring Boot ile geliştirilmiş bir uçak bileti otomasyonu projesidir.  
Şu anda projede JWT tabanlı authentication, refresh token akışı, HttpOnly cookie ile token taşıma ve logout desteği bulunmaktadır.

## Teknolojiler

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JJWT
- Lombok

## Kurulum

### 1. Veritabanı

PostgreSQL üzerinde bir veritabanı oluşturun ve şu şemayı ekleyin:

```sql
CREATE SCHEMA "UcakBiletOtamasyonu";
```

### 2. Uygulama ayarları

`src/main/resources/application.properties` içindeki ya da environment variable olarak verilen değerleri kontrol edin:

```properties
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=postgres
COOKIE_SECURE=false
```

## Çalıştırma

```bash
./mvnw spring-boot:run
```

Windows için:

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

Body:

```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

Başarılı olursa:

- Response body içinde `accessToken` döner
- `refreshToken` cookie olarak set edilir

### Refresh Token

```http
POST /api/v1/auth/refresh-token
```

Body gerekmez. Cookie ile çalışır.

### Logout

```http
POST /api/v1/auth/logout
```

Refresh token cookie'sini temizler ve DB kaydını siler.

## Notlar

- Refresh token cookie olarak `HttpOnly` biçimde tutulur.
- Logout sırasında cookie temizleme ve `Clear-Site-Data` header'i kullanılır.
- `User` entity'sinde giriş anahtarı olarak `email` kullanılır.

## Exception Mimarisi

Projede hata yönetimi custom bir yapı ile ele alınır:

- `BaseException`: Uygulama seviyesinde fırlatılan özel exception sınıfıdır.
- `ErrorMessage`: Hata tipini ve detay bilgisini taşır.
- `MessageType`: Standart hata kodlarını ve mesajlarını tutar.
- `GlobalExceptionHandler`: Controller katmanından gelen exception'ları yakalayıp tek formatta response üretir.
- `ApiError`: Hata response'unun üst seviyedeki taşıyıcı modelidir.

Akış şu şekildedir:

1. Service veya config katmanında bir problem oluşursa `BaseException` fırlatılır.
2. `GlobalExceptionHandler` bunu yakalar.
3. Response, standart bir `ApiError` yapısı ile döner.
4. Böylece frontend tarafı tek tip hata formatı ile çalışır.

Örnek response yaklaşımı:

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
