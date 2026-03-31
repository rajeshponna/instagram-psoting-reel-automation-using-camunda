package io.camunda.demo.process_order;

import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MonitorFileWorker {

    private static final String FOLDER_PATH = "E:\\ai\\DownloadReels";
    private static final int POLL_INTERVAL_MS = 5000; // 5 seconds between checks

    @JobWorker(type = "monitor-file", autoComplete = false)
    public void monitorFolder(JobClient jobClient, ActivatedJob job) {

        try {
            File folder = new File(FOLDER_PATH);

            if (!folder.exists() || !folder.isDirectory()) {
                throw new RuntimeException("Folder not found: " + FOLDER_PATH);
            }


            System.out.println("Waiting for file in folder: " + FOLDER_PATH);

            // Loop until at least one file appears
            while (true) {
                File[] files = folder.listFiles();

                if (files != null && files.length > 0) {
                    System.out.println("File detected! Completing job...");
                    jobClient.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                    break; // exit the loop after completing the job
                }

                // No file found, wait before retrying
                System.out.println("No file found, waiting...");
                Thread.sleep(POLL_INTERVAL_MS);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                jobClient.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage(e.getMessage())
                        .send()
                        .join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}