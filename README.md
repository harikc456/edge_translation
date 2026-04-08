# Edge Translation: Any-to-Any Audio Translation on Android

This project implements a fully on-device, any-to-any audio translation pipeline for Android. It leverages **LiteRT** (formerly TensorFlow Lite) to serve three state-of-the-art models in a modular pipeline.

## 🚀 Pipeline Architecture
1.  **Speech-to-Text (STT):** **Whisper Tiny** converts input audio (16kHz mono) into text.
2.  **Translation:** **Gemma 4 E2B** (Effective 2 Billion) translates the source text into the target language using the **LiteRT-LM** SDK.
3.  **Text-to-Speech (TTS):** **MMS-TTS** (Massively Multilingual Speech) synthesizes the translated text back into high-quality audio.

---

## 🛠️ Prerequisites
- **Python 3.10+**: For model conversion scripts.
- **Android Studio Jellyfish+**: To build the Kotlin application.
- **Physical Android Device**: Android 13+ (API 33+) with at least 6GB RAM (8GB+ recommended for Gemma 4).
- **ADB (Android Debug Bridge)**: To sideload large model files.

---

## 📦 Step 1: Model Preparation (Python)
Before running the app, you must convert the models from PyTorch to LiteRT format.

1.  **Install Dependencies**:
    ```bash
    pip install -r scripts/requirements.txt
    ```
2.  **Convert Whisper Tiny**:
    ```bash
    python scripts/convert_whisper.py
    ```
3.  **Convert MMS-TTS**:
    ```bash
    # This script generates models for English (eng) and Spanish (spa) by default
    python scripts/convert_mms.py
    ```
4.  **Download Gemma 4 E2B**:
    Download the `.litertlm` bundle for Gemma 4 E2B from the [Google AI Edge](https://huggingface.co/google/gemma-4-2b-it-litert) or Hugging Face repository.

---

## 📱 Step 2: Deploy Models to Device
Large models (Gemma 4 is ~2.5GB) should be sideloaded to the device's local storage to avoid bloated APK sizes and slow installation.

Connect your phone via USB and run:
```bash
# Push Whisper and MMS models
adb shell mkdir -p /data/local/tmp/models/
adb push models/*.tflite /data/local/tmp/models/

# Push Gemma 4 Model
adb push gemma-4-e2b.litertlm /data/local/tmp/
```

---

## 🏗️ Step 3: Build the Android App
1.  Open **Android Studio**.
2.  Select **Open** and navigate to the `android_app/` directory.
3.  Sync Gradle and ensure all dependencies (LiteRT-LM, TensorFlow Lite) are downloaded.
4.  **Build and Run** the app on your physical device.

---

## 🎤 How to Use
1.  **Grant Permissions**: On first launch, the app will request Microphone and Storage permissions.
2.  **Record**: Tap the **"Record"** button. The app will capture 5 seconds of audio.
3.  **Process**:
    - The status will change to **"Processing..."**.
    - **Whisper** transcribes your voice.
    - **Gemma 4** translates it (default: English to Spanish).
    - **MMS-TTS** synthesizes the Spanish audio.
4.  **Listen**: The translated audio will play automatically through the device speaker.

---

## ⚠️ Important Notes
- **Hardware Acceleration**: The app is configured to use the **GPU/NPU** via the LiteRT-LM backend for Gemma 4. If your device does not support this, you may need to switch the backend to `CPU` in `GemmaModel.kt`.
- **Model Path**: If you change the location of your sideloaded models, update the paths in `GemmaModel.kt` and `WhisperModel.kt`.
- **Latency**: First-time model initialization (Cold Start) may take 5–10 seconds. Subsequent translations will be significantly faster.

---

## 📜 License
This project uses models from OpenAI (Whisper), Google (Gemma), and Meta (MMS). Please refer to their respective licenses for redistribution and commercial use.
