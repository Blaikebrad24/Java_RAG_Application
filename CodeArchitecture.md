### Code & Architecture for RAG Implementation with Java + Spring AI + Pgvector | Multi Layer Project ###

*** Steps in Code Impl ***
1) Navigate to `Spring Initializr` in the browser 
    *** Project Configurations & Metadata ***
    - Project: Maven
    - Language: Java 
    - Spring Boot version: LTS or SNAPSHOT
    - Group: com.Blaike1x.JavaContent.RAG
    - Artifact: JavaContent.RAG
    - Description: RAG Implementation using Spring AI + Pgvector
    - PackageName: filled out already 
    - Packaging: .JAR
    - Java: 21

    *** Spring Dependencies List ***
    - Spring Web `org.springframework.boot` -> `spring-boot-starter-web`
    - Spring AI Starter Model OpenAI `org.springframework.ai` -> `spring-ai-starter-model-openai`
    - Spring AI Starter Vectore Store PgVector/Pinecone `org.springframework.ai` -> `spring-ai-starter-vector-store-pgvector`/`pinecone`
    - Spring AI Advisors Vector Store
    - Spring AI RAG
    - Spring AI TIKA Document Reader
    - Spring AI Transformers 

*** @SpringBootApplication | Setup | Class Structure | File Hierarchy ***

### Resources Needed ###
`etl-documents-folder-path`: Documents to train the LLM for context
### Known Functions ###
`loadDocumentsIntoVectorDatabase()`:
    - use a logger object 
    - read files with extensions of [.pdf,.doc,.docx,.txt]
`basicPromptAndResponse(ChatClient chatClient)`
`loadDocumentsIntoVectorStore()`: create a List<Document> object and add to vectorStore object (for small datasets)
`retrieveDocumentsFromVectorStore(String query)`: invoke `similaritySearch` on the vectorStore object using a SearchRequest.builder() to query for topK results
`customPromptTemplateSample(ChatClient chatClient)`
`questionAnswerAdvisorSample(ChatClient chatClient)`