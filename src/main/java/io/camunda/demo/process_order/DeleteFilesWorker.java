package io.camunda.demo.process_order;

import io.camunda.client.CamundaClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import org.springframework.stereotype.Component;

import java.io.File;
@Component
public class DeleteFilesWorker {

    private static final String FOLDER_PATH = "E:\\ai\\DownloadReels";

    private final CamundaClient client;

    public DeleteFilesWorker(CamundaClient client) {
        this.client = client;
    }

    @JobWorker(type = "delete-files", autoComplete = false)
    public void deleteFiles(ActivatedJob job) {  // Only ActivatedJob here

        try {

            File folder = new File(FOLDER_PATH);

            if (!folder.exists() || !folder.isDirectory()) {
                throw new RuntimeException("Folder does not exist: " + FOLDER_PATH);
            }

            File[] files = folder.listFiles();

            if (files != null && files.length > 0) {

                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();

                        if (deleted) {
                            System.out.println("Deleted: " + file.getName());
                        } else {
                            System.out.println("Failed to delete: " + file.getName());
                        }
                    }
                }

            } else {
                System.out.println("No files found in folder.");
            }

            // complete Camunda job
            client.newCompleteCommand(job.getKey())
                    .send()
                    .join();

        } catch (Exception e) {

            e.printStackTrace();

            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}