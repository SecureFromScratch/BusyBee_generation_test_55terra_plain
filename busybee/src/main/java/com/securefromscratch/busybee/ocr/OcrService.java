package com.securefromscratch.busybee.ocr;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.securefromscratch.busybee.comment.ImageMetadataRepository;
import com.securefromscratch.busybee.comment.ImageStorageService;
import com.securefromscratch.busybee.comment.StoredImage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
class OcrService {

    private static final Set<String> LANGUAGES = Set.of("eng", "heb", "eng+heb");
    private static final Set<Integer> TEXT_LAYOUTS = Set.of(3, 4, 6, 7, 8, 11, 12, 13);

    private final ImageMetadataRepository imageMetadataRepository;
    private final ImageStorageService imageStorageService;
    private final String ocrCommand;

    OcrService(
            ImageMetadataRepository imageMetadataRepository,
            ImageStorageService imageStorageService,
            @Value("${busybee.ocr.command}") String ocrCommand
    ) {
        this.imageMetadataRepository = imageMetadataRepository;
        this.imageStorageService = imageStorageService;
        this.ocrCommand = ocrCommand;
    }

    OcrDraft extractImage(Authentication authentication, OcrRequest request) {
        requireOcrAccess(authentication);
        String imageId = validImageId(request.image());
        validateParameters(request);
        StoredImage image = imageMetadataRepository.findImage(imageId)
                .orElseThrow(() -> new OcrException("Image was not found."));
        String rawText = runOcr(imageStorageService.pathFor(image), request.language(), request.textLayout());
        return toDraft(rawText);
    }

    private void requireOcrAccess(Authentication authentication) {
        boolean permitted = authentication.getAuthorities().stream()
                .anyMatch(authority -> "OCR_ENABLED".equals(authority.getAuthority())
                        || "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!permitted) {
            throw new OcrException("OCR is not available for this user.");
        }
    }

    private String validImageId(String imageId) {
        if (imageId == null) {
            throw new OcrException("Image was not found.");
        }

        try {
            return UUID.fromString(imageId).toString();
        } catch (IllegalArgumentException exception) {
            throw new OcrException("Image was not found.");
        }
    }

    private void validateParameters(OcrRequest request) {
        if (!LANGUAGES.contains(request.language()) || !TEXT_LAYOUTS.contains(request.textLayout())) {
            throw new OcrException("OCR parameters are invalid.");
        }
    }

    private String runOcr(Path imagePath, String language, int textLayout) {
        List<String> command = List.of(
                ocrCommand,
                imagePath.toString(),
                "stdout",
                "-l",
                language,
                "--psm",
                String.valueOf(textLayout)
        );
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new OcrException("OCR timed out.");
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (process.exitValue() != 0 || output.isEmpty()) {
                throw new OcrException("OCR could not extract text.");
            }
            return output;
        } catch (IOException exception) {
            throw new OcrException("OCR could not be started.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OcrException("OCR was interrupted.", exception);
        }
    }

    private OcrDraft toDraft(String rawText) {
        String[] sections = rawText.split("\\R\\s*\\R", 2);
        String title = sections[0].trim();
        String description = sections.length == 1 ? title : sections[1].trim();
        return new OcrDraft(title, description, rawText);
    }
}
