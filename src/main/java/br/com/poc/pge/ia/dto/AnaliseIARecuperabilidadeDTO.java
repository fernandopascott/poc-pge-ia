package br.com.poc.pge.ia.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnaliseIARecuperabilidadeDTO {

    @JsonProperty("CNPJBASE")
    private String cnpjBase;

    @JsonProperty("CNPJ")
    private String cnpj;

    @JsonProperty("SITUACAO_BCADASTRO_CNPJ")
    private String situacaoBcadastroCnpj;

    @JsonProperty("status_ajuizamento")
    private String statusAjuizamento;

    @JsonProperty("classificacao_tributaria")
    private String classificacaoTributaria;

    @JsonProperty("id_situacao_debito")
    private Long idSituacaoDebito;

    @JsonProperty("tipo_debito")
    private String tipoDebito;

    @JsonProperty("qtde_debitos_base")
    private Long qtdeDebitosBase;

    @JsonProperty("vlr_estoque_base")
    private BigDecimal vlrEstoqueBase;

    @JsonProperty("SITUACAO_CNPJBASE")
    private String situacaoCnpjBase;

    @JsonProperty("SITUACAO_ANALISADA")
    private String situacaoAnalisada;

    @JsonProperty("qtde_debitos")
    private Long qtdeDebitos;

    @JsonProperty("valor")
    private BigDecimal valor;

    @JsonProperty("vh")
    private BigDecimal vh;

    @JsonProperty("vhadm")
    private BigDecimal vhadm;

    @JsonProperty("vl_sem_honorarios")
    private BigDecimal vlSemHonorarios;

    @JsonProperty("nome_razaosocial")
    private String nomeRazaoSocial;
}