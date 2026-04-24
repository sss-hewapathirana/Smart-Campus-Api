# Smart Campus Sensor & Room Management API

A robust, scalable RESTful API built with **JAX-RS (Jersey)** for the university's Smart Campus initiative. This service manages campus-wide infrastructure including Rooms, Sensors (temperature, CO2, occupancy, lighting), and historical Sensor Readings through a clean, versioned REST interface.

---

## Table of Contents

- [API Design Overview](#api-design-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Build & Run Instructions](#build--run-instructions)
- [API Endpoints](#api-endpoints)
- [Sample curl Commands](#sample-curl-commands)
- [Error Handling Strategy](#error-handling-strategy)
- [Coursework Report](#coursework-report)
  - [Part 1: Service Architecture & Setup](#part-1-service-architecture--setup)
  - [Part 2: Room Management](#part-2-room-management)
  - [Part 3: Sensor Operations & Linking](#part-3-sensor-operations--linking)
  - [Part 4: Deep Nesting with Sub-Resources](#part-4-deep-nesting-with-sub-resources)
  - [Part 5: Advanced Error Handling, Exception Mapping & Logging](#part-5-advanced-error-handling-exception-mapping--logging)

---

## API Design Overview

The Smart Campus API follows a **resource-oriented architecture** with three primary entities:

| Resource | Description | Endpoint |
|---|---|---|
| **Room** | Physical campus rooms with capacity and sensor assignments | `/api/v1/rooms` |
| **Sensor** | IoT sensor devices (temperature, CO2, occupancy, etc.) linked to rooms | `/api/v1/sensors` |
| **SensorReading** | Historical time-series data recorded by individual sensors | `/api/v1/sensors/{id}/readings` |

**Key Design Principles:**

- **Versioned API** (`/api/v1`) for backward compatibility and future evolution
- **HATEOAS Discovery** endpoint at the root for API self-documentation
- **Sub-Resource Pattern** for sensor readings, maintaining a clean hierarchical URL structure
- **Referential Integrity** — sensors cannot reference non-existent rooms; rooms with active sensors cannot be deleted
- **Structured Error Responses** — all errors return consistent JSON with `error`, `status`, `message`, and `details` fields
- **Cross-Cutting Logging** via JAX-RS filters for full request/response observability
- **Thread-Safe In-Memory Storage** using `ConcurrentHashMap` for safe concurrent access

---

## Technology Stack

| Component | Technology | Version |
|---|---|---|
| API Framework | JAX-RS (Jakarta RESTful Web Services) | 3.1.0 |
| Implementation | Eclipse Jersey | 3.1.3 |
| JSON Serialisation | Jackson (via jersey-media-json-jackson) | 3.1.3 |
| DI Container | HK2 (via jersey-hk2) | 3.1.3 |
| Servlet Container | Eclipse Jetty (Maven plugin) | 11.0.15 |
| Build Tool | Apache Maven | 3.9+ |
| Java Version | JDK 11+ | 11 |
| Testing | JUnit Jupiter | 5.10.0 |

---

## Project Structure

```
smart-campus-api/
├── pom.xml                                          # Maven build configuration
├── README.md                                        # This file
└── src/
    ├── main/
    │   ├── java/com/smartcampus/
    │   │   ├── SmartCampusApplication.java           # JAX-RS Application entry point
    │   │   ├── model/
    │   │   │   ├── Room.java                         # Room POJO
    │   │   │   ├── Sensor.java                       # Sensor POJO
    │   │   │   └── SensorReading.java                # SensorReading POJO
    │   │   ├── resource/
    │   │   │   ├── DiscoveryResource.java            # GET /api/v1 (root discovery)
    │   │   │   ├── RoomResource.java                 # /api/v1/rooms CRUD
    │   │   │   ├── SensorResource.java               # /api/v1/sensors CRUD + filtering
    │   │   │   └── SensorReadingResource.java        # Sub-resource for readings
    │   │   ├── exception/
    │   │   │   ├── RoomNotEmptyException.java        # 409 Conflict
    │   │   │   ├── LinkedResourceNotFoundException.java  # 422 Unprocessable Entity
    │   │   │   ├── SensorUnavailableException.java   # 403 Forbidden
    │   │   │   └── mapper/
    │   │   │       ├── RoomNotEmptyExceptionMapper.java
    │   │   │       ├── LinkedResourceNotFoundExceptionMapper.java
    │   │   │       ├── SensorUnavailableExceptionMapper.java
    │   │   │       └── GlobalExceptionMapper.java    # Catch-all safety net (500)
    │   │   ├── filter/
    │   │   │   └── ApiLoggingFilter.java             # Request/Response logging
    │   │   └── store/
    │   │       └── DataStore.java                    # Thread-safe singleton data store
    │   └── webapp/WEB-INF/
    │       └── web.xml                               # Servlet deployment descriptor
    └── test/java/com/smartcampus/
        └── DataStoreTest.java                        # Unit tests
```

---

## Build & Run Instructions

### Prerequisites

- **Java JDK 11** or higher installed and configured in `PATH`
- **Apache Maven 3.9+** installed and configured in `PATH`

### Step 1: Clone the Repository

```bash
git clone https://github.com/sss-hewapathirana/Smart-Campus-Api.git
cd smart-campus-api
```

### Step 2: Build the Project

```bash
mvn clean install
```

This command will:
- Compile all 17 Java source files
- Run the unit tests (JUnit 5)
- Package the application as `target/smart-campus-api.war`
- Install the artifact to your local Maven repository

### Step 3: Start the Server

```bash
mvn jetty:run
```

The embedded Jetty server will start on **port 8080**. You will see:

```
[INFO] Started ServerConnector@...{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
[INFO] Started Server@...{STARTING}[11.0.15,sto=0]
```

### Step 4: Verify

Open a browser or use curl to access the discovery endpoint:

```
http://localhost:8080/api/v1
```

### Stopping the Server

Press `Ctrl+C` in the terminal to stop Jetty.

---

## API Endpoints

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| `GET` | `/api/v1` | API discovery & HATEOAS links | 200 OK |
| `GET` | `/api/v1/rooms` | List all rooms | 200 OK |
| `POST` | `/api/v1/rooms` | Create a new room | 201 Created |
| `GET` | `/api/v1/rooms/{roomId}` | Get room by ID | 200 OK |
| `DELETE` | `/api/v1/rooms/{roomId}` | Delete a room (if no sensors attached) | 204 No Content |
| `GET` | `/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 OK |
| `POST` | `/api/v1/sensors` | Register a new sensor (roomId must exist) | 201 Created |
| `GET` | `/api/v1/sensors/{sensorId}` | Get sensor by ID | 200 OK |
| `GET` | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor | 200 OK |
| `POST` | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (sensor must not be in MAINTENANCE) | 201 Created |

---

## Sample curl Commands

### 1. Discovery Endpoint

```bash
curl -s http://localhost:8080/api/v1
```

**Response (200 OK):**

```json
{
  "name": "Smart Campus API",
  "version": "1.0.0",
  "status": "operational",
  "contact": "backend-team@university.edu",
  "_links": {
    "self": "/api/v1",
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  },
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### 2. Create a Room

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}' \
  http://localhost:8080/api/v1/rooms
```

**Response (201 Created):**

```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

### 3. Register a Sensor

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}' \
  http://localhost:8080/api/v1/sensors
```

**Response (201 Created):**

```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "LIB-301"
}
```

### 4. Filter Sensors by Type

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**Response (200 OK):**

```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  }
]
```

### 5. Add a Sensor Reading

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"value":23.7}' \
  http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

**Response (201 Created):**

```json
{
  "id": "a3f1c2d4-...",
  "timestamp": 1714070400000,
  "value": 23.7
}
```

### 6. Get Sensor Readings History

```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 7. Attempt to Delete a Room with Sensors (409 Conflict)

```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Response (409 Conflict):**

```json
{
  "error": "Conflict",
  "status": 409,
  "message": "Room LIB-301 cannot be deleted. It still contains active sensors.",
  "details": "The room contains active hardware and cannot be decommissioned."
}
```

### 8. Create Sensor with Invalid Room ID (422 Unprocessable Entity)

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"NONEXISTENT"}' \
  http://localhost:8080/api/v1/sensors
```

**Response (422 Unprocessable Entity):**

```json
{
  "error": "Unprocessable Entity",
  "status": 422,
  "message": "Room with ID 'NONEXISTENT' does not exist.",
  "details": "The request body contains a reference to a non-existent resource."
}
```

---

## Error Handling Strategy

The API employs a **layered exception-handling architecture** where every error returns structured JSON — never raw stack traces.

| HTTP Code | Exception | Trigger Scenario |
|---|---|---|
| **409** Conflict | `RoomNotEmptyException` | Deleting a room that still has sensors |
| **422** Unprocessable Entity | `LinkedResourceNotFoundException` | Creating a sensor with a non-existent `roomId` |
| **403** Forbidden | `SensorUnavailableException` | Posting a reading to a sensor in `MAINTENANCE` status |
| **500** Internal Server Error | `GlobalExceptionMapper` (catch-all) | Any unhandled runtime exception |

All error responses follow a consistent JSON structure:

```json
{
  "error": "<HTTP Status Phrase>",
  "status": <HTTP Code>,
  "message": "<Specific error message>",
  "details": "<Additional context>"
}
```

---

## Coursework Report

### Part 1: Service Architecture & Setup

#### Question 1.1: JAX-RS Resource Class Lifecycle

By default, JAX-RS Resource classes follow a **per-request lifecycle**. This means that the JAX-RS runtime (Jersey in our case) creates a **new instance** of the Resource class for every incoming HTTP request. Once the request is processed and the response is sent, that instance is eligible for garbage collection and is discarded.

This architectural decision has a profound impact on how we manage in-memory data. Because each request handler is a fresh object, **instance fields on a Resource class are not shared** between requests. If we were to store our rooms or sensors as instance variables within `RoomResource`, the data would be lost after every single request — each new instance would start with an empty data set.

To solve this, we employ the **Singleton Data Store pattern**. Our `DataStore` class uses a private constructor and a static `INSTANCE` field to guarantee that exactly one instance exists across the entire application lifetime:

```java
public class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    // ...
    public static DataStore getInstance() { return INSTANCE; }
}
```

We use `ConcurrentHashMap` rather than `HashMap` because the per-request lifecycle means that multiple threads (handling concurrent requests) will be accessing the same shared data simultaneously. `ConcurrentHashMap` provides thread-safe operations without requiring explicit synchronisation blocks, preventing **race conditions** such as two simultaneous writes corrupting the data structure or a read occurring mid-write and returning inconsistent data. This ensures that even under heavy concurrent load, the data store remains consistent and no data is lost.

#### Question 1.2: HATEOAS and Hypermedia Benefits

HATEOAS (Hypermedia as the Engine of Application State) is the principle that API responses should include **navigational links** that tell the client what actions are available and where related resources can be found. It is considered a hallmark of mature RESTful design because it enables APIs to be **self-descriptive and discoverable**.

Our Discovery endpoint at `GET /api/v1` returns a `_links` object containing URLs for rooms and sensors:

```json
{
  "_links": {
    "self": "/api/v1",
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

The key benefits over static documentation include:

1. **Decoupled Clients**: Client developers do not need to hard-code endpoint URLs. They can follow links dynamically, which means the server can evolve its URL structure without breaking existing clients — the links simply update.

2. **Self-Documentation**: A new developer can explore the API starting from the root endpoint alone, following links to discover all available resources, without needing external documentation.

3. **State-Driven Navigation**: In more advanced implementations, HATEOAS links can change based on the current state of a resource (e.g., a room with sensors might include a link to its sensor list, while an empty room might include a "delete" action link), guiding the client toward valid operations.

4. **Reduced Coupling to Documentation**: Static documentation (e.g., Swagger/OpenAPI) can become stale if not maintained. Hypermedia links embedded in responses are always current because they are generated by the live service.

---

### Part 2: Room Management

#### Question 2.1: Returning IDs vs Full Objects

When returning a list of rooms from `GET /api/v1/rooms`, there is an important design trade-off between returning only resource IDs versus returning the full room objects:

**Returning Only IDs:**
- **Advantages**: Extremely lightweight responses. A list of 1,000 room IDs would be a few kilobytes, making it fast to transmit over the network. This is beneficial for low-bandwidth environments or mobile clients.
- **Disadvantages**: Forces the client to make **N additional HTTP requests** — one `GET /api/v1/rooms/{id}` call per room — to retrieve the actual data. This is known as the **N+1 problem** and creates significant overhead in terms of latency (many round-trips) and server load (many requests).

**Returning Full Objects (our approach):**
- **Advantages**: The client receives all the data it needs in a **single request**. This eliminates the N+1 problem, reduces total latency, and simplifies the client-side code (no need for batch-fetching logic).
- **Disadvantages**: Larger payload size. If rooms have many fields or very large nested data, this can consume more bandwidth.

In our implementation, we return full Room objects because our data model is relatively compact (id, name, capacity, sensorIds), and the convenience of having all data in one response far outweighs the marginal bandwidth cost. For very large datasets, pagination (e.g., `?page=1&size=20`) would be the recommended approach to balance both concerns.

#### Question 2.2: DELETE Idempotency

The HTTP specification states that `DELETE` **should be idempotent**, meaning that sending the same DELETE request multiple times should produce the same observable outcome as sending it once.

In our implementation, DELETE is **effectively idempotent**. Here is what happens if a client sends `DELETE /api/v1/rooms/LIB-301` multiple times:

1. **First call**: The room exists and has no sensors. It is deleted from the data store. The server returns **204 No Content** — the room is gone.

2. **Second call (and all subsequent calls)**: The room no longer exists in the data store. Our code checks `store.getRoom(roomId)` which returns `null`. The server returns **404 Not Found**.

The key insight is that idempotency does not require identical response codes — it requires that the **server-side state** is the same after each call. After the first DELETE, the room is gone. After the second, third, and hundredth DELETE, the room is still gone. The server state has not changed. The 404 response simply informs the client that the resource is already absent, which is the desired end-state of a DELETE operation.

If the room has active sensors, every call will consistently return **409 Conflict** because the precondition (room must be empty) is never satisfied — this is also idempotent behaviour, as the state never changes.

---

### Part 3: Sensor Operations & Linking

#### Question 3.1: @Consumes Annotation and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on our POST methods explicitly declares that the endpoint **only accepts** request bodies formatted as JSON. This triggers JAX-RS's built-in **content negotiation** mechanism.

If a client sends a request with a different `Content-Type` header (e.g., `text/plain` or `application/xml`), the following happens:

1. **JAX-RS intercepts the request** before it even reaches our method code.
2. The runtime checks the `Content-Type` header against the `@Consumes` annotation.
3. Finding no match, JAX-RS automatically returns an **HTTP 415 Unsupported Media Type** response.
4. Our resource method is **never invoked**.

This is a powerful defensive mechanism because:

- It prevents malformed or incompatible data from reaching our business logic.
- It eliminates the need for manual content-type checking inside every endpoint.
- It provides clear, standards-compliant feedback to the client about what formats are accepted.
- Without this annotation, JAX-RS might attempt to deserialise the body using any available `MessageBodyReader`, potentially causing unpredictable errors or silent data corruption.

The annotation effectively acts as a **contract** between the client and server, ensuring both parties agree on the data exchange format before any processing occurs.

#### Question 3.2: @QueryParam vs Path-Based Filtering

We implemented sensor filtering using `@QueryParam("type")` which produces URLs like:

```
GET /api/v1/sensors?type=Temperature
```

An alternative would be embedding the type in the URL path:

```
GET /api/v1/sensors/type/Temperature
```

The query parameter approach is generally superior for filtering and searching for several reasons:

1. **Semantic Correctness**: In REST, each path segment represents a **resource or resource hierarchy**. `/sensors/type/Temperature` implies that `type` is a sub-resource of sensors, and `Temperature` is a sub-resource of `type` — which is semantically incorrect. The `type` is a **property filter**, not a resource.

2. **Optional Parameters**: Query parameters are inherently optional. `GET /api/v1/sensors` returns all sensors; adding `?type=CO2` narrows the result. With path-based design, you would need separate route definitions for filtered vs. unfiltered access, creating code duplication.

3. **Composability**: Query parameters can be easily combined: `?type=CO2&status=ACTIVE&roomId=LIB-301`. Path-based filtering becomes unwieldy with multiple filters: `/sensors/type/CO2/status/ACTIVE/room/LIB-301`.

4. **Caching**: Proxies and CDNs understand that query parameters modify a base resource. Path-based filtering creates entirely different cache entries for what is conceptually the same resource collection.

5. **Convention**: Industry standards (REST best practices, JSON:API specification, OData) universally use query parameters for filtering, sorting, and pagination. Deviating from this convention increases the learning curve for API consumers.

---

### Part 4: Deep Nesting with Sub-Resources

#### Question 4.1: Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern is used in our `SensorResource` class where the method `getReadingResource()` returns an instance of `SensorReadingResource`:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    if (!store.sensorExists(sensorId)) {
        throw new NotFoundException("Sensor " + sensorId + " not found");
    }
    return new SensorReadingResource(sensorId);
}
```

The architectural benefits of this pattern include:

1. **Separation of Concerns**: Each resource class is responsible for a single entity. `SensorResource` handles sensor CRUD; `SensorReadingResource` handles readings. This follows the **Single Responsibility Principle**, making each class focused, readable, and testable in isolation.

2. **Reduced Class Complexity**: Without sub-resource locators, a single `SensorResource` class would need to handle `GET /sensors`, `POST /sensors`, `GET /sensors/{id}`, `GET /sensors/{id}/readings`, and `POST /sensors/{id}/readings` — all in one file. As the API grows (e.g., adding `/sensors/{id}/calibrations`, `/sensors/{id}/alerts`), this monolithic class would become unmanageable.

3. **Encapsulated Context**: The sub-resource locator passes the `sensorId` context to `SensorReadingResource` via its constructor. The reading resource then operates exclusively within that sensor's scope, without needing to re-validate or re-extract the sensor ID.

4. **Centralised Validation**: The locator method validates that the sensor exists **before** delegation. This means `SensorReadingResource` does not need to re-check sensor existence — it can trust that its context is valid. This eliminates duplicated validation logic.

5. **Reusability**: `SensorReadingResource` could theoretically be reused in different contexts (e.g., a batch processing endpoint) by simply instantiating it with a different sensor ID.

6. **Team Scalability**: In a real-world team, different developers can work on `SensorResource` and `SensorReadingResource` independently without merge conflicts, improving development velocity.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### Question 5.2: Why HTTP 422 over 404 for Missing References

When a client sends a `POST /api/v1/sensors` with a `roomId` that does not exist, we return **422 Unprocessable Entity** rather than **404 Not Found**. This choice is deliberate and semantically important:

**Why NOT 404:**
- HTTP 404 means "the resource you are trying to access does not exist." It refers to the **target URL** of the request.
- In our case, `POST /api/v1/sensors` — the target URL — absolutely exists and is a valid endpoint.
- Returning 404 would mislead the client into thinking the `/sensors` endpoint itself is missing, which is incorrect.

**Why 422:**
- HTTP 422 means "the server understands the request, the syntax is valid JSON, but the **content is semantically invalid**."
- The client sent well-formed JSON with a proper structure, but the `roomId` field references a resource that does not exist in the system.
- This is a **validation error at the business logic level**, not a routing error.
- 422 precisely communicates: "I received and parsed your JSON correctly, but I cannot process it because the data within it is logically invalid."

This distinction is critical for client-side error handling. A client receiving 404 might retry the same URL or report a connectivity issue. A client receiving 422 knows to inspect its **request body** for invalid references — leading to faster debugging and resolution.

#### Question 5.4: Cybersecurity Risks of Exposing Stack Traces

Exposing internal Java stack traces to external API consumers is a serious security vulnerability. An attacker can extract the following information from a raw stack trace:

1. **Technology Stack**: Package names like `org.glassfish.jersey`, `com.smartcampus` reveal the exact framework (Jersey), language (Java), and application namespace. Attackers can then target known vulnerabilities specific to those technologies and versions.

2. **Internal Architecture**: Stack traces expose the full call chain, revealing class names (`DataStore`, `SensorResource`), method names, and the application's internal package structure. This is a blueprint of the system's architecture.

3. **File Paths and Line Numbers**: Traces include paths like `SensorResource.java:73`, revealing the exact source code structure on the server and potentially the operating system (Windows vs. Linux paths).

4. **Library Versions**: Dependency stack frames can reveal exact versions of third-party libraries, allowing attackers to cross-reference against CVE (Common Vulnerabilities and Exposures) databases for known exploits.

5. **Database or Data Layer Information**: If an exception originates from a data access layer, the trace might expose table names, query structures, or connection details.

6. **Business Logic Insights**: Exception messages and method names can reveal business rules, validation logic, and internal workflows that could be exploited for privilege escalation or data manipulation.

Our `GlobalExceptionMapper` acts as a **security boundary** — it intercepts all unhandled exceptions, logs the full stack trace server-side only (for debugging), and returns a generic, safe message to the client:

```json
{
  "error": "Internal Server Error",
  "status": 500,
  "message": "An unexpected error occurred. Please contact support."
}
```

This ensures that no internal implementation details ever leak to external consumers, following the principle of **least information disclosure**.

#### Question 5.5: JAX-RS Filters vs Manual Logging

We implement API observability using `ApiLoggingFilter`, which implements both `ContainerRequestFilter` and `ContainerResponseFilter`. This approach is vastly superior to manually inserting `Logger.info()` calls inside every resource method for several reasons:

1. **Single Point of Implementation**: The filter is written once and automatically applies to **every** request and response across the entire API. With manual logging, you would need to add identical logging statements to every single method in every resource class — duplicating code across potentially dozens of endpoints.

2. **Guaranteed Coverage**: Filters execute for all requests, including those that result in JAX-RS-level errors (e.g., 404 for missing routes, 415 for unsupported media types) before they ever reach a resource method. Manual logging would miss these cases entirely, creating blind spots in observability.

3. **Separation of Concerns**: Logging is a **cross-cutting concern** — it is not part of the core business logic of managing rooms or sensors. By extracting it into a filter, our resource methods remain clean and focused on their primary responsibility. This follows the Aspect-Oriented Programming principle.

4. **Consistency**: A filter guarantees that every log entry follows the exact same format. Manual logging across different developers and methods would inevitably result in inconsistent formats, missing fields, or forgotten log statements.

5. **Maintainability**: If the logging format needs to change (e.g., adding a correlation ID or timestamp), you modify **one file** rather than hunting down logging statements scattered across the entire codebase.

6. **Easy Enable/Disable**: Removing or disabling logging is as simple as unregistering the filter class from `SmartCampusApplication.getClasses()`. With manual logging, you would need to locate and remove every individual log statement.

---

## License

This project is developed as coursework for the Client-Server Architectures module.
