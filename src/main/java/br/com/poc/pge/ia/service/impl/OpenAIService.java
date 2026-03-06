package br.com.poc.pge.ia.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String perguntar(String pergunta) {

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", """
        Você é um analista fiscal especializado em recuperabilidade de crédito tributário.

        REGRAS OBRIGATÓRIAS:
        - Utilize exclusivamente os dados fornecidos.
        - Não consulte fontes externas.
        - Não mencione Receita Federal, Serasa ou consultas públicas.
        - Considere que os dados já foram extraídos do banco oficial.
        - Nunca diga que não pode acessar dados externos.
        """);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", pergunta);

        messages.add(system);
        messages.add(user);

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        List choices = (List) response.getBody().get("choices");
        Map choice = (Map) choices.get(0);
        Map message = (Map) choice.get("message");

        return message.get("content").toString();
    }
    
    
}