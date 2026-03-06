package br.com.poc.pge.ia.service;

import java.util.List;

import br.com.poc.pge.ia.dto.AnaliseIARecuperabilidadeDTO;

public interface AnaliseRecuperabilidadeService {

    List<AnaliseIARecuperabilidadeDTO> buscarPorCnpjBase(String cnpjBase);

}