package com.securefromscratch.busybee.functionality;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"mysql", "test"})
class BusyBeeFunctionalityTests {

    private static final String FALLBAK_OPENAI_API_KEY = "do not hardcode!";

    private static final String MYSQL_DOWN_MESSAGE = """
            ***************
            MySQL is down. Start it with:
            C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin>.\\mysqld.exe
            ***************
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void mysqlIsRunning() {
        Assertions.assertDoesNotThrow(BusyBeeFunctionalityTests::connectToMysql, MYSQL_DOWN_MESSAGE);
    }

    private static void connectToMysql() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("192.168.48.1", 3307), 1000);
        }
    }

    @Test
    void contextLoads() {
    }

    @Test
    void registerRejectsDuplicateUser() throws Exception {
        String username = uniqueUsername();

        register(username)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectTo").value("/main/main.html"));

        register(username)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is already registered."));
    }

    @Test
    void adminCanSearchUsersAndSetEntitlements() throws Exception {
        String username = createUser();

        mvc.perform(get("/admin/users")
                        .queryParam("query", username)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(username));

        mvc.perform(post("/admin/user-entitlements")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "entitlements", List.of("IMPORT_ENABLED", "OCR_ENABLED")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.entitlements[0]").value("IMPORT_ENABLED"))
                .andExpect(jsonPath("$.entitlements[1]").value("OCR_ENABLED"));

        mvc.perform(get("/me")
                        .with(user(username).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectiveEntitlements[0]").value("IMPORT_ENABLED"))
                .andExpect(jsonPath("$.effectiveEntitlements[1]").value("OCR_ENABLED"));
    }

    @Test
    void userCanCreateCommentAndCompleteTask() throws Exception {
        String username = createUser();
        String taskId = createTask(username);

        addComment(username, taskId, "hello from integration")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentid").exists());

        mvc.perform(post("/done")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("taskid", taskId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        JsonNode task = findTask(username, taskId);
        Assertions.assertTrue(task.get("done").asBoolean());
    }

    @Test
    void commentImageCanBeFetchedAndConvertedToOcrDraft() throws Exception {
        String username = createUser();
        String taskId = createTask(username);
        String commentId = commentWithImage(username, taskId);
        JsonNode comment = findComment(commentId);
        String imageId = comment.get("image").asText();

        Assertions.assertEquals("note.png", comment.get("imageFilename").asText());
        Assertions.assertNotEquals(imageId, comment.get("imageFilename").asText());
        Assertions.assertTrue(comment.get("attachment").isNull());
        Assertions.assertTrue(comment.get("attachmentFilename").isNull());

        mvc.perform(get("/image")
                        .queryParam("img", imageId)
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));

        mvc.perform(post("/ocr/image")
                        .with(csrf())
                        .with(user(username).authorities(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("OCR_ENABLED")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "image", imageId,
                                "language", "eng",
                                "textLayout", 3
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.description").isNotEmpty())
                .andExpect(jsonPath("$.rawText").isNotEmpty());
    }

    @Test
    void svgCommentImageCanBeUploadedAndFetched() throws Exception {
        String username = createUser();
        String taskId = createTask(username);
        String commentId = commentWithSvgImage(username, taskId);
        JsonNode comment = findComment(commentId);
        String imageId = comment.get("image").asText();

        Assertions.assertEquals("diagram.svg", comment.get("imageFilename").asText());
        Assertions.assertNotEquals(imageId, comment.get("imageFilename").asText());

        mvc.perform(get("/image")
                        .queryParam("img", imageId)
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
    }

    @Test
    void commentAttachmentCanBeFetchedAndIncludesOriginalFilename() throws Exception {
        String username = createUser();
        String taskId = createTask(username);
        String commentId = commentWithAttachment(username, taskId);
        JsonNode comment = findComment(commentId);
        String attachmentId = comment.get("attachment").asText();

        Assertions.assertEquals("meeting-notes.txt", comment.get("attachmentFilename").asText());
        Assertions.assertNotEquals(attachmentId, comment.get("attachmentFilename").asText());
        Assertions.assertTrue(comment.get("image").isNull());
        Assertions.assertTrue(comment.get("imageFilename").isNull());

        mvc.perform(get("/attachment")
                        .queryParam("file", attachmentId)
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    void aiCredentialSelectionUsesPersonalKeyBeforeServerKey() throws Exception {
        String username = createUser();

        mvc.perform(get("/ai/credential")
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("gemini"))
                .andExpect(jsonPath("$.model").value("gemini-2.5-flash-lite"))
                .andExpect(jsonPath("$.providerType").value("GEMINI_FLASH"))
                .andExpect(jsonPath("$.personalCredentialConfigured").value(false))
                .andExpect(jsonPath("$.serverCredentialAvailable").value(true))
                .andExpect(jsonPath("$.serverCredentialAllowed").value(false))
                .andExpect(jsonPath("$.selection").value("UNAVAILABLE"));

        mvc.perform(put("/ai/credential")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "provider", "GEMINI_FLASH",
                                "apiKey", "AIzaPersonalGeminiCredential123456789"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalCredentialConfigured").value(true))
                .andExpect(jsonPath("$.personalCredentialSuffix").value("6789"))
                .andExpect(jsonPath("$.selection").value("PERSONAL_KEY"));

        if (hasOpenAiApiKey()) {
            String openAiApiKey = openAiApiKey();
            mvc.perform(put("/ai/credential")
                            .with(csrf())
                            .with(user(username).roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "provider", "GPT_5_NANO",
                                    "apiKey", openAiApiKey
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.provider").value("openai"))
                    .andExpect(jsonPath("$.model").value("gpt-5-nano"))
                    .andExpect(jsonPath("$.providerType").value("GPT_5_NANO"))
                    .andExpect(jsonPath("$.personalCredentialConfigured").value(true))
                    .andExpect(jsonPath("$.personalCredentialSuffix").value(openAiApiKey.substring(openAiApiKey.length() - 4)))
                    .andExpect(jsonPath("$.selection").value("PERSONAL_KEY"));
        }

        mvc.perform(delete("/ai/credential")
                        .with(csrf())
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalCredentialConfigured").value(false))
                .andExpect(jsonPath("$.selection").value("UNAVAILABLE"));
    }

    @Test
    void aiCredentialSelectionAllowsServerKeyForEntitledUsers() throws Exception {
        String username = createUser();

        mvc.perform(get("/ai/credential")
                        .with(user(username).authorities(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("AI_ENABLED")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverCredentialAvailable").value(true))
                .andExpect(jsonPath("$.serverCredentialAllowed").value(true))
                .andExpect(jsonPath("$.selection").value("SERVER_KEY"));
    }

    @Test
    void aiSummarizesLongCommentThreadAndMarksItStaleAfterNewComment() throws Exception {
        String username = createUser();
        String taskId = createTask(username);
        for (int index = 1; index <= 5; index++) {
            addComment(username, taskId, "summary comment")
                    .andExpect(status().isOk());
        }

        mvc.perform(post("/ai/task/comment-summary")
                        .with(csrf())
                        .with(user(username).authorities(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("AI_ENABLED")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("taskid", taskId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("AI summary of the comment thread."))
                .andExpect(jsonPath("$.summarizedCommentCount").value(5))
                .andExpect(jsonPath("$.currentCommentCount").value(5))
                .andExpect(jsonPath("$.stale").value(false));

        addComment(username, taskId, "newer comment")
                .andExpect(status().isOk());

        JsonNode task = findTask(username, taskId);
        Assertions.assertTrue(task.get("commentSummary").get("stale").asBoolean());
        Assertions.assertEquals(6, task.get("commentSummary").get("currentCommentCount").asInt());

    }

    @Test
    void aiRefreshRequiresPersonalCredentialAndTaskAssistanceReturnsStructuredSuggestions() throws Exception {
        String username = createUser();
        String taskId = createTask(username);
        for (int index = 1; index <= 5; index++) {
            addComment(username, taskId, "personal summary comment")
                    .andExpect(status().isOk());
        }

        mvc.perform(put("/ai/credential")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "provider", "GEMINI_FLASH",
                                "apiKey", "AIzaPersonalGeminiCredential123456789"
                        ))))
                .andExpect(status().isOk());

        mvc.perform(post("/ai/task/comment-summary/refresh")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("taskid", taskId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("AI summary of the comment thread."))
                .andExpect(jsonPath("$.credentialSource").value("PERSONAL_KEY"));

        mvc.perform(post("/ai/task/improve")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "rough title",
                                "description", "rough description"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Improved task title"))
                .andExpect(jsonPath("$.description").value("Improved task description."));

        mvc.perform(post("/ai/task/subtasks")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "rough title",
                                "description", "rough description"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtasks[0]").value("First subtask"));

        mvc.perform(post("/ai/task/ocr-structure")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("rawText", "OCR raw text"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("OCR task title"))
                .andExpect(jsonPath("$.description").value("Structured from OCR text."));
    }

    @Test
    void userProfileIncludesSettingsAiCredentialAndSecurityEndpoints() throws Exception {
        String username = createUser();

        mvc.perform(get("/user/profile")
                        .with(user(username).authorities(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("AI_ENABLED")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.settings.summaryThresholdComments").value(5))
                .andExpect(jsonPath("$.settings.aiCredential.selection").value("SERVER_KEY"))
                .andExpect(jsonPath("$.authentication.loginUrl").value("/login"))
                .andExpect(jsonPath("$.authentication.logoutUrl").value("/logout"))
                .andExpect(jsonPath("$.authentication.changePasswordUrl").value("/user/password"));
    }

    @Test
    void userCanReplaceSettingsAndLookupUsersForAssignment() throws Exception {
        String firstUsername = createUser();
        String secondUsername = createUser();

        mvc.perform(put("/user/settings")
                        .with(csrf())
                        .with(user(firstUsername).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("summaryThresholdComments", 7))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summaryThresholdComments").value(7));

        mvc.perform(get("/user/settings")
                .with(user(firstUsername).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summaryThresholdComments").value(7));

        mvc.perform(get("/users/lookup")
                        .queryParam("query", secondUsername)
                        .with(user(firstUsername).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(secondUsername));
    }

    @Test
    void userCanChangePasswordAfterCurrentPasswordVerification() throws Exception {
        String username = createUser();

        mvc.perform(put("/user/password")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "123456",
                                "newPassword", "new-password-123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mvc.perform(formLogin().user(username).password("new-password-123"))
                .andExpect(authenticated().withUsername(username));
    }

    @Test
    void configuredDemoSessionCookieAuthenticatesAndRefreshesExpiration() throws Exception {
        registerIfNeeded("demo_session_user");

        mvc.perform(get("/me")
                        .cookie(new Cookie("JSESSIONID", "DEMOSESSIONID1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo_session_user"))
                .andExpect(result -> Assertions.assertTrue(
                        result.getResponse().getHeaders("Set-Cookie").stream()
                                .anyMatch(header -> header.contains("JSESSIONID=DEMOSESSIONID1234567890")
                                        && header.contains("Max-Age="))
                ));
    }

    @Test
    void linkPreviewAcceptsBareDomainPresentInTaskTextWithTrailingPunctuation() throws Exception {
        String username = createUser();
        String taskId = createTask(
                username,
                "Bare link preview",
                "Read securefromscratch.com."
        );

        mvc.perform(post("/link-preview/task/delete")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "taskid", taskId,
                                "url", "securefromscratch.com"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void entitledUsersCanExportAndImport() throws Exception {
        String username = createUser();
        createTask(username);

        mvc.perform(get("/extra/export")
                        .with(user(username).authorities(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("EXPORT_ENABLED")
                        )))
                .andExpect(status().isOk());

        MockMultipartFile importFile = new MockMultipartFile(
                "file",
                "tasks.ser",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                json(List.of(Map.of(
                        "name", "Imported task",
                        "desc", "Imported description",
                        "dueDate", "2099-01-01",
                        "dueTime", "09:00",
                        "responsibilityOf", List.of(username)
                ))).getBytes(StandardCharsets.UTF_8)
        );

        mvc.perform(multipart("/extra/import")
                        .file(importFile)
                        .with(csrf())
                        .with(user(username).authorities(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("IMPORT_ENABLED")
                        )))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions register(String username) throws Exception {
        return mvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "username", username,
                        "password", "123456"
                ))));
    }

    private String createUser() throws Exception {
        String username = uniqueUsername();
        register(username)
                .andExpect(status().isOk());
        return username;
    }

    private String createTask(String username) throws Exception {
        return createTask(
                username,
                "Integration task",
                "A task created by the server test."
        );
    }

    private String createTask(String username, String name, String description) throws Exception {
        MvcResult result = mvc.perform(post("/create")
                        .with(csrf())
                        .with(user(username).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", name,
                                "desc", description,
                                "dueDate", "2099-01-01",
                                "dueTime", "09:00",
                                "responsibilityOf", List.of(username)
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return jsonTree(result).get("taskid").asText();
    }

    private org.springframework.test.web.servlet.ResultActions addComment(String username, String taskId, String text) throws Exception {
        MockMultipartFile fields = jsonPart("commentFields", commentFields(text, taskId));
        return mvc.perform(multipart("/comment")
                .file(fields)
                .with(csrf())
                .with(user(username).roles("USER")));
    }

    private String commentWithImage(String username, String taskId) throws Exception {
        MockMultipartFile fields = jsonPart("commentFields", commentFields("image comment", taskId));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4, 5, 6}
        );
        MvcResult result = mvc.perform(multipart("/comment")
                        .file(fields)
                        .file(file)
                        .with(csrf())
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        return jsonTree(result).get("commentid").asText();
    }

    private String commentWithSvgImage(String username, String taskId) throws Exception {
        MockMultipartFile fields = jsonPart("commentFields", commentFields("svg image comment", taskId));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagram.svg",
                "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 10 10\"><circle cx=\"5\" cy=\"5\" r=\"4\"/></svg>".getBytes(StandardCharsets.UTF_8)
        );
        MvcResult result = mvc.perform(multipart("/comment")
                        .file(fields)
                        .file(file)
                        .with(csrf())
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        return jsonTree(result).get("commentid").asText();
    }

    private String commentWithAttachment(String username, String taskId) throws Exception {
        MockMultipartFile fields = jsonPart("commentFields", commentFields("attachment comment", taskId));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "meeting-notes.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "notes from the meeting".getBytes(StandardCharsets.UTF_8)
        );
        MvcResult result = mvc.perform(multipart("/comment")
                        .file(fields)
                        .file(file)
                        .with(csrf())
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        return jsonTree(result).get("commentid").asText();
    }

    private ObjectNode commentFields(String text, String taskId) {
        ObjectNode fields = objectMapper.createObjectNode();
        fields.put("text", text);
        fields.put("taskid", taskId);
        fields.putNull("commentid");
        return fields;
    }

    private String findCommentImage(String commentId) throws Exception {
        JsonNode comment = findComment(commentId);
        return comment.get("image").asText();
    }

    private JsonNode findComment(String commentId) throws Exception {
        JsonNode tasksNode = jsonTree(mvc.perform(get("/tasks")
                        .with(user("reader").roles("USER")))
                .andExpect(status().isOk())
                .andReturn());
        for (JsonNode task : tasksNode) {
            for (JsonNode comment : task.get("comments")) {
                if (commentId.equals(comment.get("commentid").asText())) {
                    return comment;
                }
            }
        }
        Assertions.fail("Created comment was not found.");
        return objectMapper.createObjectNode();
    }

    private JsonNode findTask(String username, String taskId) throws Exception {
        JsonNode tasksNode = jsonTree(mvc.perform(get("/tasks")
                        .with(user(username).roles("USER")))
                .andExpect(status().isOk())
                .andReturn());
        for (JsonNode task : tasksNode) {
            if (taskId.equals(task.get("taskid").asText())) {
                return task;
            }
        }
        Assertions.fail("Created task was not found.");
        return objectMapper.createObjectNode();
    }

    private MockMultipartFile jsonPart(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name,
                "blob",
                MediaType.APPLICATION_JSON_VALUE,
                json(value).getBytes(StandardCharsets.UTF_8)
        );
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode jsonTree(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static String uniqueUsername() {
        return UUID.randomUUID().toString();
    }

    private static String openAiApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        // ALLOW NULL LITERAL:
        // old api: getenv could return null if the environment variable is not set, and we want to allow that for local development without a real key.
        if (apiKey == null || apiKey.isBlank()) {
            return FALLBAK_OPENAI_API_KEY;
        }

        return apiKey;
    }

    private static boolean hasOpenAiApiKey() {
        return !FALLBAK_OPENAI_API_KEY.equals(openAiApiKey());
    }

    private void registerIfNeeded(String username) throws Exception {
        register(username)
                .andExpect(result -> Assertions.assertTrue(
                        result.getResponse().getStatus() == 200
                                || result.getResponse().getStatus() == 400
                ));
    }
}
