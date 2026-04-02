# JavaContent RAG

A Retrieval-Augmented Generation (RAG) application built with **Spring Boot 4** and **Spring AI 2.0**. This project implements a basic RAG pipeline — document ingestion, vector embedding, and similarity-based retrieval — as a learning foundation before porting the concepts to a Golang implementation for comparative content.

## Architecture

The application follows the core RAG pattern:

1. **Document Ingestion** - Apache Tika reads documents (PDF, DOCX, TXT, MD, HTML, PPTX)
2. **Text Splitting** - `TokenTextSplitter` breaks documents into manageable chunks
3. **Embedding** - Spring AI Transformers model converts chunks into 384-dimensional vector embeddings
4. **Storage** - Embeddings are stored in PostgreSQL with the pgvector extension
5. **Retrieval** - Similarity search (cosine distance via HNSW index) finds the most relevant chunks for a given query

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 4.0.5 / Spring AI 2.0.0-M3 |
| Language | Java 21 |
| Vector Store | PostgreSQL 17 + pgvector |
| Document Parsing | Apache Tika |
| Embedding Model | Spring AI Transformers (local, 384-dim) |
| Build Tool | Maven |
| Infrastructure | Docker Compose |

## Project Structure

```
src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/
  JavaContentRagApplication.java       # Entry point - loads documents on startup
  LoadVectorDatabase/
    LoadDataFunctions.java             # ETL pipeline - read, chunk, embed, store
  RetrieveFromVectorStore/
    RetrievalMethods.java              # Similarity search against the vector store
  ChatClientLLM/                       # (Planned) LLM chat client integration
  Data/                                # Sample documents for ingestion
```

## Prerequisites

- Java 21+
- Maven
- Docker & Docker Compose

## Getting Started

### 1. Start the pgvector database

```bash
docker compose up -d
```

This launches a PostgreSQL 17 container with pgvector enabled, creates the `ragdb` database, and runs [init.sql](init.sql) to set up the `vector_store` table with an HNSW index.

### 2. Configure environment variables

Create a `.env` file in the project root:

```properties
DATA_PATH=/path/to/your/documents
DB_URL=jdbc:postgresql://localhost:5432/ragdb
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

On startup, the application automatically walks the `DATA_PATH` directory, parses all supported documents via Tika, splits them into token-based chunks, generates embeddings using the local transformers model, and loads them into the pgvector store.

## How It Works

**Document Loading** ([LoadDataFunctions.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/LoadVectorDatabase/LoadDataFunctions.java)) - The `loadDocumentsIntoVectorDatabase()` method recursively walks the configured data directory, filters for supported file types, reads each file with Apache Tika, splits content into chunks with `TokenTextSplitter`, and batch-inserts the resulting embeddings into the vector store.

**Retrieval** ([RetrievalMethods.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/RetrieveFromVectorStore/RetrievalMethods.java)) - Uses `VectorStore.similaritySearch()` with a configurable `topK` (default 7) to find the most semantically relevant document chunks for a given query.

**Vector Store Schema** ([init.sql](init.sql)) - Stores document content, metadata (JSONB), and 384-dimensional embeddings with an HNSW index using cosine distance for efficient approximate nearest neighbor search.

## Research Notes

See [Research.md](Research.md) for detailed notes on RAG architecture patterns including Basic RAG, Cache Augmented Generation, Agentic RAG, and Corrective RAG, as well as notes on vector databases and embeddings.
