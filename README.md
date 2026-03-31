

# 🚀 Instagram Reel Automation using Camunda 8 + UiPath + AI (Ollama)

## 📌 Project Overview

This project automates the complete process of creating and posting Instagram reels using:

* **Camunda 8 (Zeebe)** → Workflow orchestration
* **Ollama (Mistral AI model)** → Content generation
* **UiPath (RPA)** → Canva automation + Instagram posting
* **Email Trigger** → Starts the automation

👉 No manual work required after sending an email.

---

## ⚙️ How It Works (Flow)

1. 📩 **Receive Email**

   * Process starts when an email is received
   * Extracts content from email body

2. 🧠 **Generate AI Content**

   * Sends prompt to local AI model (Ollama)
   * AI generates quote/content for reel

3. 🧹 **Extract Data**

   * Cleans AI response (removes unwanted characters)

4. 🎬 **Create Reel using Canva (UiPath)**

   * Sends data to UiPath queue
   * UiPath bot:

     * Opens Canva
     * Creates reel
     * Downloads video

5. 🏷️ **Generate Tags & Caption**

   * AI generates hashtags and captions

6. ⏳ **Wait for Reel Download**

   * Monitors system for downloaded reel file

7. 📤 **Post Reel on Instagram (UiPath)**

   * Sends reel + caption to UiPath queue
   * UiPath bot uploads reel to Instagram

8. ⏱️ **Delay (2 Minutes)**

   * Timer event before ending process

9. 🧹 **Cleanup**

   * Deletes temporary files

---

## 🏗️ Architecture

```
Email → Camunda → AI (Ollama)
              ↓
          UiPath (Canva)
              ↓
          File Monitor
              ↓
          UiPath (Instagram)
```

---

## 🧰 Technologies Used

* Camunda 8 (Zeebe Workflow Engine)
* BPMN (Process Modeling)
* Ollama (Local AI Model - Mistral)
* UiPath Orchestrator (Queues & RPA)
* Canva (via Automation)
* Instagram (via Automation)
* IMAP (Email Trigger)

---

## 🔐 Important Note (Security)

⚠️ The current BPMN file contains sensitive data:

* Email credentials
* UiPath Client Secret

👉 **Do NOT expose in public repositories**

### Recommended Fix:

* Use environment variables
* Use Camunda secrets
* Store credentials securely

---

## 📂 Key BPMN Tasks

| Task Name           | Description                  |
| ------------------- | ---------------------------- |
| Receive Email       | Starts workflow              |
| Extract Prompt      | Gets content from email      |
| Connect to AI Model | Generates reel content       |
| Extract Data        | Cleans AI response           |
| Create Reel         | UiPath creates reel in Canva |
| Generate Caption    | AI generates tags            |
| Monitor File        | Waits for reel download      |
| Post Instagram      | Uploads reel                 |
| Timer Event         | Delay before end             |
| Cleanup             | Deletes files                |

---

## ▶️ How to Run

1. Start Camunda 8
2. Deploy BPMN process
3. Ensure:

   * Ollama is running locally
   * UiPath Orchestrator is configured
   * Email connector is active
4. Send an email with content

🎉 Automation will run end-to-end

---

## 💡 Features

* ✅ Fully automated reel creation
* ✅ No human interaction required
* ✅ Uses FREE local AI (Ollama)
* ✅ End-to-end workflow orchestration
* ✅ Scalable using Camunda

---

## 🚀 Future Improvements

* Add error handling & retries
* Add logging & monitoring
* Support multiple social platforms
* Improve AI prompts for better content
* Add UI dashboard

---

## 🙌 Author

**Rajesh Ponna**

---
