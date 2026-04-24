package cesar.rv.ingressify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

@SpringBootApplication(scanBasePackages = "cesar.rv.ingressify")
@EntityScan(basePackages = "cesar.rv.ingressify.infraestrutura.persistencia.jpa")
@EnableJpaRepositories(basePackages = "cesar.rv.ingressify.infraestrutura.persistencia.jpa")
public class BackendAplicacao {

	public static void main(String[] args) {
		SpringApplication.run(BackendAplicacao.class, args);
	}

	@Bean
	public EventoServico eventoServico(EventoRepositorio repositorio) {
		return new EventoServico(repositorio);
	}

	@Bean
	public TipoIngressoServico tipoIngressoServico(TipoIngressoRepositorio repositorio, EventoRepositorio eventoRepositorio) {
		return new TipoIngressoServico(repositorio, eventoRepositorio);
	}

	@Bean
	public IngressoServico ingressoServico(IngressoRepositorio repositorio) {
		return new IngressoServico(repositorio);
	}

	@Bean
	public AnuncioRevendaServico anuncioRevendaServico(AnuncioRevendaRepositorio anuncioRepositorio,
			IngressoRepositorio ingressoRepositorio) {
		return new AnuncioRevendaServico(anuncioRepositorio, ingressoRepositorio);
	}

	@Bean
	public PagamentoServico pagamentoServico(PagamentoRepositorio repositorio) {
		return new PagamentoServico(repositorio);
	}

	@Bean
	public SaldoServico saldoServico(SaldoRepositorio repositorio) {
		return new SaldoServico(repositorio);
	}
}
