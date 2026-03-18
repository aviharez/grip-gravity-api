# Grip & Gravity, Bouldering Gym Route Management API

A Spring Boot REST API for managing bouldering routes at the **Grip & Gravity** gym.
Features full route lifecycle management, community grade voting, and wall freshness analytics.

## Prerequisites
- **Java 17** (JDK 17+) - verify with `java -version`
- IntelliJ IDEA 

---

## Setup & Run

### Clone / open the project
```bash
git clone https://github.com/aviharez/grip-gravity-api.git
cd grip-gravity-api
```

### IntelliJ IDEA

1. Open the project root folder (`grip-gravity-api`) as a Maven project.
2. IntelliJ will auto-import dependencies.
3. Run `GripGravityApplication.java` via the green play button.

---

## H2 Console

Access the embedded H2 database console at:
```
http://localhost:8080/h2-console
```

| Field   | Value   |
|---------|---------|
| JDBC URL | `jdbc:h2:file:./data/gripgravity` |
| Username | `sa` |
| Password | *(leave blank)* |

---

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI JSON spec:
```
http://localhost:8080/api-docs
```

---

## API Endpoints

### Routes - `/api/routes`

| Method | Path | Description | Request Body | Success Response |
|--------|------|-------------|--------------|------------------|
| `GET`  | `/api/routes` | List all routes (optional `?status=ACTIVE`) | - | `200 List<RouteResponse> |
| `POST` | `/api/routes` | Create a new route (starts as DRAFT) | `RouteRequest` | `201 RouteResponse` |
| `GET`  | `/api/routes/{id}` | Get route by ID | - | `200 RouteResponse` |
| `PUT`  | `/api/routes/{id}` | Update a DRAFT or ACTIVE route | `RouteRequest` | `200 RouteResponse` |
| `DELETE`| `/api/routes/{id}`| Delete a DRAFT route | - | `204 No Content` |
| `POST` | `/api/routes/{id}/activate` | DRAFT -> ACTIVE | - | `200 RouteResponse` |
| `POST` | `/api/routes/{id}/maintenance` | ACTIVE -> MAINTENANCE | - | `200 RouteResponse` |
| `POST` | `/api/routes/{id}/restore` | MAINTENANCE -> ACTIVE | - | `200 RouteResponse` |
| `POST` | `/api/routes/{id}/retire` | ACTIVE / MAINTENANCE -> RETIRED | - | `200 RouteResponse` |
| `POST` | `/api/routes/{id}/reset` | RETIRED -> new DRAFT copy | - `201 RouteResponse` |
| `POST` | `/api/routes/{id}/grades` | Submit a community grade | `GradeSubmissionRequest` | `201 GradeSubmissionResponse` |
| `GET`  | `/api/routes/{id}/grades` | List all grade submissions | - | `200 List<GradeSubmissionResponse>` |

### Walls - `/api/walls`

| Method | Path | Description | Success Response |
|--------|------|-------------|------------------|
| `GET`  | `/api/walls/freshness` | Freshness scores for all sections | `200 List<FreshnessResponse>` |
| `GET`  | `/api/walls/{section}/freshness` | Freshness score for one section | `200 FreshnessResponse` |

---