package br.com.poc.pge.ia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.poc.pge.ia.service.impl.AzureOpenAIService;
import br.com.poc.pge.ia.service.impl.OpenAIService;
import br.com.poc.pge.ia.service.impl.PerguntaOrquestradorService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final OpenAIService openAIService;

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    @Autowired
    private PerguntaOrquestradorService perguntaOrquestradorService;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Endpoint usando OpenAI
     */
    @GetMapping("/openai")
    public String perguntarOpenAI(@RequestParam String pergunta) {
        return openAIService.perguntar(pergunta);
    }

    /**
     * Endpoint usando Azure OpenAI
     */
    @GetMapping("/azure")
    public String perguntarAzure(@RequestParam String pergunta) {
        return azureOpenAIService.perguntar(pergunta);
    }

    /**
     * Endpoint usando o orquestrador (decide qual IA usar)
     */
    @GetMapping("/orquestrador")
    public String perguntarOrquestrador(@RequestParam String pergunta) {
        return perguntaOrquestradorService.processarPergunta(pergunta);
    }
}