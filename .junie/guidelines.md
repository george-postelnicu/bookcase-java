bookcase-java: Project-specific Development Guidelines

Audience: Experienced Java/Spring developers contributing to this codebase.

1. Build and Runtime Configuration
- Toolchain
  - Java 21 (Temurin recommended). Preview features are enabled for both compilation and tests.
  - Maven Wrapper is provided. Use ./mvnw to ensure plugin versions match the repo.
  - Core libs: Spring Boot 3.2.x, Spring Data JPA, Flyway, MapStruct.
- Maven specifics (pom.xml)
  - maven-compiler-plugin
    - source/target: 21
    - enablePreview: true
    - MapStruct annotation processor configured; make sure your IDE recognizes it for generated mappers (target/generated-sources/annotations).
  - maven-surefire-plugin
    - version 3.2.5
    - argLine: --enable-preview (tests also use Java preview features).
- Build
  - Full build with tests: ./mvnw clean verify
  - Skip tests (not recommended unless needed): ./mvnw clean package -DskipTests
  - Artifact: target/bookcase-java-0.0.1-SNAPSHOT.jar
- Running the application
  - Docker (recommended): docker compose -f docker/docker-compose.yml up
    - This builds the image from target/*.jar (ensure you ran a package step beforehand), and brings up MySQL + phpMyAdmin.
    - Ports
      - App: 9000 -> 8080 (application available on http://localhost:9000)
      - phpMyAdmin: http://localhost:9001 (host=db, user=root, password=ThisIsClassified or user=spring / ThePassword against the library DB).
    - MySQL service config (from compose)
      - DB: library
      - User: spring / ThePassword
      - Root: ThisIsClassified
      - JDBC URL (used by app container): jdbc:mysql://spring:ThePassword@db:3306/library?allowPublicKeyRetrieval=True&useSSL=false
  - Local host run (without Docker)
    - You must provide DB_JDBC_URL as an environment variable or JVM system property; the app reads spring.datasource.url from ${DB_JDBC_URL}.
      Example (MySQL running locally on 3306):
      export DB_JDBC_URL="jdbc:mysql://spring:ThePassword@localhost:3306/library?allowPublicKeyRetrieval=True&useSSL=false"
      ./mvnw spring-boot:run

2. Testing: Configuration, Running, and Adding Tests
- Test infrastructure
  - JUnit 5 via spring-boot-starter-test.
  - In-memory H2 database for tests (scope test) configured to emulate MySQL:
    - spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;
    - spring.jpa.hibernate.ddl-auto=none (schema is managed by Flyway)
    - spring.flyway.locations=classpath:flyway/mysql (use same migrations as prod)
  - Flyway runs against H2 for tests, so migrations in src/main/resources/flyway/mysql must be MySQL-compatible or H2-compatible under MODE=MYSQL. Keep an eye on MySQL-specific features that H2 may not fully support.
  - AbstractIntegrationTest
    - @SpringBootTest bootstraps the full context.
    - @Sql executes src/test/resources/sql/clean-all-data.sql BEFORE each test to ensure DB cleanliness.
    - @Import(ObjectMapperConfiguration.class) wires a consistent Jackson ObjectMapper for tests.
- Running tests
  - All tests: ./mvnw test
  - Single class: ./mvnw -Dtest=BookControllerTest test
  - Single method: ./mvnw -Dtest=BookControllerTest#create_isSuccessful test
  - Verify with integration-style tests included: ./mvnw verify
- Writing tests
  - Controller/service integration tests
    - Extend AbstractIntegrationTest to get a clean H2 + migrated schema.
    - Use MockMvc standalone setup if you want ControllerAdvice behavior without starting the full web server:
      mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();
    - Seed data either by calling services/repositories or with @Sql scripts. Clean-up is handled for you by the BEFORE_TEST_METHOD @Sql in AbstractIntegrationTest.
  - Data layer tests (@DataJpaTest)
    - You can use @DataJpaTest for repository testing; ensure Flyway is active (in many setups Spring Boot auto-detects Flyway). If you want schema strictly from migrations, keep ddl-auto=none and ensure flyway locations are visible. If you want to bypass Flyway for a pure JPA DDL test, override properties in the test with @TestPropertySource and set spring.jpa.hibernate.ddl-auto=create-drop.
  - Unit tests (pure JVM, no Spring context)
    - Keep them outside of Spring where possible for speed; use Mockito if needed (spring-boot-starter-test includes it).
- Demonstrated minimal test run (what we validated while writing these notes)
  - The existing BookControllerTest runs green with ./mvnw test (internally uses H2 + Flyway + cleaning script).
  - A simple JUnit 5 test compiling and running through surefire also works (we validated by temporarily creating a trivial test and running it). We removed the temporary test to keep the repo clean.

3. Additional Development Notes
- Database and Migrations
  - Flyway migrations live under src/main/resources/flyway/mysql and are used both in prod and tests.
  - Because tests run against H2 in MODE=MYSQL, prefer ANSI SQL or MySQL features supported by H2. Test your migrations locally with ./mvnw test to catch incompatibilities early.
  - The test cleaning script (src/test/resources/sql/clean-all-data.sql) assumes specific table names and disables foreign key checks; keep it in sync with schema changes.
- JPA/Hibernate
  - spring.jpa.hibernate.ddl-auto=none to avoid drift; schema is owned by Flyway.
  - spring.jpa.properties.hibernate.query.fail_on_pagination_over_collection_fetch=true is enabled; avoid fetch-joining collection associations with pagination. Consider slicing queries or adjusting fetch strategies.
- MapStruct
  - The project uses MapStruct for mapping DTOs to entities and vice versa. If you add new mappers, ensure @Mapper interfaces are present and that annotation processing is enabled in your IDE so generated sources appear under target/generated-sources/annotations.
- Logging/Debugging
  - SQL logging is enabled in tests (spring.jpa.show-sql=true). For detailed Hibernate logs, consider enabling org.hibernate.SQL and org.hibernate.type.descriptor.sql logs in application-test properties if needed.
  - For controller tests using MockMvc standaloneSetup, remember to register GlobalControllerAdvice so error payloads match production behavior.
- Docker workflow tips
  - docker/Dockerfile expects target/*.jar. Run ./mvnw -q -DskipTests package (or full verify) before docker compose up.
  - On schema changes, rebuild the app image so Flyway migrations are included. The DB data persists in the bookcase_data volume; you may need to drop it to test clean migrations: docker compose down -v.
- Conventions
  - Java: 21, JUnit 5, AssertJ or JUnit assertions. Keep tests deterministic and independent.
  - Packages
    - controller: REST endpoints + GlobalControllerAdvice
    - service: business logic
    - repository: Spring Data JPA repositories
    - dto: request/response objects
    - mapper: MapStruct mappers
    - specification: dynamic query specifications
    - util/exception: helpers and error types

4. Quick Commands Reference
- Build: ./mvnw clean verify
- Run (Docker): docker compose -f docker/docker-compose.yml up
- Stop: docker compose -f docker/docker-compose.yml down
- Run all tests: ./mvnw test
- Run one test: ./mvnw -Dtest=BookControllerTest test
- Run one method: ./mvnw -Dtest=BookControllerTest#create_isSuccessful test

Notes
- These guidelines intentionally skip generic Java/Maven basics and focus on details unique to this repository (Java 21 + preview, H2 in MySQL mode with Flyway, cleaning strategy, and Docker wiring).
