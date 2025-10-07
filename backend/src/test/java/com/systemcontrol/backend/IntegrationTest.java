package com.systemcontrol.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private String baseUrl(String path) { return "http://localhost:" + port + path; }

    @Test
    void fullFlow_register_login_createProject_createDefect() throws Exception {
    // register (use unique username to avoid conflicts with existing DB state)
    String username = "itestuser-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    String registerJson = String.format("{\"username\":\"%s\",\"password\":\"pass123\",\"role\":\"ROLE_ENGINEER\"}", username);
    ResponseEntity<String> regResp = restTemplate.postForEntity(baseUrl("/api/auth/register"), new HttpEntity<>(registerJson, jsonHeaders()), String.class);
        Assertions.assertTrue(regResp.getStatusCode().is2xxSuccessful());

        // login
    String loginJson = String.format("{\"username\":\"%s\",\"password\":\"pass123\"}", username);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(baseUrl("/api/auth/login"),
                new HttpEntity<>(loginJson, jsonHeaders()), String.class);
        Assertions.assertTrue(loginResp.getStatusCode().is2xxSuccessful());
        JsonNode node = mapper.readTree(loginResp.getBody());
        String token = node.get("token").asText();

        // create project
        String projectJson = "{\"name\":\"IT Test Project\",\"description\":\"integration test\"}";
        HttpHeaders auth = jsonHeaders(); auth.set("Authorization", "Bearer " + token);
        ResponseEntity<String> projResp = restTemplate.postForEntity(baseUrl("/api/projects"), new HttpEntity<>(projectJson, auth), String.class);
        Assertions.assertEquals(HttpStatus.CREATED, projResp.getStatusCode());
        JsonNode projNode = mapper.readTree(projResp.getBody());
        long projectId = projNode.get("id").asLong();

        // create defect
        String defectJson = String.format("{\"title\":\"IT Defect\",\"description\":\"desc\",\"projectId\":%d}", projectId);
        ResponseEntity<String> defectResp = restTemplate.postForEntity(baseUrl("/api/defects"), new HttpEntity<>(defectJson, auth), String.class);
        Assertions.assertEquals(HttpStatus.CREATED, defectResp.getStatusCode());
        JsonNode defectNode = mapper.readTree(defectResp.getBody());
        Assertions.assertEquals("IT Defect", defectNode.get("title").asText());
        Assertions.assertEquals(projectId, defectNode.get("projectId").asLong());

    // negative: creating defect with non-existent project -> 400
    String badDefectJson = "{\"title\":\"Bad\",\"description\":\"no project\",\"projectId\":999999}";
    ResponseEntity<String> badResp = restTemplate.postForEntity(baseUrl("/api/defects"), new HttpEntity<>(badDefectJson, auth), String.class);
    // debug: print status and body to help diagnose why we're getting a 403 instead of 400
    System.out.println("BAD DEFECT RESPONSE STATUS = " + badResp.getStatusCode());
    System.out.println("BAD DEFECT RESPONSE BODY = " + badResp.getBody());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, badResp.getStatusCode());
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
