# EC2 Docker Deployment

These notes deploy `sms-backend` as a Docker container on AWS EC2.

## Build the jar

The Dockerfile copies `target/*.jar`, so build the jar before building the Docker image:

```bash
./mvnw clean package -DskipTests
```

Generated jar:

```text
target/sms-backend-0.0.1-SNAPSHOT.jar
```

## Build and push the Docker image

Build for EC2 Linux amd64 and push to Docker Hub:

```bash
docker buildx build --platform linux/amd64 -t szchen/sms-backend --push .
```

## Run on EC2

This command uses host networking so the container can reach PostgreSQL on the EC2 host at `localhost:5432`.

```bash
docker stop sms-backend || true
docker rm sms-backend || true

docker run -d \
  --name sms-backend \
  --network host \
  -e SERVER_PORT=8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgresql-sms \
  -e SPRING_DATASOURCE_USERNAME=sms_user \
  -e SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD \
  -e DOWNSTREAM_URL=http://18.236.231.101:8080/name/aggregation \
  -e AGGREGATION_SERVICE_NAME=Suzy \
  szchen/sms-backend
```

Optional resilience tuning environment variables:

```text
AGGREGATION_RETRY_MAX_ATTEMPTS=3
AGGREGATION_DOWNSTREAM_TIMEOUT_MS=2000
AGGREGATION_CIRCUIT_FAILURE_THRESHOLD=3
AGGREGATION_CIRCUIT_OPEN_DURATION_MS=30000
```

## Logs

```bash
docker logs -f sms-backend
```

## Test name aggregation

Request:

```bash
curl -X POST http://34.229.77.238:8080/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":["Jessica","Jocelyn","Simon"]}'
```

Expected output if downstream is available:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy", "April", "Allen"]
}
```

Expected fallback output if downstream is unavailable:

```json
{
  "name": ["Jessica", "Jocelyn", "Simon", "Suzy"]
}
```

If the request body is missing, null, or contains an empty `name` list, the service uses `AGGREGATION_SERVICE_NAME`, which should be set to `Suzy`.
