## Overview

This project is built using Java and Spring Boot. 
It serves as the backend service layer and demonstrates RESTful API development.

## Google OAuth2 Deployment Notes

The backend uses Spring Security with Google OAuth2 login. Configure OAuth2 credentials with environment variables; never commit client secrets to git.

On EC2, export the credentials before starting the application:

```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
java -jar target/sms-backend-0.0.1-SNAPSHOT.jar
```

Set the PostgreSQL connection values as environment variables. The app supports both the Spring names and short aliases:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/postgresql-sms"
export SPRING_DATASOURCE_USERNAME="sms_user"
export SPRING_DATASOURCE_PASSWORD="1234"
```

or:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/postgresql-sms"
export DB_USERNAME="sms_user"
export DB_PASSWORD="1234"
```

If PostgreSQL reports `The server requested SCRAM-based authentication, but no password was provided`, the app did not receive `SPRING_DATASOURCE_PASSWORD` or `DB_PASSWORD`.

Or pass them inline:

```bash
GOOGLE_CLIENT_ID="your-google-client-id" \
GOOGLE_CLIENT_SECRET="your-google-client-secret" \
java -jar target/sms-backend-0.0.1-SNAPSHOT.jar
```

In Google Cloud Console, add the EC2 redirect URI:

```text
http://<EC2_PUBLIC_IP>:8080/login/oauth2/code/google
```

For a domain with HTTPS, add:

```text
https://<domain>/login/oauth2/code/google
```

Local development uses Spring Boot's default Google OAuth2 redirect path:

```text
http://localhost:8080/login/oauth2/code/google
```

## Verifying OAuth2 User Persistence

`/actuator/health` showing PostgreSQL `UP` only proves database connectivity. It does not prove that a Google OAuth2 user was mapped and persisted locally.

After a successful Google login, verify the local user mapping in the same PostgreSQL database configured by `SPRING_DATASOURCE_URL` or `spring.datasource.url`:

```sql
select current_database(), current_schema();
select * from app_users;
```

The `app_users` table appears after the `AppUser` entity exists and Hibernate schema update runs with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

For EC2/Docker, pass this explicitly if needed:

```bash
-e SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

## Kafka Demo

Start PostgreSQL, the Spring Boot app container, the three Kafka brokers, and Kafka UI:

```bash
docker compose up -d
```

For local app development against the Docker Kafka cluster, run:

```bash
./mvnw spring-boot:run
```

Kafka UI is available at:

```text
http://localhost:8090
```

Inspect the `sms-events` topic:

```bash
docker exec -it kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --describe --topic sms-events
```

Equivalent command inside a Kafka broker container:

```bash
kafka-topics.sh --describe --topic sms-events
```

Read events from the beginning:

```bash
docker exec -it kafka-1 /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka-1:19092 --topic sms-events --from-beginning
```

Equivalent command inside a Kafka broker container:

```bash
kafka-console-consumer.sh --topic sms-events --from-beginning
```

Check notification consumer lag:

```bash
docker exec -it kafka-1 /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka-1:19092 --describe --group sms-notification-service
```

Equivalent command inside a Kafka broker container:

```bash
kafka-consumer-groups.sh --describe --group sms-notification-service
```

Expected result: `sms-events` has 3 partitions and replication factor 3. Creating, updating, or deleting a student publishes an event after the database operation succeeds, the producer logs the Kafka partition and offset, `sms-notification-service` receives the event, and consumer lag settles at 0.
