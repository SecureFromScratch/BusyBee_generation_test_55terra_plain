package com.securefromscratch.busybee.extra;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
class TaskTransferController {

    private final TaskTransferService taskTransferService;
    private final ObjectMapper objectMapper;

    TaskTransferController(TaskTransferService taskTransferService, ObjectMapper objectMapper) {
        this.taskTransferService = taskTransferService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/extra/export")
    ResponseEntity<byte[]> exportTasks(Authentication authentication) throws IOException {
        byte[] contents = objectMapper.writeValueAsBytes(taskTransferService.exportTasks(authentication));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("tasks.ser").build().toString())
                .body(contents);
    }

    @PostMapping(path = "/extra/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> importTasks(Authentication authentication, @RequestPart("file") MultipartFile file) {
        try {
            List<ImportedTask> tasks = objectMapper.readValue(file.getBytes(), new TypeReference<>() { });
            taskTransferService.importTasks(authentication, tasks);
            return ResponseEntity.ok().build();
        } catch (IOException exception) {
            throw new InvalidTaskTransferException("Import file is invalid.");
        }
    }

    @ExceptionHandler(InvalidTaskTransferException.class)
    ResponseEntity<Map<String, String>> invalidImport(InvalidTaskTransferException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
