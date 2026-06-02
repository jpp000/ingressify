package cesar.rv.ingressify.aplicacao.marketplace.catalogo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FiltroCatalogo {

	private String cidade;
	private LocalDateTime dataInicio;
	private LocalDateTime dataFim;
	private BigDecimal precoMin;
	private BigDecimal precoMax;
	private String nome;

	public FiltroCatalogo() {
	}

	public FiltroCatalogo(String cidade, LocalDateTime dataInicio, LocalDateTime dataFim, BigDecimal precoMin,
			BigDecimal precoMax, String nome) {
		this.cidade = cidade;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
		this.precoMin = precoMin;
		this.precoMax = precoMax;
		this.nome = nome;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public LocalDateTime getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDateTime dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDateTime getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDateTime dataFim) {
		this.dataFim = dataFim;
	}

	public BigDecimal getPrecoMin() {
		return precoMin;
	}

	public void setPrecoMin(BigDecimal precoMin) {
		this.precoMin = precoMin;
	}

	public BigDecimal getPrecoMax() {
		return precoMax;
	}

	public void setPrecoMax(BigDecimal precoMax) {
		this.precoMax = precoMax;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
}
