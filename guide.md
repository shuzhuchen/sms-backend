# Name Aggregation API Demo Guide

This guide explains the production-style refactor for `POST /name/aggregation` and gives talking points for a demo. It only covers the name aggregation API, not the student management CRUD logic.

## API Contract

Endpoint:

```text
POST /name/aggregation
```

Request format:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon"]
}
```

Local aggregation adds this service's configured name, normally `Suzy`:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy"]
}
```

If downstream succeeds, the API returns the downstream response, for example:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy", "April", ...]
}
```

If downstream fails, times out, returns an error, or the circuit is open, the API returns the local fallback response:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy"]
}
```

## Configuration Accountability

`src/main/resources/application.properties` owns runtime values:

```properties
server.port=${SERVER_PORT:8080}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/postgresql-sms}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

downstream.url=${DOWNSTREAM_URL:http://18.236.231.101:8080/name/aggregation}
aggregation.service-name=${AGGREGATION_SERVICE_NAME:Suzy}
aggregation.retry.max-attempts=${AGGREGATION_RETRY_MAX_ATTEMPTS:3}
aggregation.downstream.timeout-ms=${AGGREGATION_DOWNSTREAM_TIMEOUT_MS:2000}
aggregation.circuit.failure-threshold=${AGGREGATION_CIRCUIT_FAILURE_THRESHOLD:3}
aggregation.circuit.open-duration-ms=${AGGREGATION_CIRCUIT_OPEN_DURATION_MS:30000}
```

Demo accountability:

- Downstream URL is not hardcoded in business logic; it comes from `DOWNSTREAM_URL`.
- This service's own name is configurable through `AGGREGATION_SERVICE_NAME`.
- Password remains environment-driven, not committed.
- Retry, timeout, and circuit breaker values are configurable. Retry and circuit breaker behavior are implemented with Resilience4j core APIs.

## Request DTO

Class:

```text
src/main/java/net/javaguides/sms_backend/dto/NameAggregationRequest.java
```

Purpose:

- Represents both request and response body.
- Uses `List<String>` so the API accepts JSON array format.

Key field:

```java
private List<String> name;
```

Demo accountability:

- Show that the API no longer expects `"Jessica, Jocelyn, Simon"` as one comma-separated string.
- It expects `"name": ["Jessica", "Jocelyn", "Simon"]`.

## Controller Layer

Class:

```text
src/main/java/net/javaguides/sms_backend/controller/NameAggregationController.java
```

Responsibility:

- Owns the REST endpoint path.
- Accepts request body.
- Delegates business logic to `NameAggregationService`.
- Depends on the service interface, not the implementation.

Important code behavior:

- `@PostMapping("/name/aggregation")` keeps the required endpoint path.
- `@RequestBody(required = false)` allows a missing body so the service can apply the default name.
- `@Qualifier("nameAggregationServiceImpl")` makes the selected implementation explicit.

Demo accountability:

- "The controller is intentionally thin. It does not append names, call downstream, handle retries, or persist failures. That all belongs in the service layer."

## Service Interface

Class:

```text
src/main/java/net/javaguides/sms_backend/service/NameAggregationService.java
```

Responsibility:

- Defines the public service contract:

```java
NameAggregationRequest aggregate(NameAggregationRequest request);
```

Demo accountability:

- Satisfies the instructor requirement for service interface plus implementation.
- Makes the controller depend on an abstraction.

## Service Implementation

Class:

```text
src/main/java/net/javaguides/sms_backend/service/impl/NameAggregationServiceImpl.java
```

Main function:

```java
aggregate(NameAggregationRequest request)
```

What it does:

1. Logs the incoming request.
2. Builds local response by appending the configured service name.
3. Checks the manual circuit breaker.
4. Calls downstream with retry and timeout.
5. Returns downstream response on success.
6. Returns local fallback response on downstream failure.
7. Persists failed downstream requests.

Supporting functions:

- `appendServiceName(...)`
  - Copies incoming names into a mutable list.
  - Uses `Suzy` when request body is null, name list is null, or name list is empty.
  - Avoids duplicating `Suzy` if already present.

