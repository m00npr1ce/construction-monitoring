package com.systemcontrol.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DefectWorkflowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void completeDefectWorkflow_ShouldWorkCorrectly() throws Exception {
        // 1. Register a user
        String username = "workflow-test-" + UUID.randomUUID().toString().substring(0, 8);
        String registerJson = String.format("{\"username\":\"%s\",\"password\":\"testpass123\"}", username);
        
        ResponseEntity<String> regResp = restTemplate.postForEntity(
            baseUrl("/api/auth/register"), 
            new HttpEntity<>(registerJson, jsonHeaders()), 
            String.class
        );
        assertTrue(regResp.getStatusCode().is2xxSuccessful());

        // 2. Login and get token
        String loginJson = String.format("{\"username\":\"%s\",\"password\":\"testpass123\"}", username);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(
            baseUrl("/api/auth/login"),
            new HttpEntity<>(loginJson, jsonHeaders()),
            String.class
        );
        assertTrue(loginResp.getStatusCode().is2xxSuccessful());
        
        JsonNode loginNode = mapper.readTree(loginResp.getBody());
        String token = loginNode.get("token").asText();
        assertNotNull(token);

        // 3. Create a project using admin token
        // Login as admin
        String adminLoginJson = "{\"username\":\"admin\",\"password\":\"admin\"}";
        ResponseEntity<String> adminLoginResp = restTemplate.postForEntity(
            baseUrl("/api/auth/login"), new HttpEntity<>(adminLoginJson, jsonHeaders()), String.class);
        assertTrue(adminLoginResp.getStatusCode().is2xxSuccessful());
        String adminToken = mapper.readTree(adminLoginResp.getBody()).get("token").asText();

        HttpHeaders authHeaders = jsonHeaders();
        authHeaders.set("Authorization", "Bearer " + adminToken);
        
        String projectJson = "{\"name\":\"Integration Test Project\",\"description\":\"Test project for integration testing\"}";
        ResponseEntity<String> projectResp = restTemplate.postForEntity(
            baseUrl("/api/projects"),
            new HttpEntity<>(projectJson, authHeaders),
            String.class
        );
        assertEquals(HttpStatus.CREATED, projectResp.getStatusCode());
        
        JsonNode projectNode = mapper.readTree(projectResp.getBody());
        long projectId = projectNode.get("id").asLong();
        assertTrue(projectId > 0);

        // 4. Create a defect
        String defectJson = String.format(
            "{\"title\":\"Integration Test Defect\",\"description\":\"Test defect description\",\"projectId\":%d,\"priority\":\"HIGH\",\"status\":\"NEW\"}",
            projectId
        );
        ResponseEntity<String> defectResp = restTemplate.postForEntity(
            baseUrl("/api/defects"),
            new HttpEntity<>(defectJson, authHeaders),
            String.class
        );
        assertEquals(HttpStatus.CREATED, defectResp.getStatusCode());
        
        JsonNode defectNode = mapper.readTree(defectResp.getBody());
        long defectId = defectNode.get("id").asLong();
        assertEquals("Integration Test Defect", defectNode.get("title").asText());
        assertEquals("HIGH", defectNode.get("priority").asText());
        assertEquals("NEW", defectNode.get("status").asText());

        // 5. Update defect status
        String updateJson = String.format(
            "{\"title\":\"Integration Test Defect\",\"description\":\"Test defect description\",\"projectId\":%d,\"priority\":\"HIGH\",\"status\":\"IN_PROGRESS\"}",
            projectId
        );
        ResponseEntity<String> updateResp = restTemplate.exchange(
            baseUrl("/api/defects/" + defectId),
            HttpMethod.PUT,
            new HttpEntity<>(updateJson, authHeaders),
            String.class
        );
        assertEquals(HttpStatus.OK, updateResp.getStatusCode());

        // 6. Get defect to verify update
        ResponseEntity<String> getResp = restTemplate.exchange(
            baseUrl("/api/defects/" + defectId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders),
            String.class
        );
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        
        JsonNode getNode = mapper.readTree(getResp.getBody());
        assertEquals("IN_PROGRESS", getNode.get("status").asText());

        // 7. Get analytics
        ResponseEntity<String> analyticsResp = restTemplate.exchange(
            baseUrl("/api/reports/analytics"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders),
            String.class
        );
        assertEquals(HttpStatus.OK, analyticsResp.getStatusCode());
        
        JsonNode analyticsNode = mapper.readTree(analyticsResp.getBody());
        assertTrue(analyticsNode.has("totalDefects"));
        assertTrue(analyticsNode.has("statusDistribution"));
    }

    @Test
    void defectWorkflow_WithInvalidData_ShouldHandleErrors() throws Exception {
        // Test creating defect with non-existent project
        String username = "error-test-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Register and login
        String registerJson = String.format("{\"username\":\"%s\",\"password\":\"testpass123\"}", username);
        restTemplate.postForEntity(baseUrl("/api/auth/register"), new HttpEntity<>(registerJson, jsonHeaders()), String.class);
        
        String loginJson = String.format("{\"username\":\"%s\",\"password\":\"testpass123\"}", username);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(baseUrl("/api/auth/login"), new HttpEntity<>(loginJson, jsonHeaders()), String.class);
        
        JsonNode loginNode = mapper.readTree(loginResp.getBody());
        String token = loginNode.get("token").asText();
        
        HttpHeaders authHeaders = jsonHeaders();
        authHeaders.set("Authorization", "Bearer " + token);
        
        // Try to create defect with non-existent project
        String badDefectJson = "{\"title\":\"Bad Defect\",\"description\":\"No project\",\"projectId\":999999}";
        ResponseEntity<String> badResp = restTemplate.postForEntity(
            baseUrl("/api/defects"),
            new HttpEntity<>(badDefectJson, authHeaders),
            String.class
        );
        // Since regular user has no role/privileges to create defects, expect 403
        assertEquals(HttpStatus.FORBIDDEN, badResp.getStatusCode());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}




