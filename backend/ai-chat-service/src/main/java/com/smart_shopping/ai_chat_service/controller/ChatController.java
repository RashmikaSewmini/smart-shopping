package com.smartshopping.aichat.controller;

import com.smartshopping.aichat.dto.ChatRequest;
import com.smartshopping.aichat.dto.ChatResponse;
import com.smartshopping.aichat.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai-chat")
public class ChatController {

    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        if (request.message().length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message must be 2000 characters or fewer."
            );
        }

        try {
            return new ChatResponse(
                    geminiService.ask(request.message().trim())
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The AI assistant is temporarily unavailable."
            );
        }
    }
}
