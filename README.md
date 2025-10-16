# Bank Cards Management System

Полнофункциональное REST API приложение для управления банковскими картами, пользователями и переводами с использованием Spring Boot.

## Технологии

- **Java 17**
- **Spring Boot 3.2.0**
    - Spring Web
    - Spring Security
    - Spring Data JPA
- **PostgreSQL** - основная база данных
- **Liquibase** - миграции БД
- **JWT** - аутентификация
- **Swagger/OpenAPI** - документация API
- **Docker & Docker Compose** - контейнеризация
- **JUnit 5 & Mockito** - тестирование
- **Lombok** - уменьшение boilerplate кода
- **Jasypt** - шифрование данных

## Требования

- Java 17 или выше
- Maven 3.6+
- Docker и Docker Compose (опционально)
- PostgreSQL 15+ (если запуск без Docker)

## Установка и запуск

### Запуск (Docker)

```bash
git clone https://github.com/gKonstantin17/bankcards.git
cd bankcards

docker-compose up -d

# Запустить PostgreSQL
docker run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=bankcards_db -p 5432:5432 -d postgres:15
```
### Запуск (Maven)
Создать бд bankcards_db
```bash
git clone https://github.com/gKonstantin17/bankcards.git
cd bankcards
# Собрать приложение
./mvnw clean package -DskipTests
# Запустить приложение
java -jar target/bankcards-1.0.0.jar
```
Swagger UI
http://localhost:8080/swagger-ui.html

OpenAPI спецификация
http://localhost:8080/v3/api-docs

Основные эндпоинты
Аутентификация
```bash
POST /api/auth/register - Регистрация нового пользователя
POST /api/auth/login    - Вход в систему
```

Управление картами
```bash
GET    /api/cards              - Получить все карты (ADMIN)
POST   /api/cards              - Создать новую карту (ADMIN)
GET    /api/cards/{id}         - Получить карту по ID
GET    /api/cards/my-cards     - Получить свои карты (USER)
PUT    /api/cards/{id}/block   - Заблокировать карту (ADMIN)
PUT    /api/cards/{id}/unblock - Разблокировать карту (ADMIN)
DELETE /api/cards/{id}         - Удалить карту (ADMIN)
POST   /api/cards/{id}/request-block - Запросить блокировку (USER)
GET    /api/cards/{id}/balance - Получить баланс карты (USER)
```
Переводы
```bash
POST /api/transfers                 - Перевод между своими картами
GET  /api/transfers/my-transactions - История транзакций
GET  /api/transfers/card/{cardId}   - Транзакции по карте
```

Управление пользователями (ADMIN)
```bash
GET    /api/users           - Получить всех пользователей
GET    /api/users/{id}      - Получить пользователя
PUT    /api/users/{id}      - Обновить пользователя
DELETE /api/users/{id}      - Удалить пользователя
PUT    /api/users/{id}/roles/{roleName} - Назначить роль
DELETE /api/users/{id}/roles/{roleName} - Убрать роль
```

