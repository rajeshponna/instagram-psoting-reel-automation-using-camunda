package io.camunda.demo.process_order;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.client.CamundaClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Component
public class OllamaJobWorker {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    @Autowired
    private CamundaClient camundaClient;  // ✅ Inject here, NOT as method param

    private final ObjectMapper objectMapper = new ObjectMapper();

    @JobWorker(type = "ollama-generate", autoComplete = false)
    public void generateText(ActivatedJob job) {  // ✅ Only ActivatedJob as param

        try {
            String prompt = (String) job.getVariablesAsMap().get("prompt");

            if (prompt == null || prompt.isBlank()) {
                camundaClient.newFailCommand(job.getKey())
                        .retries(0)
                        .errorMessage("Missing required variable: prompt")
                        .send()
                        .join();
                return;
            }

            // Safe JSON serialization
            String jsonInput = objectMapper.writeValueAsString(Map.of(
                    "model", "mistral",
                    "prompt", prompt + "Respond with exactly one quote, no explanation, no author, just the quote itself in one line.(With Format Constraint)" ,
                    "stream", false
            ));

            // Connect to Ollama
            URL url = new URL(OLLAMA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            // Check HTTP status
            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                String errorBody;
                try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                    errorBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "No error body";
                }
                camundaClient.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage("Ollama HTTP error: " + statusCode + " - " + errorBody)
                        .send()
                        .join();
                return;
            }

            // Read response
            String rawResponse;
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                rawResponse = scanner.useDelimiter("\\A").next();
            }

            // Extract only the "response" field
            JsonNode jsonNode = objectMapper.readTree(rawResponse);
            String quotes = jsonNode.path("response").asText();

            Map<String, Object> variables = new HashMap<>();
            variables.put("aiText", quotes);

            // Complete job
            camundaClient.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("Ollama response sent to Camunda: " + quotes);

        } catch (Exception e) {
            e.printStackTrace();
            camundaClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .send()
                    .join();
        }
    }
}