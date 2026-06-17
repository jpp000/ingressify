package cesar.rv.ingressify.infraestrutura.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import cesar.rv.ingressify.aplicacao.financeiro.extrato.ExtratoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.identidade.usuario.UsuarioServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda.AnuncioRevendaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.avaliacao.AvaliacaoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.checkin.CheckinServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.compra.CompraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.feed.FeedServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.MapaAssentosServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.sorteio.SorteioServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.CriarLoteDto;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoServicoAplicacao;
import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pontos.PontosServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

@Component
@Profile("seed")
public class DatabaseSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
	private static final String SENHA = "Senha123";
	private static final String MARCADOR = "admin@ingressify.local";

	private final ApplicationContext context;
	private final UsuarioRepositorio usuarioRepositorio;
	private final UsuarioServicoAplicacao usuarioServico;
	private final ExtratoServicoAplicacao extratoServico;
	private final EventoServicoAplicacao eventoServicoAplicacao;
	private final EventoServico eventoServico;
	private final TipoIngressoServicoAplicacao tipoIngressoServicoAplicacao;
	private final TipoIngressoServico tipoIngressoServico;
	private final CompraServicoAplicacao compraServico;
	private final IngressoServico ingressoServico;
	private final SorteioServicoAplicacao sorteioServico;
	private final MapaAssentosServicoAplicacao mapaAssentosServico;
	private final AnuncioRevendaServicoAplicacao anuncioRevendaServico;
	private final FeedServicoAplicacao feedServico;
	private final AvaliacaoServicoAplicacao avaliacaoServico;
	private final CheckinServicoAplicacao checkinServico;
	private final DenunciaServicoAplicacao denunciaServico;
	private final ReembolsoServicoAplicacao reembolsoServico;
	private final PontosServico pontosServico;

	public DatabaseSeeder(ApplicationContext context, UsuarioRepositorio usuarioRepositorio,
			UsuarioServicoAplicacao usuarioServico, ExtratoServicoAplicacao extratoServico,
			EventoServicoAplicacao eventoServicoAplicacao, EventoServico eventoServico,
			TipoIngressoServicoAplicacao tipoIngressoServicoAplicacao, TipoIngressoServico tipoIngressoServico,
			CompraServicoAplicacao compraServico, IngressoServico ingressoServico,
			SorteioServicoAplicacao sorteioServico, MapaAssentosServicoAplicacao mapaAssentosServico,
			AnuncioRevendaServicoAplicacao anuncioRevendaServico, FeedServicoAplicacao feedServico,
			AvaliacaoServicoAplicacao avaliacaoServico, CheckinServicoAplicacao checkinServico,
			DenunciaServicoAplicacao denunciaServico, ReembolsoServicoAplicacao reembolsoServico,
			PontosServico pontosServico) {
		this.context = context;
		this.usuarioRepositorio = usuarioRepositorio;
		this.usuarioServico = usuarioServico;
		this.extratoServico = extratoServico;
		this.eventoServicoAplicacao = eventoServicoAplicacao;
		this.eventoServico = eventoServico;
		this.tipoIngressoServicoAplicacao = tipoIngressoServicoAplicacao;
		this.tipoIngressoServico = tipoIngressoServico;
		this.compraServico = compraServico;
		this.ingressoServico = ingressoServico;
		this.sorteioServico = sorteioServico;
		this.mapaAssentosServico = mapaAssentosServico;
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.feedServico = feedServico;
		this.avaliacaoServico = avaliacaoServico;
		this.checkinServico = checkinServico;
		this.denunciaServico = denunciaServico;
		this.reembolsoServico = reembolsoServico;
		this.pontosServico = pontosServico;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepositorio.existeEmail(MARCADOR)) {
			log.info("Banco já populado ({} encontrado). Pulando seed.", MARCADOR);
			exitIfRequested();
			return;
		}

		log.info("Iniciando seed do banco Ingressify...");

		Map<String, UsuarioId> usuarios = criarUsuarios();
		UsuarioId maria = usuarios.get("maria");
		UsuarioId joao = usuarios.get("joao");
		UsuarioId ana = usuarios.get("ana");
		UsuarioId pedro = usuarios.get("pedro");
		UsuarioId carlos = usuarios.get("carlos");
		UsuarioId lucia = usuarios.get("lucia");
		List<UsuarioId> compradores = usuarios.entrySet().stream()
				.filter(e -> e.getKey().startsWith("comprador") || List.of("ana", "pedro", "lucia").contains(e.getKey()))
				.map(Map.Entry::getValue)
				.toList();

		criarSaldos(compradores);

		// Pontos de fidelidade: ana já começa com 1.200 pontos para demonstrar o resgate
		// (1.000 pontos = R$ 10), deixando uma sobra de 200 pontos após o primeiro resgate.
		pontosServico.adicionar(ana, 1200);

		LocalDateTime agora = LocalDateTime.now();

		EventoId festival = criarEvento(maria, "Festival de Verão 2026",
				agora.plusDays(90), "Arena Recife", "O maior festival de música do Nordeste.",
				5000, "Música", agora.plusDays(90).minusHours(3));
		TipoIngressoId pista = criarTipo(festival, maria, "Pista", new BigDecimal("120.00"), 800,
				"Acesso à área pista", List.of("Acesso geral", "Food trucks"),
				List.of(new CriarLoteDto("Early Bird", new BigDecimal("89.90"), 200,
						agora.minusDays(1), agora.plusDays(30)),
						new CriarLoteDto("1º Lote", new BigDecimal("109.90"), 300,
								agora.plusDays(30), agora.plusDays(60)),
						new CriarLoteDto("2º Lote", new BigDecimal("120.00"), 300,
								agora.plusDays(60), agora.plusDays(89))));
		TipoIngressoId camarote = criarTipo(festival, maria, "Camarote VIP", new BigDecimal("350.00"), 150,
				"Open bar e vista privilegiada", List.of("Open bar", "Lounge exclusivo"), null);

		EventoId standup = eventoServicoAplicacao.criarEvento(joao, "Stand-up Comedy Night",
				agora.plusDays(25), "Teatro Guararapes", "Noite de risadas com os melhores comediantes.",
				300, 200, null, 7, agora.plusDays(25).minusHours(2), "Teatro e Artes");
		TipoIngressoId inteira = criarTipo(standup, joao, "Inteira", new BigDecimal("60.00"), 200,
				"Ingresso inteiro", null, null);
		TipoIngressoId meia = criarTipo(standup, joao, "Meia-entrada", new BigDecimal("30.00"), 100,
				"Estudantes e idosos", List.of("Documento obrigatório"), null);
		mapaAssentosServico.criarMapa(standup, 10, 20, new BigDecimal("60.00"), new BigDecimal("90.00"), joao);

		EventoId tech = criarEvento(maria, "Tech Conference CESAR",
				agora.plusDays(45), "CESAR - Recife", "Palestras sobre IA, cloud e produto digital.",
				800, "Conferências", agora.plusDays(45).minusHours(1));
		TipoIngressoId palestras = criarTipo(tech, maria, "Credencial Palestras", new BigDecimal("150.00"), 500,
				"Acesso a todas as trilhas", List.of("Certificado", "Coffee break"), null);
		TipoIngressoId workshop = criarTipo(tech, maria, "Workshop Hands-on", new BigDecimal("280.00"), 120,
				"Vagas limitadas com mentoria", List.of("Material incluso", "Vaga garantida"), null);

		EventoId rock = criarEvento(joao, "Rock in Recife",
				agora.plusDays(70), "Espaço Cultural", "Bandas locais e nacionais no palco principal.",
				2000, "Música", agora.plusDays(70).minusHours(4));
		TipoIngressoId rockPista = criarTipo(rock, joao, "Pista", new BigDecimal("95.00"), 1500,
				"Pista premium", null, null);

		EventoId eletronica = criarEvento(maria, "Noite Eletrônica Warehouse",
				agora.plusDays(18), "Warehouse Boa Viagem", "DJs internacionais até o amanhecer.",
				1500, "Vida Noturna", agora.plusDays(18).minusHours(2));
		TipoIngressoId vip = criarTipo(eletronica, maria, "VIP", new BigDecimal("200.00"), 300,
				"Área VIP com bar exclusivo", List.of("Fast pass", "Welcome drink"), null);

		EventoId gastronomico = criarEvento(joao, "Festival Gastronômico do Porto",
				agora.plusDays(12), "Porto Digital", "Degustações e chefs convidados.",
				400, "Comida e Bebida", agora.plusDays(12).minusHours(1));
		TipoIngressoId degustacao = criarTipo(gastronomico, joao, "Passaporte Degustação", new BigDecimal("75.00"), 350,
				"15 barracas parceiras", List.of("Copo colecionável"), null);

		EventoId showHoje = criarEvento(maria, "Show ao Vivo — Portões Abertos",
				agora.plusHours(6), "Clube Náutico", "Show com portões já liberados para check-in.",
				800, "Música", agora.minusHours(2));
		TipoIngressoId showHojeTipo = criarTipo(showHoje, maria, "Pista", new BigDecimal("80.00"), 600,
				"Ingresso para o show de hoje", null, null);

		EventoId festivalPassado = criarEventoPassado(joao, "Festival Antigo 2025",
				agora.minusDays(20), "Parque da Jaqueira", "Edição passada para avaliações.",
				1200, "Música", agora.minusDays(20).minusHours(3));
		TipoIngressoId passadoTipo = criarTipoDireto(festivalPassado, "Pista", new BigDecimal("100.00"), 0, 500);

		EventoId cancelado = criarEvento(maria, "Show Cancelado — Teste Reembolso",
				agora.plusDays(40), "Centro de Eventos", "Evento que será cancelado para testes.",
				500, "Música", agora.plusDays(40).minusHours(2));
		TipoIngressoId canceladoTipo = criarTipo(cancelado, maria, "Pista", new BigDecimal("50.00"), 400,
				"Ingresso do evento cancelado", null, null);

		comprarEmMassa(compradores, List.of(pista, camarote, inteira, meia, palestras, workshop, rockPista, vip, degustacao));
		comprar(compradores.get(0), showHojeTipo, 2);
		comprar(compradores.get(1), showHojeTipo, 1);
		comprar(lucia, canceladoTipo, 2);

		criarIngressosPassado(festivalPassado, passadoTipo, ana, pedro, lucia);

		// ── Sorteios em diferentes estados para a apresentação ──────────────
		// Cada sorteio fica num estado distinto para demonstrar uma operação ao vivo.
		// Atores: organizador = maria@ ; comprador de demo = ana@.

		// (1) CONFIGURADO — maria pode demonstrar "Abrir Inscrições" ao vivo.
		sorteioServico.criar(tech, workshop, maria, 20, 10, agora.plusDays(40), 48);

		// (2) INSCRIÇÕES ABERTAS — ana ainda NÃO está inscrita: pode "Inscrever-se" ao vivo.
		//     maria também pode demonstrar "Encerrar Inscrições".
		SorteioId sorteioAberto = sorteioServico.criar(festival, pista, maria, 30, 50,
				agora.plusDays(55), 48);
		sorteioServico.abrirInscricoes(sorteioAberto, maria);
		for (int i = 3; i <= 14 && i < compradores.size(); i++) { // comprador1..comprador12 (sem ana/pedro/lucia)
			sorteioServico.inscrever(sorteioAberto, compradores.get(i));
		}

		// (3) AGUARDANDO SORTEIO — 12 inscritos para 4 vagas: maria demonstra "Realizar
		//     Sorteio" ao vivo e o resultado mostra contemplados + lista de espera.
		SorteioId sorteioPronto = sorteioServico.criar(eletronica, vip, maria, 4, 4,
				agora.plusDays(16), 48);
		sorteioServico.abrirInscricoes(sorteioPronto, maria);
		for (int i = 1; i <= 12 && i < compradores.size(); i++) {
			sorteioServico.inscrever(sorteioPronto, compradores.get(i));
		}
		sorteioServico.encerrarInscricoes(sorteioPronto, maria);

		// (4) SORTEADO — já executado; ana é contemplada (vagas >= inscritos) e pode
		//     demonstrar "Confirmar Participação" ao vivo.
		SorteioId sorteioFinalizado = sorteioServico.criar(festival, camarote, maria, 8, 4,
				agora.plusDays(50), 48);
		sorteioServico.abrirInscricoes(sorteioFinalizado, maria);
		sorteioServico.inscrever(sorteioFinalizado, ana);
		for (int i = 3; i <= 6 && i < compradores.size(); i++) {
			sorteioServico.inscrever(sorteioFinalizado, compradores.get(i));
		}
		sorteioServico.encerrarInscricoes(sorteioFinalizado, maria);
		sorteioServico.executarSorteio(sorteioFinalizado);

		EventoId esgotado = criarEvento(maria, "Indie Rock Night — Lotado",
				agora.plusDays(15), "Tato Coletivo", "Casa lotada: venda oficial encerrada.",
				2, "Música", agora.plusDays(15).minusHours(2));
		TipoIngressoId esgotadoTipo = criarTipoDireto(esgotado, "Pista", new BigDecimal("160.00"), 0, 2);
		Ingresso ingressoRevenda = new Ingresso(esgotadoTipo, esgotado, pedro);
		ingressoServico.salvar(ingressoRevenda);
		ingressoServico.salvar(new Ingresso(esgotadoTipo, esgotado, pedro));
		AnuncioRevendaId anuncio = anuncioRevendaServico.anunciar(pedro, List.of(ingressoRevenda.getId()),
				new BigDecimal("320.00"));
		denunciaServico.denunciar(anuncio, ana, MotivoDenuncia.PRECO_ABUSIVO,
				"Preço muito acima do valor original do ingresso.");

		PostagemId post = feedServico.publicar(festival, maria, "Line-up confirmado!",
				"Mais de 40 atrações confirmadas para o Festival de Verão 2026. Garanta seu ingresso!",
				null, true);
		feedServico.comentar(post, ana, "Mal posso esperar! Já comprei meu ingresso.");

		avaliacaoServico.avaliar(festivalPassado, ana, 5, "Evento incrível, organização impecável!");
		avaliacaoServico.avaliar(festivalPassado, pedro, 4, "Muito bom, só faltou mais banheiros.");
		avaliacaoServico.avaliar(festivalPassado, lucia, 5, "Melhor festival que já fui em Recife.");

		List<IngressoId> ingressosAnaShow = compraServico.comprar(ana, showHojeTipo, 1);
		checkinServico.realizarCheckin(ingressosAnaShow.get(0).toString(), carlos);

		List<IngressoId> ingressosLuciaFestival = compraServico.comprar(lucia, pista, 3);
		reembolsoServico.solicitar(ingressosLuciaFestival.get(2), lucia);

		eventoServicoAplicacao.cancelarEvento(cancelado, maria);

		log.info("Seed concluído com sucesso!");
		log.info("── Contas de acesso (senha: {}) ──", SENHA);
		log.info("  Admin:       {} (ADMIN)", MARCADOR);
		log.info("  Organizador: maria@ingressify.local, joao@ingressify.local");
		log.info("  Operador:    carlos@ingressify.local (OPERADOR_PORTA)");
		log.info("  Compradores: ana@, pedro@, lucia@, comprador1@ ... comprador15@ingressify.local");
		log.info("  Eventos:     10 | Usuários: {} | Compras em massa realizadas", usuarios.size());
		log.info("── Sorteios para a apresentação (organizador maria@, comprador ana@) ──");
		log.info("  Tech Conference CESAR  → CONFIGURADO        (maria: Abrir Inscrições)");
		log.info("  Festival de Verão 2026 → INSCRIÇÕES ABERTAS (ana: Inscrever-se / maria: Encerrar)");
		log.info("  Noite Eletrônica       → AGUARDANDO SORTEIO (maria: Realizar Sorteio ao vivo)");
		log.info("  Festival de Verão 2026 → SORTEADO           (ana contemplada: Confirmar Participação)");

		exitIfRequested();
	}

	private Map<String, UsuarioId> criarUsuarios() {
		Map<String, UsuarioId> map = new LinkedHashMap<>();
		map.put("admin", cadastrar("Admin Ingressify", MARCADOR));
		usuarioServico.concederPapel(map.get("admin"), Papel.ADMIN);

		map.put("maria", cadastrarOrganizador("Maria Silva", "maria@ingressify.local"));
		map.put("joao", cadastrarOrganizador("João Santos", "joao@ingressify.local"));
		map.put("ana", cadastrar("Ana Costa", "ana@ingressify.local"));
		map.put("pedro", cadastrar("Pedro Oliveira", "pedro@ingressify.local"));
		map.put("lucia", cadastrar("Lúcia Ferreira", "lucia@ingressify.local"));

		UsuarioId carlos = cadastrar("Carlos Porta", "carlos@ingressify.local");
		usuarioServico.concederPapel(carlos, Papel.OPERADOR_PORTA);
		map.put("carlos", carlos);

		for (int i = 1; i <= 15; i++) {
			map.put("comprador" + i, cadastrar("Comprador " + i, "comprador" + i + "@ingressify.local"));
		}
		return map;
	}

	private UsuarioId cadastrar(String nome, String email) {
		return usuarioServico.cadastrar(nome, email, SENHA);
	}

	private UsuarioId cadastrarOrganizador(String nome, String email) {
		UsuarioId id = cadastrar(nome, email);
		usuarioServico.concederPapel(id, Papel.ORGANIZADOR);
		return id;
	}

	private void criarSaldos(List<UsuarioId> compradores) {
		for (UsuarioId id : compradores) {
			extratoServico.adicionarSaldo(id, new BigDecimal("2500.00"));
		}
	}

	private EventoId criarEvento(UsuarioId organizador, String nome, LocalDateTime dataHora, String local,
			String descricao, int capacidade, String categoria, LocalDateTime aberturaPortoes) {
		return eventoServicoAplicacao.criarEvento(organizador, nome, dataHora, local, descricao, capacidade,
				0, null, 7, aberturaPortoes, categoria);
	}

	private EventoId criarEventoPassado(UsuarioId organizador, String nome, LocalDateTime dataHora, String local,
			String descricao, int capacidade, String categoria, LocalDateTime aberturaPortoes) {
		EventoId id = criarEvento(organizador, nome, LocalDateTime.now().plusDays(120), local, descricao,
				capacidade, categoria, LocalDateTime.now().plusDays(120).minusHours(3));
		Evento passado = new Evento(id, organizador, nome, dataHora, local, descricao,
				StatusEvento.ATIVO, capacidade, null, 7, aberturaPortoes, categoria);
		eventoServico.salvar(passado);
		return id;
	}

	private TipoIngressoId criarTipo(EventoId eventoId, UsuarioId organizador, String nome, BigDecimal preco,
			int quantidade, String descricao, List<String> beneficios, List<CriarLoteDto> lotes) {
		return tipoIngressoServicoAplicacao.criarTipoIngresso(eventoId, organizador, nome, preco, quantidade,
				descricao, beneficios, lotes, false, 50, 0);
	}

	private TipoIngressoId criarTipoDireto(EventoId eventoId, String nome, BigDecimal preco,
			int disponivel, int total) {
		TipoIngresso tipo = new TipoIngresso(eventoId, nome, new Dinheiro(preco), disponivel, total, nome);
		tipoIngressoServico.salvar(tipo);
		return tipo.getId();
	}

	private void comprar(UsuarioId comprador, TipoIngressoId tipo, int qtd) {
		compraServico.comprar(comprador, tipo, qtd);
	}

	private void comprarEmMassa(List<UsuarioId> compradores, List<TipoIngressoId> tipos) {
		int idx = 0;
		for (TipoIngressoId tipo : tipos) {
			for (int q = 0; q < 3; q++) {
				UsuarioId comprador = compradores.get(idx % compradores.size());
				int quantidade = (idx % 3) + 1;
				compraServico.comprar(comprador, tipo, quantidade);
				idx++;
			}
		}
	}

	private void criarIngressosPassado(EventoId eventoId, TipoIngressoId tipoId, UsuarioId... proprietarios) {
		for (UsuarioId proprietario : proprietarios) {
			Ingresso ingresso = new Ingresso(tipoId, eventoId, proprietario);
			ingressoServico.salvar(ingresso);
		}
	}

	private void exitIfRequested() {
		if ("true".equalsIgnoreCase(System.getenv("SEED_EXIT"))) {
			log.info("SEED_EXIT=true — encerrando aplicação.");
			System.exit(SpringApplication.exit(context, () -> 0));
		}
	}
}
