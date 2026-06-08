package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.SorteioJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.SorteioSpringDataRepository;

@Repository
public class SorteioRepositorioPersistencia implements SorteioRepositorio {

    private final SorteioSpringDataRepository springData;

    public SorteioRepositorioPersistencia(SorteioSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public void salvar(Sorteio sorteio) {
        SorteioJpa jpa = SorteioJpa.fromDomain(sorteio);
        SorteioJpa salvo = springData.save(jpa);
        if (sorteio.getId() == null) {
            sorteio.atribuirId(new SorteioId(salvo.getId()));
        }
    }

    @Override
    public Sorteio obter(SorteioId id) {
        return springData.findById(id.getId())
                .map(SorteioJpa::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Sorteio não encontrado: " + id));
    }

    @Override
    public List<Sorteio> pesquisarPorEvento(EventoId eventoId) {
        return springData.findByEventoId(eventoId.getId()).stream()
                .map(SorteioJpa::toDomain)
                .toList();
    }
}
