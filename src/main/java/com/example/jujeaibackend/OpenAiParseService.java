package com.example.jujeaibackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;

@Service
public class OpenAiParseService {

    private final OpenAIClient client;
    private final ObjectMapper objectMapper;

    public OpenAiParseService() {
        this.client = OpenAIOkHttpClient.fromEnv();
        this.objectMapper = new ObjectMapper();
    }

    public AiParseResult parseUserInput(String userInput) {
        try {
            String prompt = """
                You are extracting structured shopping intent for a baby/toddler recommendation tool.

                Return ONLY valid JSON in this exact shape:
                {
                  "primary_intent": "travel | beach_pool | outdoor_day | feeding | diapering | bath | play_development | sleep",
                  "secondary_intents": ["..."],
                  "known_info": {
                    "age_groups": ["0-6 months" | "6-12 months" | "1-2 years" | "2-4 years"],
                    "feeding_mode": "breastfeeding | formula | solids | combination | unknown",
                    "location": "home | outdoor | travel | beach | pool | unknown",
                    "transport": "flight | car | train | mixed | unknown"
                  },
                  "candidate_tags": ["..."],
                  "confidence": "low | medium | high"
                }

                IMPORTANT:
                Only use tags from this list:
                
                baby, toddler, kids, newborn,
                feeding, snacks, meal_prep, diaper, changing, sleep, play, activity, bath, organization, storage,
                travel, flight, car, beach, pool, outdoor, home, on_the_go,
                portable, quiet, no_mess, easy_clean, comfort, self_feeding, learning
                
                Do not invent new tags.
                Do not return phrases like "beach essentials".

                Rules:
                - Use only the allowed intents.
                - If something is not explicitly known, return "unknown".
                - Keep candidate_tags short and useful for product matching.
                - Return JSON only.
                - Do not add markdown.
                - Do not explain anything.

                User input:
                "%s"
                """.formatted(userInput);

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(ChatModel.GPT_5_2)
                    .input(prompt)
                    .build();

            Response response = client.responses().create(params);

            String raw = response.toString();

            // מושך את הטקסט מתוך outputText=... כי זה מה שראינו בפועל ב-SDK אצלך
            String marker = "outputText=ResponseOutputText{annotations=[], text=";
            int start = raw.indexOf(marker);
            if (start == -1) {
                throw new RuntimeException("Could not find AI text in response");
            }

            start += marker.length();
            int end = raw.indexOf(", type=output_text", start);
            if (end == -1) {
                throw new RuntimeException("Could not find end of AI text in response");
            }

            String jsonText = raw.substring(start, end).trim();

            return objectMapper.readValue(jsonText, AiParseResult.class);

        } catch (Exception e) {
            throw new RuntimeException("AI parsing failed", e);
        }
    }
}