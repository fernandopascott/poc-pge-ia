package br.com.poc.pge.ia.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.poc.pge.ia.dto.AnaliseIARecuperabilidadeDTO;
import br.com.poc.pge.ia.service.AnaliseRecuperabilidadeService;

@Service
public class PerguntaOrquestradorService {
	
	private static final String REGEX_CNPJ =
	        "\\b(\\d{8}|\\d{14}|\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2})\\b";
	
	private static final Set<String> VERBOS_BUSCA = Set.of(
			"informar",
			"informe",
			"qual",
	        "busque",
	        "buscar",
	        "procure",
	        "procurar",
	        "consulte",
	        "consultar",
	        "verifique",
	        "determine",
	        "analisar",
	        "analise"
	);

	@Autowired
    private OpenAIService openAIService;
	
	@Autowired
    private AzureOpenAIService azureOpenAIService;
	
	@Autowired
	private AnaliseRecuperabilidadeService analiseRecuperabilidadeService;
	
	public String processarPergunta(String pergunta) {

	    Optional<String> cnpjBaseOpt = extrairCnpj(pergunta);

	    if (cnpjBaseOpt.isPresent() && contemVerboBusca(pergunta)) {

	        String cnpjBase = cnpjBaseOpt.get();
	        List<AnaliseIARecuperabilidadeDTO> lista = analiseRecuperabilidadeService.buscarPorCnpjBase(cnpjBase);

	        if (lista != null && !lista.isEmpty()) {
	            String contexto = montarContextoEmpresas(lista);
	            String promptFinal = obterContextoEnxuto(cnpjBase, contexto);

	            // Escolha da IA baseada em regra simples: se houver CNPJ, Azure
	            return azureOpenAIService.perguntar(promptFinal);
	        }

	        return "CNPJ base não encontrado na base!";
	    }

	    // Pergunta geral → OpenAI
	    String termos = String.join(", ", VERBOS_BUSCA);

	    return openAIService.perguntar("Dentro de [], temos uma pergunta do usuário, responda o mesmo orientando que "
	            + "ele deve informar o numero do CNPJ base usando termos como: " + termos 
	            + ", e ele sempre deverá usar o termo Grau de Recuperabilidade. Dê um Exemplos ao usuario de uma pergunta padrão, seja objetivo usando frase curta."
	    );
	}

	private String obterContextoDetalhado(String cnpjBase, String contexto) {
		String promptFinal = """
				Você é um analista fiscal sênior especializado em recuperabilidade de crédito tributário.

				CONTEXTO OPERACIONAL:
				Os dados abaixo já foram extraídos do banco oficial.
				Eles são completos e suficientes para análise.
				É proibido assumir informações externas ou sugerir consultas adicionais.

				DADOS DO CONTRIBUINTE (CNPJ raiz %s):
				%s

				OBJETIVO:
				Avaliar o grau de recuperabilidade do crédito tributário com base exclusivamente nos dados apresentados.

				CRITÉRIOS TÉCNICOS OBRIGATÓRIOS:
				1) Volume total consolidado de débitos
				2) Distribuição e concentração dos débitos por filial
				3) Situação cadastral (ativa, inapta, baixada, etc.)
				4) Classificação tributária
				5) Existência de parcelamentos
				6) Situação de ajuizamento (executado, garantido, suspenso, etc.)
				7) Indícios de capacidade operacional

				METODOLOGIA DE ANÁLISE:
				- Avalie risco financeiro
				- Avalie probabilidade de adimplemento
				- Avalie complexidade jurídica
				- Considere impacto de concentração de débito
				- Considere histórico de regularização

				FORMATO OBRIGATÓRIO DA RESPOSTA:

				DIAGNÓSTICO TÉCNICO:
				(Análise objetiva dos dados apresentados)

				GRAU DE RECUPERABILIDADE:
				(ALTO, MÉDIO ou BAIXO)

				JUSTIFICATIVA FUNDAMENTADA:
				(Explique tecnicamente os fatores determinantes)

				CONCLUSÃO EXECUTIVA:
				(Resumo final claro e direto)
				""".formatted(cnpjBase, contexto);
		return promptFinal;
	}
	
	private String obterContextoEnxuto(String cnpjBase, String contexto) {
		String promptFinal = """
				Você é um analista fiscal sênior especializado em recuperabilidade de crédito tributário.

				Os dados abaixo são completos e suficientes.
				Não utilize informações externas.
				Não faça suposições adicionais.
				Seja técnico e objetivo.

				DADOS DO CONTRIBUINTE (CNPJ raiz %s):
				%s

				OBJETIVO:
				Determinar o grau de recuperabilidade.

				REGRAS DE RESPOSTA (OBRIGATÓRIAS):
				- Máximo de 12 linhas no total.
				- Parágrafos curtos.
				- Linguagem técnica e direta.
				- Sem listas numeradas.
				- Sem repetições.
				- Sem recomendações extensas.

				FORMATO OBRIGATÓRIO:

				DIAGNÓSTICO TÉCNICO:
				(Texto objetivo em até 4 linhas)

				GRAU DE RECUPERABILIDADE:
				(ALTO, MÉDIO ou BAIXO)

				JUSTIFICATIVA FUNDAMENTADA:
				(Resumo técnico em até 4 linhas)

				CONCLUSÃO EXECUTIVA:
				(Conclusão direta em até 3 linhas)
				""".formatted(cnpjBase, contexto);
		
		return promptFinal;
		
	}
	
