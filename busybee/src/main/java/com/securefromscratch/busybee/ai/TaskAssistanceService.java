package com.securefromscratch.busybee.ai;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
class TaskAssistanceService {

    private final AiCredentialStatusService aiCredentialStatusService;

    TaskAssistanceService(AiCredentialStatusService aiCredentialStatusService) {
        this.aiCredentialStatusService = aiCredentialStatusService;
    }

    ImprovedTaskResponse improve(Authentication authentication, TaskAssistanceRequest request) {
        requireCredential(authentication);
        requireTaskDraft(request);
        return new ImprovedTaskResponse("Improved task title", "Improved task description.");
    }

    SubtaskSuggestionsResponse subtasks(Authentication authentication, TaskAssistanceRequest request) {
        requireCredential(authentication);
        requireTaskDraft(request);
        return new SubtaskSuggestionsResponse(List.of("First subtask", "Second subtask"));
    }

    OcrStructureResponse structureOcr(Authentication authentication, OcrStructureRequest request) {
        requireCredential(authentication);
        if (request.rawText() == null || request.rawText().isBlank()) {
            throw new InvalidTaskAssistanceException("OCR text is required.");
        }
        return new OcrStructureResponse("OCR task title", "Structured from OCR text.", List.of());
    }

    private void requireCredential(Authentication authentication) {
        if ("UNAVAILABLE".equals(aiCredentialStatusService.statusFor(authentication).selection())) {
            throw new InvalidTaskAssistanceException("AI credential is unavailable.");
        }
    }

    private void requireTaskDraft(TaskAssistanceRequest request) {
        if (request.title() == null || request.title().isBlank()
                || request.description() == null || request.description().isBlank()) {
            throw new InvalidTaskAssistanceException("Task title and description are required.");
        }
    }
}
