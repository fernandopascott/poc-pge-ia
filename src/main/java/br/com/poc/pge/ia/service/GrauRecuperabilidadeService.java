package br.com.poc.pge.ia.service;

import org.springframework.stereotype.Service;

@Service
public class GrauRecuperabilidadeService {
	
	public void gerarPerguntaGrauRecuperabilidade(String cnpjRaiz) {
		obterDadosGrauRecuperabilidade(cnpjRaiz);
	}	
	
	public void obterDadosGrauRecuperabilidade(String cnpjRaiz) {
		gerarMockGrauRecuperabilidade();
	}
	
	private void gerarMockGrauRecuperabilidade() {
		
	}

}