	private String montarContextoEmpresas(List<AnaliseIARecuperabilidadeDTO> lista) {

	    if (lista == null || lista.isEmpty()) {
	        return "Nenhum dado encontrado.";
	    }

	    BigDecimal totalValor = lista.stream()
	            .map(AnaliseIARecuperabilidadeDTO::getValor)
	            .filter(Objects::nonNull)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    Long totalDebitos = lista.stream()
	            .map(AnaliseIARecuperabilidadeDTO::getQtdeDebitos)
	            .filter(Objects::nonNull)
	            .reduce(0L, Long::sum);

	    Long totalCnpjs = lista.stream()
	            .map(AnaliseIARecuperabilidadeDTO::getCnpj)
	            .filter(Objects::nonNull)
	            .distinct()
	            .count();

	    StringBuilder sb = new StringBuilder();

	    AnaliseIARecuperabilidadeDTO base = lista.get(0);

	    sb.append("CNPJ Base: ").append(base.getCnpjBase()).append("\n");
	    sb.append("Situação CNPJ Base: ").append(base.getSituacaoCnpjBase()).append("\n");
	    sb.append("Situação Analisada: ").append(base.getSituacaoAnalisada()).append("\n");
	    sb.append("Total de CNPJs vinculados: ").append(totalCnpjs).append("\n");
	    sb.append("Total de Débitos: ").append(totalDebitos).append("\n");
	    sb.append("Valor Total Consolidado: ").append(totalValor).append("\n");
	    sb.append("Valor Estoque Base: ").append(base.getVlrEstoqueBase()).append("\n\n");

	    sb.append("Detalhamento por CNPJ:\n");

	    for (AnaliseIARecuperabilidadeDTO dto : lista) {

	        sb.append("-------------------------------------------\n");
	        sb.append("CNPJ: ").append(dto.getCnpj()).append("\n");
	        sb.append("Razão Social: ").append(dto.getNomeRazaoSocial()).append("\n");
	        sb.append("Situação Cadastro: ").append(dto.getSituacaoBcadastroCnpj()).append("\n");
	        sb.append("Classificação Tributária: ").append(dto.getClassificacaoTributaria()).append("\n");
	        sb.append("Status Ajuizamento: ").append(dto.getStatusAjuizamento()).append("\n");
	        sb.append("Qtd Débitos: ").append(dto.getQtdeDebitos()).append("\n");
	        sb.append("Valor: ").append(dto.getValor()).append("\n");
	        sb.append("Valor sem Honorários: ").append(dto.getVlSemHonorarios()).append("\n");
	    }

	    return sb.toString();
	}
    
   
    
    private String montarContextoEmpresaBasico(AnaliseIARecuperabilidadeDTO e) {
        return """
                CNPJ: %s
                Razão Social: %s
                Situação: %s
                Débitos Ativos: %s
                Valor Total Débito: R$ %s
                """.formatted(
                    e.getCnpj(),
                    e.getNomeRazaoSocial(),
                    e.getSituacaoAnalisada(),
                    e.getQtdeDebitos(),
                    e.getValor()
                );
    }
    
    private String montarContextoEmpresa(AnaliseIARecuperabilidadeDTO dto) {
    	String contexto = """
    			Empresa: %s
    			CNPJ: %s
    			Situação Cadastral: %s
    			Situação CNPJ Base: %s
    			Status Ajuizamento: %s
    			Classificação: %s

    			Total Débitos Base: %d
    			Quantidade Débitos: %d

    			Valor Total: %s
    			Valor Honorários: %s
    			Valor Honorários Adm: %s
    			Valor Sem Honorários: %s
    			Estoque Total: %s

    			Classificação Final Sistema: %s
    			""".formatted(
    			        dto.getNomeRazaoSocial(),
    			        dto.getCnpj(),
    			        dto.getSituacaoBcadastroCnpj(),
    			        dto.getSituacaoAnalisada(),
    			        dto.getStatusAjuizamento(),
    			        dto.getClassificacaoTributaria(),
    			        dto.getQtdeDebitosBase(),
    			        dto.getQtdeDebitos(),
    			        dto.getValor(),
    			        dto.getVh(),
    			        dto.getVhadm(),
    			        dto.getVlSemHonorarios(),
    			        dto.getVlrEstoqueBase(),
    			        dto.getSituacaoAnalisada()
    			);
    	
    	return contexto;
    }  
        
    private Optional<String> extrairCnpj(String pergunta) {

        Pattern pattern = Pattern.compile(REGEX_CNPJ);
        Matcher matcher = pattern.matcher(pergunta);

        if (matcher.find()) {

            String numeroLimpo = matcher.group().replaceAll("[^\\d]", "");

            if (numeroLimpo.length() == 14) {
                return Optional.of(numeroLimpo.substring(0, 8)); // retorna base
            }

            if (numeroLimpo.length() == 8) {
                return Optional.of(numeroLimpo); // já é base
            }
        }

        return Optional.empty();
    }
    
    private boolean contemVerboBusca(String pergunta) {
        String p = pergunta.toLowerCase();

        // verifica se algum verbo está contido na pergunta
        return VERBOS_BUSCA.stream().anyMatch(p::contains);
    }
}