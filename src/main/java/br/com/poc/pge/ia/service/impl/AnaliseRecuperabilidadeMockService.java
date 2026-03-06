package br.com.poc.pge.ia.service.impl;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.poc.pge.ia.dto.AnaliseIARecuperabilidadeDTO;
import br.com.poc.pge.ia.service.AnaliseRecuperabilidadeService;

@Service
public class AnaliseRecuperabilidadeMockService implements AnaliseRecuperabilidadeService {

    public List<AnaliseIARecuperabilidadeDTO> buscarPorCnpjBaseBasico(String cnpjBase) {

        List<AnaliseIARecuperabilidadeDTO> lista = new ArrayList<>();

        lista.add(new AnaliseIARecuperabilidadeDTO(
                cnpjBase,
                cnpjBase + "0001",
                "ATIVA",
                "NAO_AJUIZADO",
                "LUCRO_REAL",
                1L,
                "ICMS",
                15L,
                new BigDecimal("350000.00"),
                "ATIVA",
                "EM_ANALISE",
                10L,
                new BigDecimal("200000.00"),
                new BigDecimal("150000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("180000.00"),
                "Empresa Matriz LTDA"
        ));

        lista.add(new AnaliseIARecuperabilidadeDTO(
                cnpjBase,
                cnpjBase + "0002",
                "ATIVA",
                "AJUIZADO",
                "SIMPLES_NACIONAL",
                2L,
                "IPVA",
                15L,
                new BigDecimal("350000.00"),
                "ATIVA",
                "EM_ANALISE",
                5L,
                new BigDecimal("150000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("130000.00"),
                "Empresa Filial 01 LTDA"
        ));

        return lista;
    }
    
    @Override
    public List<AnaliseIARecuperabilidadeDTO> buscarPorCnpjBase(String cnpjBase) {

        String jsonMock = carregarJsonMock(); // pode vir de arquivo ou string

        List<AnaliseIARecuperabilidadeDTO> lista =
                converterJsonParaLista(jsonMock);

        return lista.stream()
                .filter(dto -> cnpjBase.equals(dto.getCnpjBase()))
                .toList();
    }
    
    public List<AnaliseIARecuperabilidadeDTO> converterJsonParaLista(String json) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(
                    json,
                    new TypeReference<List<AnaliseIARecuperabilidadeDTO>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter JSON para DTO", e);
        }
    }
    
    private String carregarJsonMock() {

        try {
            ClassPathResource resource =
                    new ClassPathResource("mock/analise-recuperabilidade.json");

            try (InputStream inputStream = resource.getInputStream()) {

                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar JSON mock", e);
        }
    }

    
    
    
}