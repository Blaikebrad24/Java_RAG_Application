package com.Blaike1x.JavaContent.RAG.JavaContent_RAG.Controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Blaike1x.JavaContent.RAG.JavaContent_RAG.Service.ChatClientService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClientService chatClientService;

    public ChatController(ChatClientService chatClientService)
    {
        this.chatClientService = chatClientService;
    }

    // ==================== Request / Response DTOs ====================

    public record ChatRequest(
        @NotBlank(message = "Question must not be blank")
        @Size(max = 5000, message = "Question must not exceed 5000 characters")
        String question
    ) {}

    public record ChatResponse(String model, String answer) {}


    // ==================== Synchronous Endpoints ====================

    @PostMapping("/openai")
    public ResponseEntity<ChatResponse> openAIChat(@Valid @RequestBody ChatRequest request)
    {
        String answer = chatClientService.openAIModelAnswer(request.question());
        return ResponseEntity.ok(new ChatResponse("openai", answer));
    }

    @PostMapping("/anthropic")
    public ResponseEntity<ChatResponse> anthropicChat(@Valid @RequestBody ChatRequest request)
    {
        String answer = chatClientService.anthropicModelAnswer(request.question());
        return ResponseEntity.ok(new ChatResponse("anthropic", answer));
    }

    @PostMapping("/ollama")
    public ResponseEntity<ChatResponse> ollamaChat(@Valid @RequestBody ChatRequest request)
    {
        String answer = chatClientService.ollamaModelAnswer(request.question());
        return ResponseEntity.ok(new ChatResponse("ollama", answer));
    }


    // ==================== Streaming Endpoints (SSE) ====================

    @PostMapping(value = "/openai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamOpenAI(@Valid @RequestBody ChatRequest request)
    {
        return chatClientService.streamOpenAIChat(request.question());
    }

    @PostMapping(value = "/anthropic/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAnthropic(@Valid @RequestBody ChatRequest request)
    {
        return chatClientService.streamAnthropicChat(request.question());
    }

    @PostMapping(value = "/ollama/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamOllama(@Valid @RequestBody ChatRequest request)
    {
        return chatClientService.streamOllamaChat(request.question());
    }
}