- `callDownstreamWithResilience4j(...)`
  - Calls downstream through the async client.
  - Waits with timeout.
  - Uses Resilience4j Retry for a configured number of attempts.
  - Uses Resilience4j CircuitBreaker to stop downstream calls temporarily after repeated failures.
  - Wraps failures in `DownstreamServiceException`.

- `recordSuccess()`
  - Resets the circuit breaker failure count after downstream success.

- `recordFailure()`
  - Increments downstream failure count.
  - Opens circuit temporarily after the configured failure threshold.

- `persistFailedRequest(...)`
  - Saves failed downstream request details through `FailedAggregationRequestRepository`.

Demo accountability:

- Dynamic input: request names come from the caller.
- Default value: null or empty input becomes `["Suzy"]`.
- No duplicate: `["Jessica", "Suzy"]` stays `["Jessica", "Suzy"]`.
- Retry: downstream is attempted up to `AGGREGATION_RETRY_MAX_ATTEMPTS`.
- Timeout: each downstream call uses `AGGREGATION_DOWNSTREAM_TIMEOUT_MS`.
- Downgrade: downstream failure returns local response instead of HTTP 500.
- Circuit breaker: Resilience4j opens the circuit after repeated failures and skips downstream for a short window.

## Async Downstream Client

Interface:

```text
src/main/java/net/javaguides/sms_backend/service/DownstreamNameAggregationClient.java
```

Implementation:

```text
src/main/java/net/javaguides/sms_backend/service/impl/RestClientDownstreamNameAggregationClient.java
```

Responsibility:

- Sends the local aggregated request to the downstream service.
- Uses Spring `RestClient`.
- Returns `CompletableFuture<NameAggregationRequest>`.
- Runs through the custom executor:

```java
@Async("nameAggregationTaskExecutor")
```

Demo accountability:

- "The HTTP call is isolated from business logic. The aggregation service decides retry/fallback; the client only performs the downstream call."
- "The downstream URL is injected from configuration, not hardcoded in the method."

## Async Thread Pool

Class:

```text
src/main/java/net/javaguides/sms_backend/config/AsyncConfig.java
```

Bean:

```java
nameAggregationTaskExecutor
```

Settings:

- `corePoolSize = 2`
- `maxPoolSize = 4`
- `queueCapacity = 50`
- `threadNamePrefix = "name-aggregation-"`

Demo accountability:

- This avoids Spring's default async executor.
- The values are small and bounded for an EC2 t3.micro-style instance.

## Failed Request Persistence

Entity:

```text
src/main/java/net/javaguides/sms_backend/entity/FailedAggregationRequest.java
```

Status enum:

```text
src/main/java/net/javaguides/sms_backend/entity/FailedAggregationStatus.java
```

Repository:

```text
src/main/java/net/javaguides/sms_backend/repository/FailedAggregationRequestRepository.java
```

Persisted fields:

- `id`
- `requestPayload`
- `downstreamUrl`
- `errorMessage`
- `status`
- `createdAt`
- `updatedAt`

Statuses:

- `PENDING`
- `RETRIED`
- `FAILED`
- `RECOVERED`

Current behavior:

- When downstream fails, the local request payload is saved with status `PENDING`.
- Persistence is simple and direct through `FailedAggregationRequestRepository`.

Demo accountability:

- "I did not overbuild a scheduled recovery job. The requirement was to persist failed requests, so the minimum production-ready version stores enough information to retry later."
- "The table is `failed_aggregation_requests`."

## Exception Handling

Classes:

```text
src/main/java/net/javaguides/sms_backend/exception/DownstreamServiceException.java
src/main/java/net/javaguides/sms_backend/exception/NameAggregationException.java
src/main/java/net/javaguides/sms_backend/exception/GlobalExceptionHandler.java
```

Responsibility:

- `DownstreamServiceException`: wraps downstream call failures.
- `NameAggregationException`: wraps internal aggregation failures.
- `GlobalExceptionHandler`: returns clean JSON for unexpected errors.

Example error response:

```json
{
  "status": 500,
  "message": "Name aggregation failed",
  "timestamp": "2026-06-30T..."
}
```

Demo accountability:

- Downstream failures do not normally reach the global handler because fallback succeeds.
- The global handler is still present for true internal errors.
- Raw stack traces are not exposed to users.

