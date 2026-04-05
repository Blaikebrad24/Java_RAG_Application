package com.Blaike1x.JavaContent.RAG.JavaContent_RAG.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;


@Service
public class ChatClientService {


    private static final Logger logger = LoggerFactory.getLogger(ChatClientService.class);

    private final ChatClient anthropicChatClient;
    private final ChatClient openAIChatClient;
    private final ChatClient ollamaChatClient;

    public ChatClientService(
        @Qualifier("anthropicClient") ChatClient anthropicChatClient,
        @Qualifier("openAIChatClient") ChatClient openAIChatClient,
        @Qualifier("ollamaChatClient") ChatClient ollamaChatClient)
    {
        this.anthropicChatClient = anthropicChatClient;
        this.openAIChatClient = openAIChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }


    // ==================== Synchronous Methods ====================

    public String anthropicModelAnswer(String question)
    {
        String answer = anthropicChatClient.prompt().user(question).call().content();
        logger.info("Anthropic Agent has generated a response");
        return answer;
    }

    public String openAIModelAnswer(String question)
    {
        String answer = openAIChatClient.prompt().user(question).call().content();
        logger.info("OpenAI Agent has generated a response");
        return answer;
    }

    public String ollamaModelAnswer(String question)
    {
        String answer = ollamaChatClient.prompt().user(question).call().content();
        logger.info("Ollama Agent has generated a response");
        return answer;
    }


    // ==================== Streaming Methods ====================

    public Flux<String> streamOpenAIChat(String question)
    {
        logger.info("Streaming OpenAI response");
        return openAIChatClient.prompt().user(question).stream().content();
    }

    public Flux<String> streamAnthropicChat(String question)
    {
        logger.info("Streaming Anthropic response");
        return anthropicChatClient.prompt().user(question).stream().content();
    }

    public Flux<String> streamOllamaChat(String question)
    {
        logger.info("Streaming Ollama response");
        return ollamaChatClient.prompt().user(question).stream().content();
    }

}
