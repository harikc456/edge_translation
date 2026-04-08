import os
import torch
from transformers import WhisperProcessor, WhisperForConditionalGeneration
import tensorflow as tf
import numpy as np

# Load Whisper Tiny
model_name = "openai/whisper-tiny"
processor = WhisperProcessor.from_pretrained(model_name)
model = WhisperForConditionalGeneration.from_pretrained(model_name)
model.eval()

def convert_encoder():
    print("Converting Whisper Encoder...")
    class WhisperEncoder(torch.nn.Module):
        def __init__(self, model):
            super().__init__()
            self.encoder = model.model.encoder
        def forward(self, input_features):
            return self.encoder(input_features).last_hidden_state

    encoder = WhisperEncoder(model)
    # Whisper Tiny input: [1, 80, 3000] (80 mel bins, 3000 frames for 30s)
    dummy_input = torch.randn(1, 80, 3000)
    
    # Use ai-edge-torch (Google's new Pytorch-to-LiteRT converter)
    import ai_edge_torch
    edge_model = ai_edge_torch.convert(encoder, (dummy_input,))
    edge_model.export("models/whisper_tiny_encoder.tflite")
    print("Encoder exported to models/whisper_tiny_encoder.tflite")

def convert_decoder():
    print("Converting Whisper Decoder...")
    # The decoder is more complex due to its autoregressive nature.
    # For a simple implementation, we'll export a fixed-length decoder step.
    class WhisperDecoder(torch.nn.Module):
        def __init__(self, model):
            super().__init__()
            self.decoder = model.model.decoder
            self.proj_out = model.proj_out
        def forward(self, input_ids, encoder_hidden_states):
            outputs = self.decoder(input_ids, encoder_hidden_states=encoder_hidden_states)
            logits = self.proj_out(outputs.last_hidden_state)
            return logits

    decoder = WhisperDecoder(model)
    dummy_input_ids = torch.ones((1, 1), dtype=torch.long)
    dummy_hidden_states = torch.randn(1, 1500, 384) # 1500 = 3000/2, 384 = tiny d_model
    
    edge_model = ai_edge_torch.convert(decoder, (dummy_input_ids, dummy_hidden_states))
    edge_model.export("models/whisper_tiny_decoder.tflite")
    print("Decoder exported to models/whisper_tiny_decoder.tflite")

if __name__ == "__main__":
    os.makedirs("models", exist_ok=True)
    convert_encoder()
    convert_decoder()
    # Save vocab for Android
    processor.tokenizer.save_pretrained("models/whisper_tokenizer")
