package pe.tecsup.proyectos.catalogo.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.tecsup.proyectos.catalogo.dto.ProyectoDetalleDto;
import pe.tecsup.proyectos.catalogo.dto.ProyectoListaDto;
import pe.tecsup.proyectos.catalogo.repository.ProyectoRepository;
import pe.tecsup.proyectos.shared.entity.Proyecto;
import pe.tecsup.proyectos.shared.exception.ApiException;
import pe.tecsup.proyectos.shared.response.PaginaDto;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private static final int MAX_PAGINA = 50;

    private final ProyectoRepository proyectoRepository;

    public List<String> listarCiclos() {
        return proyectoRepository.findDistinctCiclos();
    }

    public PaginaDto<ProyectoListaDto> listarProyectos(String ciclo, String origen, String busqueda, int page, int size) {
        int tamanio = Math.min(Math.max(size, 1), MAX_PAGINA);
        Specification<Proyecto> spec = construirSpec(ciclo, origen, busqueda);
        Page<Proyecto> pageResult = proyectoRepository.findAll(spec, PageRequest.of(page, tamanio));
        List<ProyectoListaDto> content = pageResult.getContent().stream()
                .map(ProyectoListaDto::from)
                .toList();
        return new PaginaDto<>(content, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    public ProyectoDetalleDto obtenerDetalle(Long id) {
        Proyecto proyecto = proyectoRepository.findByIdWithIntegrantes(id)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Proyecto no encontrado.", HttpStatus.NOT_FOUND));
        return ProyectoDetalleDto.from(proyecto);
    }

    private Specification<Proyecto> construirSpec(String ciclo, String origen, String busqueda) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (ciclo != null && !ciclo.isBlank()) {
                predicados.add(cb.equal(root.get("ciclo"), ciclo));
            }

            if (busqueda != null && !busqueda.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + busqueda.toLowerCase() + "%"));
            }

            if (origen != null && !origen.isBlank()) {
                try {
                    Proyecto.Origen origenEnum = Proyecto.Origen.valueOf(origen.toUpperCase());
                    predicados.add(cb.equal(root.get("origen"), origenEnum));
                } catch (IllegalArgumentException e) {
                    predicados.add(cb.disjunction());
                }
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
