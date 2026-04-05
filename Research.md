### Java Spring AI RAG Research Doc ###

- Building enterprise grade RAG application foundation 

`RAG IMPLEMENTATIONS ARCHITECTURE`: 
    1) Basic RAG (Retrieval | Augmentation | Generation)
        - Convert Documents to Embeddings (numerical representations)
        - Embed User Query (user's query is converted into embeddings)
        - Retrieve Similar Documents (documents similar to the query are identified)
        - Augment Prompt with Retrieved Documents (the prompt is enhanced with relevant documents)
        - Generate Response (response is created based on augmented Prompt)

        1B) Limitations of Retrieval Approach
            - Single retrieval step (approach performs only one retrieval step)
            - No verification mechanisms (approach lacks methods to verfiy or refine information)
            - Equal Treatment of Documents (approach treats all documents as equally relevant)
            - Lack of Query Adaptation (does not adjust to different query types)

    2) Cache Augmented Generation
        - stores inference state 
        - maintains cache of computations 
        - useful for repeated queries 
        - reduces latency and is well suited for predictable query patterns/similar questions
    
    3) Agentic RAG 
        - Decision making abilities dictated by the system
        - uses LLM for sub queries
        - uses different retrieval strategies for different queries 
        - complex queries, multi-step reasoning
    
    4) Corrective RAG
        - includes a verification step
        - filters out irrelevant queries 
        - may use separate model to evaluate content 
        - Accuracy Critical

1) `Document Ingestion | Vector Database` 
    ### Traditional Database vs Vector Database ###
    `What is a Vector Database`: 
        - A vector database is a semantic retrieval engine. It stores numerical representations of meaning — called embeddings —
    in high-dimensional mathematical space (often 768–1536 dimensions). Items that are similar in meaning are close together
    in that space. The database uses specialized indexing algorithms and distance metrics to efficiently find the nearest
    neighbors to a given query vector.

  ### Notes ###
The key property is that text chunks with similar meaning end up with similar vectors (close together in that 384-dimensional space). So when you do a similarity search, you're finding chunks whose meaning is close to your query — not matching characters or keywords.

So in your pipeline: Tika reads the document, the TokenTextSplitter breaks it into chunks, and then the transformers model converts each chunk into one float[384] array that captures its semantic meaning. That array is what gets stored in the embedding column.
  ### Notes ###

