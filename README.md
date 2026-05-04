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
    "
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

### Email Verification

Register sonrası kullanıcı `emailVerified=false` durumda oluşturulur.  
Doğrulama kodu şu an uygulama loglarına yazılır.

#### Verify Email

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

Doğrulama başarılı olursa kullanıcı `emailVerified=true` olur ve login yapabilir.

#### Not

- Doğrulama kodu 6 hanelidir.
- Kod 15 dakika geçerlidir.
- Kod süresi dolarsa yeni register akışı ya da yeniden kod üretme mekanizması gerekir.

### Google Login

Google giriş akışını başlatmak için doğrudan şu adresi kullan:

```http
GET /oauth2/authorization/google-login
```

Callback endpoint:

```text
/login/oauth2/code/google-login
```

Google Cloud Console tarafında yetkili redirect URI olarak bu callback adresini tanımlaman gerekir.

### Facebook Login

Facebook giriş akışını başlatmak için doğrudan şu adresi kullan:

```http
GET /oauth2/authorization/facebook-login
```

Callback endpoint:

```text
/login/oauth2/code/facebook-login
```

Facebook Developer Console tarafında yetkili redirect URI olarak bu callback adresini tanımlaman gerekir.

### Refresh Token

```http
POST /api/v1/auth/refresh-token
```

Body gerekmez. Cookie ile çalışır.

### Logout
Ana Kaynak:
https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html


```http
POST /api/v1/auth/logout
```

Refresh token cookie'sini temizler ve DB kaydını siler.
Logout sırasında cookie temizleme ve `Clear-Site-Data` ClearSiteDataHeaderWriter sınıfı kullanılır
SecurityContextLogoutHandler sınıfının kullanımı

QATUH
https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html

## Auth Policy

Authentication tarafındaki davranış kuralları:

- `email + password` ile kayıtlı kullanıcı tekrar register olamaz.
- `email + password` ile kayıtlı kullanıcı Google veya Facebook ile giriş yapamaz.
- Google ile kayıtlı kullanıcı aynı Google hesabıyla tekrar giriş yapabilir.
- Facebook ile kayıtlı kullanıcı aynı Facebook hesabıyla tekrar giriş yapabilir.
- Aynı email farklı sosyal provider ile eşleşirse yeni hesap açılmaz.
- Aynı provider olmayan bir kayıt görülürse `email already registered` hatası döner.

Kısacası:

- `LOCAL` hesaplar sadece local login ile çalışır.
- `GOOGLE` hesaplar sadece Google login ile çalışır.
- `FACEBOOK` hesaplar sadece Facebook login ile çalışır.
- `LOCAL` hesaplar login olmadan önce email verification tamamlamalıdır.


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
## Kaynaklar
