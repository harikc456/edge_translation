import os
import ai_edge_torch
from ai_edge_torch.generative import export_hf
import torch

# Configuration
# "google/gemma-4-2b-it" is the likely model ID for the instruct variant
model_id = "google/gemma-4-E2B-it" 
output_path = "models/gemma4_e2b.tflite"

def convert_gemma():
    print(f"Starting conversion for {model_id}...")
    print("This may take 15-30 minutes depending on your hardware and connection.")

    # Create the models directory
    os.makedirs("models", exist_ok=True)

    # export_hf is the high-level API to convert Hugging Face PyTorch models
    # to optimized LiteRT (TFLite) models.
    try:
        export_hf(
            model_id,
            output_path,
            # prefill_lengths: multiple lengths to optimize for different prompts
            prefill_lengths=[128, 256, 512, 1024],
            # cache_length: max tokens (prefill + generated)
            cache_length=2048,
            # quantization_recipe: 4-bit weights (best for mobile/E2B)
            quantization_recipe="dynamic_wi4_afp32",
            # externalize_embedder: reduces memory footprint of the main graph
            externalize_embedder=True
        )
        print(f"✅ Success! Gemma 4 E2B exported to {output_path}")
    except Exception as e:
        print(f"❌ Error during conversion: {e}")

if __name__ == "__main__":
    convert_gemma()
