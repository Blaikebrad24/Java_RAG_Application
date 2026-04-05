# JavaContent RAG

A Retrieval-Augmented Generation (RAG) application built with **Spring Boot 4** and **Spring AI 2.0**. This project implements a basic RAG pipeline — document ingestion, vector embedding, and similarity-based retrieval — as a learning foundation before porting the concepts to a Golang implementation for comparative content.

## Architecture

The application follows the full RAG pipeline — Retrieval, Augmentation, and Generation:

1. **Document Ingestion** - Apache Tika reads documents (PDF, DOCX, TXT, MD, HTML, PPTX)
2. **Text Splitting** - `TokenTextSplitter` breaks documents into manageable chunks
3. **Embedding** - Spring AI Transformers model converts chunks into 384-dimensional vector embeddings
4. **Storage** - Embeddings are stored in PostgreSQL with the pgvector extension
5. **Retrieval** - `RetrievalAugmentationAdvisor` automatically queries the vector store for relevant chunks
6. **Augmentation** - Retrieved document chunks are injected into the LLM prompt as context
7. **Generation** - The LLM synthesizes retrieved context into a natural language response

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 4.0.5 / Spring AI 2.0.0-M3 |
| Language | Java 21 |
| LLM Providers | OpenAI, Anthropic Claude, Ollama (local) |
| Vector Store | PostgreSQL 17 + pgvector |
| Document Parsing | Apache Tika |
| Embedding Model | Spring AI Transformers (local, 384-dim) |
| RAG Advisor | `RetrievalAugmentationAdvisor` with `VectorStoreDocumentRetriever` |
| Build Tool | Maven |
| Infrastructure | Docker Compose (pgvector + Ollama) |

## Project Structure

```
src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/
  JavaContentRagApplication.java       # Entry point - loads documents on startup
  Config/
    ChatLLMConfig.java                 # @Configuration - ChatClient beans for each LLM provider with RAG advisor
  Service/
    ChatClientService.java             # @Service - sync + streaming chat methods per model
  Controller/
    ChatController.java                # @RestController - REST + SSE endpoints at /api/chat
  LoadVectorDatabase/
    LoadDataFunctions.java             # ETL pipeline - read, chunk, embed, store
  RetrieveFromVectorStore/
    RetrievalMethods.java              # Similarity search against the vector store
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

### 2. Pull an Ollama model

```bash
docker exec -it rag-ollama ollama pull qwen2.5:7b
```

### 3. Configure environment variables

Create a `.env` file in the project root:

```properties
DATA_PATH=/path/to/your/documents
DB_URL=jdbc:postgresql://localhost:5432/ragdb
DB_USERNAME=postgres
DB_PASSWORD=postgres
OPENAI_APIKEY=your-openai-key
CLAUDE_APIKEY=your-anthropic-key
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On startup, the application automatically walks the `DATA_PATH` directory, parses all supported documents via Tika, splits them into token-based chunks, generates embeddings using the local transformers model, and loads them into the pgvector store.

## How It Works

**Document Loading** ([LoadDataFunctions.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/LoadVectorDatabase/LoadDataFunctions.java)) - The `loadDocumentsIntoVectorDatabase()` method recursively walks the configured data directory, filters for supported file types, reads each file with Apache Tika, splits content into chunks with `TokenTextSplitter`, and batch-inserts the resulting embeddings into the vector store.

**Retrieval** ([RetrievalMethods.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/RetrieveFromVectorStore/RetrievalMethods.java)) - Uses `VectorStore.similaritySearch()` with a configurable `topK` (default 7) to find the most semantically relevant document chunks for a given query.

**Vector Store Schema** ([init.sql](init.sql)) - Stores document content, metadata (JSONB), and 384-dimensional embeddings with an HNSW index using cosine distance for efficient approximate nearest neighbor search.

## API Endpoints

All endpoints accept a JSON body: `{ "question": "your question here" }`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/chat/openai` | Synchronous RAG response via OpenAI |
| POST | `/api/chat/anthropic` | Synchronous RAG response via Anthropic Claude |
| POST | `/api/chat/ollama` | Synchronous RAG response via local Ollama |
| POST | `/api/chat/openai/stream` | Streaming SSE response via OpenAI |
| POST | `/api/chat/anthropic/stream` | Streaming SSE response via Anthropic Claude |
| POST | `/api/chat/ollama/stream` | Streaming SSE response via local Ollama |

Example request:
```bash
curl -X POST http://localhost:8080/api/chat/ollama \
  -H "Content-Type: application/json" \
  -d '{"question": "What is dependency injection?"}'
```

## How It Works

**Configuration** ([ChatLLMConfig.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/Config/ChatLLMConfig.java)) - Defines a `ChatClient` bean for each LLM provider (OpenAI, Anthropic, Ollama). Each client is built with a `RetrievalAugmentationAdvisor` that automatically performs similarity search against the vector store and augments the prompt with retrieved documents.

**Service** ([ChatClientService.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/Service/ChatClientService.java)) - Provides synchronous (`.call()`) and streaming (`.stream()`) methods for each model. The advisor handles RAG transparently — the service just passes the user's question and gets back a grounded response.

**Controller** ([ChatController.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/Controller/ChatController.java)) - REST API layer with input validation. Sync endpoints return JSON, streaming endpoints return Server-Sent Events (SSE).

**Document Loading** ([LoadDataFunctions.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/LoadVectorDatabase/LoadDataFunctions.java)) - The `loadDocumentsIntoVectorDatabase()` method recursively walks the configured data directory, filters for supported file types, reads each file with Apache Tika, splits content into chunks with `TokenTextSplitter`, and batch-inserts the resulting embeddings into the vector store.

**Retrieval** ([RetrievalMethods.java](src/main/java/com/Blaike1x/JavaContent/RAG/JavaContent_RAG/RetrieveFromVectorStore/RetrievalMethods.java)) - Uses `VectorStore.similaritySearch()` with a configurable `topK` (default 7) for direct vector store queries outside the advisor pipeline.

## Research Notes

See [Research.md](Research.md) for detailed notes on RAG architecture patterns (Basic RAG, Cache Augmented Generation, Agentic RAG, Corrective RAG), vector databases, embeddings, ChatClient abstraction, and the augmentation/generation pipeline.

See [OllamaLocalAI.md](OllamaLocalAI.md) for a guide on setting up a self-hosted Ollama instance on dedicated hardware (Mac Mini/Studio) as a local AI replacement.
