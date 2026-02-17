package com.milind.docintel.controller;

import com.milind.docintel.dto.request.AskQueryRequest;
import com.milind.docintel.dto.response.QueryResponse;
import com.milind.docintel.service.ai.AiOrchestratorService;
import com.milind.docintel.service.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final AiOrchestratorService aiOrchestratorService;

    public QueryController(AiOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponse> ask(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Valid @RequestBody AskQueryRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.answer(user, request));
    }
}
