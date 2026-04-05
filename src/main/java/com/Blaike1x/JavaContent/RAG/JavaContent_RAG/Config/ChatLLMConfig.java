package com.Blaike1x.JavaContent.RAG.JavaContent_RAG.Config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatLLMConfig {

    private Advisor buildRagAdvisor(VectorStore vectorStore)
    {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.8d)
                .vectorStore(vectorStore)
                .build())
                .build();
    }

    @Bean("openAIChatClient")
    public ChatClient openAIChatClient(OpenAiChatModel openAiChatModel, VectorStore vectorStore)
    {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(buildRagAdvisor(vectorStore))
                .build();
    }

    @Bean("anthropicClient")
    public ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel, VectorStore vectorStore)
    {
        return ChatClient.builder(anthropicChatModel)
                .defaultAdvisors(buildRagAdvisor(vectorStore))
                .build();
    }

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, VectorStore vectorStore)
    {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(buildRagAdvisor(vectorStore))
                .build();
    }
}
