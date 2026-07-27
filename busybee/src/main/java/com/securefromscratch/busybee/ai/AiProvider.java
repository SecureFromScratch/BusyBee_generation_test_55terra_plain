package com.securefromscratch.busybee.ai;

enum AiProvider {
    GEMINI_FLASH("gemini", "gemini-2.5-flash-lite"),
    GPT_5_NANO("openai", "gpt-5-nano");

    private final String provider;
    private final String model;

    AiProvider(String provider, String model) {
        this.provider = provider;
        this.model = model;
    }

    String provider() {
        return provider;
    }

    String model() {
        return model;
    }
}
