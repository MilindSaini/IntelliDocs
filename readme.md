# IntelliDocs Backend

Spring Boot backend for document ingestion, chunking, pgvector search, and Gemini-powered Q&A.

## What It Does

1. Uploads documents (PDF/DOCX/TXT).
2. Extracts text and splits it into chunks.
3. Generates embeddings for each chunk.
4. Stores embeddings in Postgres (pgvector).
5. Runs RAG: embeds the question, retrieves top chunks, builds a prompt, and calls Gemini.

## Key Features

- JWT auth (signup/login)
- Async document processing pipeline
- Vector search with pgvector
- Gemini API integration for answers
- Auditable query/response logs

## Tech Stack

- Java + Spring Boot
- PostgreSQL + pgvector
- Flyway migrations
- Gemini API (Google Generative Language API)

## Project Structure (High Level)

- `controller/` � REST endpoints
- `service/` � business logic
- `processing/` � extraction, chunking, embeddings
- `vector/` � pgvector adapter
- `entity/` + `repository/` � DB model + access
- `config/` � security, AI, async, vector, storage

## API Endpoints

Base URL: `http://localhost:8080`

### Auth

- `POST /api/auth/signup`
  - Body:
    ```json
    {
      "email": "user@example.com",
      "password": "StrongPass123"
    }
    ```
- `POST /api/auth/login`
  - Body:
    ```json
    {
      "email": "user@example.com",
      "password": "StrongPass123"
    }
    ```
  - Response includes JWT token.

### Documents

- `POST /api/documents` (multipart form)
  - Auth: Bearer token
  - Form field: `file` (PDF/DOCX/TXT)

- `GET /api/documents`
  - Auth: Bearer token
  - Lists user docs and status (`UPLOADED`, `PROCESSING`, `READY`).

- `DELETE /api/documents/{documentId}`
  - Auth: Bearer token

### Query

- `POST /api/query/ask`
  - Auth: Bearer token
  - Body:
    ```json
    {
      "documentId": "<uuid>",
      "question": "Summarize this document",
      "topK": 5
    }
    ```
  - Response:
    ```json
    {
      "answer": "...",
      "sources": [
        {
          "chunkId": "<uuid>",
          "similarityScore": 0.82,
          "excerpt": "..."
        }
      ]
    }
    ```

## How It Works (Pipeline)

### Upload

1. `DocumentController.upload()`
2. Store file locally
3. Save metadata (`documents` table)
4. Publish `DocumentUploadedEvent`

### Processing (Async)

1. Extract text (PDF/DOCX/TXT)
2. Chunk text
3. Save chunks
4. Embed each chunk
5. Save vectors to `chunk_embeddings`
6. Mark document `READY`

### Query

1. Embed question
2. Vector search top-K chunks
3. Build RAG prompt
4. Gemini API call
5. Store query/response + sources

## Database & Migrations

- Flyway migrations live in `src/main/resources/db/migration`
- pgvector extension is required
- Tables include:
  - `users`, `documents`, `document_chunks`, `chunk_embeddings`, `ai_queries`, `ai_responses`, `ai_response_sources`

## Run Locally

### 1) Install pgvector

Make sure your Postgres instance has pgvector installed.

### 2) Create DB

```
createdb docintel
```

### 3) Configure properties

Create `src/main/resources/application.properties` locally.

Sample:

```properties
spring.application.name=docintel-backend

server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/docintel
spring.datasource.username=postgres
spring.datasource.password=postgres

# If using Supabase transaction pooler, disable prepared statements:
# spring.datasource.url=jdbc:postgresql://<pooler-host>:6543/postgres?sslmode=require&prepareThreshold=0&preferQueryMode=simple
# spring.datasource.hikari.data-source-properties.prepareThreshold=0
# spring.datasource.hikari.data-source-properties.preferQueryMode=simple

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

spring.servlet.multipart.max-file-size=25MB
spring.servlet.multipart.max-request-size=25MB

docintel.jwt.secret=CHANGE_THIS_SECRET
# 24h
 docintel.jwt.expiration-seconds=86400

docintel.storage.local-root=storage/documents
docintel.storage.public-base-url=/files

docintel.processing.chunk-size=300
docintel.processing.chunk-overlap=50

docintel.embedding.dimension=1536
docintel.embedding.model=fallback-hash-embedding

docintel.ai.max-context-chunks=5

# Gemini
 docintel.gemini.api-key=YOUR_GEMINI_KEY
 docintel.gemini.model=gemini-1.5-flash
 docintel.gemini.base-url=https://generativelanguage.googleapis.com/v1beta
 docintel.gemini.timeout-seconds=30
 docintel.gemini.temperature=0.2
 docintel.gemini.max-output-tokens=1024
```

### 4) Run

```
mvn spring-boot:run
```

## Postman Quick Test

1. Signup ? `POST /api/auth/signup`
2. Login ? `POST /api/auth/login`
3. Upload file ? `POST /api/documents`
4. Wait until status = `READY` ? `GET /api/documents`
5. Ask question ? `POST /api/query/ask`

## Do Not Commit Secrets

Do not commit:

- `src/main/resources/application.properties`
- `.env` files
- `storage/`
- any logs or DB dumps

Make sure `.gitignore` includes these.

## Troubleshooting

- `relation "chunk_embeddings" does not exist`
  - Flyway didn�t run. Ensure `spring.flyway.enabled=true` and `ddl-auto=validate`.

- `prepared statement "S_x" already exists`
  - If using Supabase pooler, disable prepared statements (see sample config).

- Gemini 404: model not found
  - Use `ListModels` to confirm available models for your key.

