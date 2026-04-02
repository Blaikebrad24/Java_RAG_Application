package com.Blaike1x.JavaContent.RAG.JavaContent_RAG.RetrieveFromVectorStore;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

public class RetrievalMethods {
    

    private VectorStore vectorStore;

    public RetrievalMethods(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void retrieveDocumentsFromVectorStore(String query)
    {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(7).build());
        if(results != null)
        {
            for (Document document : results){
                System.out.println("======");
                System.out.println(document.getFormattedContent());
            }
        }else 
        {
            System.out.println("Results are empty");
        }
    }

}