2) `Augmentation & Generation | ChatClient + OpenAI Model`
    ### What is the ChatClient ###
    `What is ChatClient`:
        - ChatClient is Spring AI's high-level abstraction for interacting with LLMs (like OpenAI GPT, Anthropic Claude, etc.)
        - It wraps the underlying model API calls and provides a fluent builder-style interface for constructing prompts and receiving responses
        - Think of it as the equivalent of RestTemplate/WebClient but for LLM APIs — it handles serialization, API keys, retry logic, and response parsing
        - Created via `ChatClient.builder(chatModel)` where `chatModel` is the specific model implementation (e.g., OpenAiChatModel)
        - Spring Boot auto-configures a `ChatClient.Builder` bean when an AI model starter is on the classpath

    `ChatClient vs ChatModel`:
        - ChatModel is the low-level interface — it maps directly to the model API (send messages, get a response)
        - ChatClient is the high-level interface — it adds prompt templating, advisors (middleware), output parsing, and fluent API
        - You almost always want to use ChatClient in application code; ChatModel is for framework-level customization

    ### How the Augmentation Step Works ###
    `What is Augmentation (the "A" in RAG)`:
        - Augmentation is the step where retrieved documents are injected into the prompt sent to the LLM
        - The LLM does NOT directly access your vector database — instead, your application retrieves relevant chunks
          and stuffs them into the prompt as context
        - This gives the LLM grounded, domain-specific knowledge it was never trained on

    `Prompt Structure for RAG`:
        - A RAG prompt typically has this structure:
            ```
            System: You are a helpful assistant. Use the following context to answer the user's question.
            Context: {retrieved document chunks inserted here}
            User: {the user's actual question}
            ```
        - The "context" section is the augmentation — you are augmenting the user's query with retrieved knowledge

    `How Spring AI Handles This — Advisors`:
        - Spring AI uses an "Advisor" pattern (similar to middleware/interceptors) to handle RAG augmentation automatically
        - `QuestionAnswerAdvisor` — the key advisor for RAG. It takes a VectorStore, automatically performs similarity search
          on the user's query, retrieves relevant documents, and injects them into the prompt before sending to the LLM
        - This means you don't manually retrieve + concatenate + send — the advisor pipeline does it for you
        - Advisors are added to the ChatClient via `.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))`

    ### Connecting to OpenAI ###
    `OpenAI Model Integration`:
        - Requires the `spring-ai-starter-model-openai` dependency (currently commented out in pom.xml — needs to be uncommented)
        - Requires an `OPENAI_API_KEY` environment variable or property: `spring.ai.openai.api-key`
        - Spring Boot auto-configures `OpenAiChatModel` and a `ChatClient.Builder` bean once the dependency + key are present
        - Default model is `gpt-4o-mini` — configurable via `spring.ai.openai.chat.options.model`
        - Temperature, max-tokens, and other parameters configurable via `spring.ai.openai.chat.options.*`

    `Key Properties for application.properties`:
        - `spring.ai.openai.api-key=${OPENAI_API_KEY}` — API key from environment
        - `spring.ai.openai.chat.options.model=gpt-4o-mini` — which model to use (gpt-4o-mini is cheapest, gpt-4o is most capable)
        - `spring.ai.openai.chat.options.temperature=0.7` — controls randomness (0.0 = deterministic, 1.0 = creative)

    ### Generation Step ###
    `What is Generation (the "G" in RAG)`:
        - Generation is the final step — the LLM receives the augmented prompt and produces a natural language response
        - The LLM synthesizes the retrieved context with its own training knowledge to give a grounded answer
        - The response comes back as a `ChatResponse` object containing the generated text, token usage, and metadata

    `Basic Flow in Code`:
        1. User submits a question (e.g., via REST endpoint or command line)
        2. ChatClient receives the question
        3. QuestionAnswerAdvisor intercepts → queries VectorStore → retrieves top-K similar chunks
        4. Advisor augments the prompt with retrieved chunks
        5. Augmented prompt is sent to OpenAI API
        6. OpenAI returns a generated response grounded in your documents
        7. Response is returned to the user

    ### ChatClient Code Patterns to Research ###
    `Pattern 1 — Simple Prompt + Response (no RAG)`:
        - Use ChatClient to send a basic prompt and get a response — good for testing the OpenAI connection
        - `chatClient.prompt().user("What is RAG?").call().content()`
        - This confirms your API key and model are working before adding the RAG layer

    `Pattern 2 — Prompt with System Message`:
        - Set a system message to control the LLM's behavior/persona
        - `.system("You are a technical assistant that answers questions about Java development")`
        - System messages shape how the LLM interprets and responds to user queries

    `Pattern 3 — RAG with QuestionAnswerAdvisor`:
        - The full RAG pipeline: ChatClient + VectorStore + QuestionAnswerAdvisor
        - The advisor handles retrieval + augmentation automatically
        - This is the target implementation for the ChatClientLLM package

    `Pattern 4 — Custom Prompt Templates`:
        - PromptTemplate lets you define reusable prompt structures with variable placeholders
        - Useful for structuring how retrieved context is presented to the LLM
        - `new PromptTemplate("Given this context: {context}\nAnswer: {question}")`

    ### Research Topics for Implementation ###
    `Things to Understand Before Coding`:
        - [ ] How does ChatClient.Builder auto-configuration work with Spring Boot?
        - [ ] What is the Advisor chain and how does QuestionAnswerAdvisor fit in?
        - [ ] How does QuestionAnswerAdvisor construct the augmented prompt internally?
        - [ ] What are the token limits and how to handle context window overflow (when retrieved docs are too large)?
        - [ ] How to handle API key management securely (environment variables vs. Spring profiles)
        - [ ] What is the difference between `.call()` and `.stream()` on ChatClient (synchronous vs streaming responses)?
        - [ ] How to configure and tune the SearchRequest inside QuestionAnswerAdvisor (topK, similarity threshold)?
        - [ ] Error handling — what happens when OpenAI rate-limits or the API key is invalid?