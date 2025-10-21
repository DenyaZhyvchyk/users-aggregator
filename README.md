# User Aggregation Service

This Spring Boot service aggregates and returns a combined list of users from multiple data sources (different databases).
It can connect to several relational databases (e.g., PostgreSQL, MySQL, etc.) and merge user data into a single response.

## Features

* Fetches user data from multiple configured databases

* Supports different SQL dialects and schemas

* Aggregates results into a unified list

* Easily extendable for new database sources

* Supports parallel fetching for improved performance

## Configuration

Before running the application, you need to define your data sources in:

```path
src/main/resources/datasources.yml
```


Example configuration:

```yaml
data-sources:
- name: postgres-1
  strategy: postgres
  url: jdbc:postgresql://localhost:5432/appdb
  user: user
  password: password
  table: users
  mapping:
    id: user_id
    username: login
    name: first_name
    surname: last_name

- name: mysql-1
  strategy: mysql
  url: jdbc:mysql://localhost:3306/appdb
  user: user
  password: password
  table: user
  mapping:
    id: ldap_login
    username: ldap_login
    name: name
    surname: surname
```


Each entry defines a single data source with mapping between standard user fields and actual database columns.

## Using Docker Compose

By default, the repository includes a docker-compose.yml file that starts two database containers (PostgreSQL and MySQL).

You can start them using:

```console
docker-compose up -d
```


Note:
These databases are initially empty. You can populate them manually or via integration tests that create sample data.

## Running the Application

To start the service locally:

```console
./mvnw spring-boot:run
```


Or package and run the JAR:

```console
./mvnw clean package
java -jar target/user-aggregation-service.jar
```

## Running Integration Tests

Integration tests use Testcontainers to spin up temporary PostgreSQL and MySQL databases.

Run tests with:

```console
./mvnw test
```

## Example Request

GET /users

Response:

```json
[
  {"id": "1", "username": "jdoe", "name": "John", "surname": "Doe"},
  {"id": "2", "username": "asmith", "name": "Alice", "surname": "Smith"},
  {"id": "jdoe", "username": "jdoe", "name": "John", "surname": "Doe"},
  {"id": "asmith", "username": "asmith", "name": "Alice", "surname": "Smith"}
]
```
