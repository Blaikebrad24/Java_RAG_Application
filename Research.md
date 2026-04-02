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

  