## AOP Logging

Class already in project:

```text
src/main/java/net/javaguides/sms_backend/aspect/LoggingAspect.java
```

Responsibility:

- Logs service method entry, return, thrown exceptions, and timing.

Demo accountability:

- This satisfies the instructor's AOP requirement.
- The name aggregation service also adds specific logs for request, local aggregation, downstream call, success, failure, fallback, and persistence.

## Demo Scenarios

### 1. Normal Request

Command:

```bash
curl -X POST http://localhost:8080/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":["Jessica","Jocelyn","Simon"]}'
```

Expected if downstream is available:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy", "April", "Allen"]
}
```

Expected if downstream is unavailable:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy"]
}
```

### 2. Empty Body Uses Default

Command:

```bash
curl -X POST http://localhost:8080/name/aggregation \
  -H "Content-Type: application/json"
```

Fallback result:

```json
{
  "name": ["Suzy"]
}
```

### 3. Empty Name List Uses Default

Command:

```bash
curl -X POST http://localhost:8080/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":[]}'
```

Fallback result:

```json
{
  "name": ["Suzy"]
}
```

### 4. No Duplicate Service Name

Command:

```bash
curl -X POST http://localhost:8080/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":["Jessica","Suzy"]}'
```

Fallback result:

```json
{
  "name": ["Jessica", "Suzy"]
}
```

### 5. Failure Persistence Check

After forcing downstream failure, check PostgreSQL:

```sql
select id, request_payload, downstream_url, error_message, status, created_at
from failed_aggregation_requests
order by id desc;
```

Expected:

- A row is created.
- `status` is `PENDING`.
- `request_payload` contains the locally aggregated names.

## Tests

Focused test class:

```text
src/test/java/net/javaguides/sms_backend/service/impl/NameAggregationServiceImplTest.java
```

Covered cases:

- Appends `Suzy`.
- Does not duplicate `Suzy`.
- Null request uses default.
- Empty list uses default.
- Downstream success returns downstream response.
- Downstream failure returns fallback and records persistence call.

Run:

```bash
./mvnw -Dtest=NameAggregationServiceImplTest test
```

Build:

```bash
./mvnw clean package -DskipTests
```

Docker rebuild and push:

```bash
docker buildx build --platform linux/amd64 -t szchen/sms-backend --push .
```

## What To Say In The Demo

- "The endpoint path stayed the same: `POST /name/aggregation`."
- "The API contract uses a JSON array, not a comma-separated string."
- "The controller only handles HTTP. It delegates to `NameAggregationService`."
- "The service layer follows interface plus implementation."
- "My service name is configurable through `AGGREGATION_SERVICE_NAME`, defaulting to `Suzy`."
- "Downstream URL is configuration-driven through `DOWNSTREAM_URL`."
- "The downstream call is async and uses a bounded custom thread pool."
- "Retry and timeout are implemented so the app does not hang forever."
- "If downstream fails, the user still gets a valid local response."
- "Failed downstream calls are persisted for later recovery."
- "Global exception handling prevents raw stack traces from leaking to API callers."

## Jenkins Pipeline

File:

```text
Jenkinsfile
```

Pipeline behavior:

- Triggered by GitHub webhook through `githubPush()`.
- Intended for a Jenkins multibranch pipeline.
- Builds the jar with `./mvnw clean package -DskipTests`.
- Runs focused name aggregation service tests.
- Builds and pushes the Docker image only on the configured deploy branch, currently `dev`.
- Deploys to EC2 over SSH only on the deploy branch.

Required Jenkins credentials:

- `dockerhub-credentials`: Docker Hub username/token.
- `sms-backend-ec2-ssh-key`: SSH private key for EC2.
- `sms-backend-ec2-host`: EC2 host or public IP as secret text.
- `sms-backend-db-password`: PostgreSQL password as secret text.

Demo accountability:

- "A PR merge into `dev` creates a push event."
- "GitHub webhook triggers the Jenkins multibranch pipeline."
- "Jenkins builds, tests, pushes `szchen/sms-backend`, then restarts the EC2 container."
- "Secrets are read from Jenkins credentials, not committed to the repo."
