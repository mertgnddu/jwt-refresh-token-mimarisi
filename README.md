# JWT Refresh Token Mimarisi 

Bu proje, JWT tabanlı kimlik doğrulama yapısını **Refresh Token Mimarisi** ile genişletmek amacıyla geliştirilmiştir.

Projede temel olarak şu yapı kurulmuştur:

- Kullanıcı giriş yaptığında `access token` ve `refresh token` üretilir.
- `Access token` kısa ömürlüdür ve korumalı endpoint'lere erişimde kullanılır.
- `Refresh token` daha uzun ömürlüdür ve veritabanında saklanır.
- `Access token` süresi dolduğunda istemci `refresh token` ile yeni `access token` talep eder.
- `Refresh token` geçersiz, süresi dolmuş veya iptal edilmişse kullanıcı tekrar giriş yapmak zorundadır.

## Kullanılan Teknolojiler

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT
- H2 Database
- Thymeleaf
- JavaScript

## Projede Gerçekleştirilen Özellikler

- JWT tabanlı authentication yapısı
- Access token ve refresh token üretimi
- Refresh token'ın veritabanında tutulması
- Refresh token süresi kontrolü
- Token tipi ayrımı (`ACCESS` / `REFRESH`)
- Refresh token rotation
- Logout ile refresh token iptali
- DTO yapılarının ayrılması
- Global exception handling
- Thymeleaf ve JavaScript ile hazırlanmış demo arayüz

## Kullanıcı Bilgileri

Uygulamadaki örnek kullanıcı bilgileri:

- Kullanıcı adı: `mert`
- Şifre: `123`

## Veritabanı Bilgisi

Projede **H2 Database** kullanılmaktadır.

`Refresh token` kayıtları veritabanındaki `refresh_tokens` tablosunda tutulur.

H2 Console bilgileri:

- Adres: `http://localhost:8083/h2-console`
- JDBC URL: `jdbc:h2:file:C:/Users/DELL/Desktop/n11bootcamp/jwtornek/data/jwtornekdb`
- Kullanıcı adı: `sa`
- Şifre: boş

Örnek sorgu:

```sql
select * from refresh_tokens;
```

## Endpoint'ler

### 1. Login

```http
POST /auth/login
```

İstek gövdesi:

```json
{
  "username": "mert",
  "password": "123"
}
```

Başarılı cevap:

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "accessTokenExpiresAt": "...",
  "refreshToken": "...",
  "refreshTokenExpiresAt": "..."
}
```

### 2. Korumalı Mesaj Endpoint'i

```http
GET /message
```

Header:

```text
Authorization: Bearer accessToken
```

Başarılı cevap:

```text
Merhaba JWT
```

### 3. Refresh Token

```http
POST /auth/refresh
```

İstek gövdesi:

```json
{
  "refreshToken": "..."
}
```

Bu endpoint başarılı olduğunda yeni `access token` ve yeni `refresh token` döner.

### 4. Logout

```http
POST /auth/logout
```

İstek gövdesi:

```json
{
  "refreshToken": "..."
}
```

Bu işlem sonrası ilgili `refresh token` iptal edilir.

## Uygulamanın Çalıştırılması

Proje klasörüne girilip aşağıdaki komut çalıştırılır:

```powershell
cd C:\Users\DELL\Desktop\n11bootcamp\jwtornek
./mvnw spring-boot:run
```

## Uygulama Adresleri

- Demo arayüz: `http://localhost:8083/demo`
- H2 Console: `http://localhost:8083/h2-console`

## Demo Akışı

Projeyi gösterirken aşağıdaki sıra takip edilebilir:

1. Kullanıcı giriş yapar.
2. Sistem `access token` ve `refresh token` üretir.
3. `Access token` ile `/message` endpoint'ine başarılı istek atılır.
4. `Access token` süresi dolunca aynı istek başarısız olur.
5. `Refresh token` ile `/auth/refresh` endpoint'ine gidilir.
6. Yeni `access token` alınır.
7. Yeni `access token` ile `/message` endpoint'ine tekrar başarılı istek atılır.
8. Logout yapılır.
9. Logout sonrası aynı `refresh token` ile tekrar token yenileme denenir ve sistem isteği reddeder.

## Mimari Açıklama

Bu projede `access token` yapısı **stateless**, `refresh token` yapısı ise **stateful** olarak tasarlanmıştır.

Bunun nedeni şudur:

- `Access token` kısa ömürlüdür ve her istekte JWT doğrulaması ile kontrol edilir.
- `Refresh token` ise iptal edilebilir, süresi izlenebilir ve güvenli şekilde yönetilebilir olması için veritabanında tutulur.

Bu sayede:

- Logout işlemi yapılabilir
- Refresh token süresi kontrol edilebilir
- Eski refresh token'lar iptal edilebilir
- Token rotation uygulanabilir

## Frontend Hakkında

Projeye Thymeleaf ve JavaScript kullanılarak basit bir arayüz eklenmiştir.

Bu arayüz üzerinden:

- giriş yapılabilir,
- korumalı endpoint çağrılabilir,
- refresh işlemi tetiklenebilir,
- logout yapılabilir,
- token içerikleri ve sunucu cevapları görüntülenebilir.

## Sonuç

Bu proje ile temel JWT kullanımının ötesine geçilerek gerçek dünya uygulamalarında sık kullanılan **Refresh Token Mimarisi** uygulanmıştır.

Böylece daha güvenli, sürdürülebilir ve yönetilebilir bir authentication altyapısı oluşturulmuştur.
