package io.camunda.demo.process_order;


import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OllamaWorker {

    private final RestTemplate restTemplate = new RestTemplate();

    @JobWorker(type = "ollama-task", autoComplete = false)
    public void handleJob(JobClient client, ActivatedJob job) {

        // 1️⃣ Get "data" variable safely
        String data = (String) job.getVariablesAsMap().getOrDefault("data", "");

        // 2️⃣ Prepare request
        Map<String, Object> request = Map.of(
                "model", "mistral",
                 "prompt", data +
                        "Task: Write a compelling social media caption in EXACTLY 100 words based on the following quote.\n" +
                        "Quote: \"%s\"\n\n" +
                        "Rules:\n" +
                        "- Must be exactly 100 words (hashtags not counted)\n" +
                        "- Use an engaging and emotional tone\n" +
                        "- Suitable for social media platforms\n" +
                        "- Do NOT repeat the quote verbatim\n" +
                        "- End with a strong, powerful closing line\n" +
                        "- Include 10 to 15 relevant trending hashtags at the end\n" +
                        "- Hashtags must relate to the quote topic and theme\n" +
                        "- Include 3 to 5 emojis naturally in the caption to enhance emotions and engagement",
                "stream", false
        );

        // 3️⃣ Call Ollama API
        Map response = restTemplate.postForObject(
                "http://localhost:11434/api/generate",
                request,
                Map.class
        );

        String aiResponse = "";
        if (response != null && response.get("response") != null) {
            aiResponse = ((String) response.get("response")).trim().replaceAll("^\"|\"$", "");
        }

        System.out.println("Result: " + aiResponse);

        try {
            Thread.sleep(5000); // 5000ms = 5 seconds, adjust as needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Result: " + aiResponse);

        // 4️⃣ Complete the job
        client.newCompleteCommand(job.getKey())
                .variable("aiResponse", aiResponse)
                .send();



    }

}