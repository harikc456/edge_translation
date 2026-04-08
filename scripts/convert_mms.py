import os
import torch
from transformers import VitsModel, VitsTokenizer
import ai_edge_torch

def convert_mms_tts(lang_code="eng"):
    print(f"Converting MMS-TTS for language: {lang_code}...")
    model_id = f"facebook/mms-tts-{lang_code}"
    model = VitsModel.from_pretrained(model_id)
    tokenizer = VitsTokenizer.from_pretrained(model_id)
    model.eval()

    class MMSTTSWrapper(torch.nn.Module):
        def __init__(self, model):
            super().__init__()
            self.model = model
        def forward(self, input_ids):
            # MMS-TTS (VITS) returns (waveform, lengths, etc)
            # We only need the waveform
            return self.model(input_ids).waveform

    wrapper = MMSTTSWrapper(model)
    # Dummy input for trace (length can be dynamic but we'll start fixed)
    dummy_input = torch.tensor([[1, 2, 3, 4, 5]], dtype=torch.long)
    
    edge_model = ai_edge_torch.convert(wrapper, (dummy_input,))
    edge_model.export(f"models/mms_tts_{lang_code}.tflite")
    print(f"MMS-TTS exported to models/mms_tts_{lang_code}.tflite")

if __name__ == "__main__":
    os.makedirs("models", exist_ok=True)
    # Example: Convert English and Spanish
    convert_mms_tts("eng")
    convert_mms_tts("spa")
