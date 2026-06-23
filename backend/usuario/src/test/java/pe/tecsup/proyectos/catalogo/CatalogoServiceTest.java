package pe.tecsup.proyectos.catalogo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import pe.tecsup.proyectos.catalogo.dto.ProyectoDetalleDto;
import pe.tecsup.proyectos.catalogo.dto.ProyectoListaDto;
import pe.tecsup.proyectos.catalogo.repository.ProyectoRepository;
import pe.tecsup.proyectos.catalogo.service.CatalogoService;
import pe.tecsup.proyectos.shared.entity.Grupo;
import pe.tecsup.proyectos.shared.entity.Proyecto;
import pe.tecsup.proyectos.shared.exception.ApiException;
import pe.tecsup.proyectos.shared.response.PaginaDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CatalogoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private CatalogoService cataloService;

    @Test
    void listarProyectos_sinFiltros_retornaPaginadoConDtos() {
        Proyecto p = buildHistorico(1L, "EcoRuta", "2025-I");
        Page<Proyecto> pageMock = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        when(proyectoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageMock);

        PaginaDto<ProyectoListaDto> result = cataloService.listarProyectos(null, null, null, 0);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
    }

    @Test
    void listarProyectos_proyectoHistorico_estadoNuloEnDto() {
        Proyecto p = buildHistorico(1L, "EcoRuta", "2025-I");
        Page<Proyecto> pageMock = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        when(proyectoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageMock);

        PaginaDto<ProyectoListaDto> result = cataloService.listarProyectos(null, null, null, 0);

        ProyectoListaDto dto = result.getContent().get(0);
        assertEquals("HISTORICO", dto.getOrigen());
        assertNull(dto.getEstado()); // HISTORICO nunca expone estado
    }

    @Test
    void listarProyectos_proyectoNuevo_estadoVisibleEnDto() {
        Proyecto p = buildNuevo(2L, "App Reciclaje", "2026-I", Proyecto.Estado.EN_DESARROLLO);
        Page<Proyecto> pageMock = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        when(proyectoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageMock);

        PaginaDto<ProyectoListaDto> result = cataloService.listarProyectos(null, null, null, 0);

        ProyectoListaDto dto = result.getContent().get(0);
        assertEquals("NUEVO", dto.getOrigen());
        assertEquals("EN_DESARROLLO", dto.getEstado());
    }

    @Test
    void obtenerDetalle_proyectoHistorico_sinCamposExclusivosNuevo() {
        Proyecto p = buildHistorico(1L, "EcoRuta", "2025-I");
        when(proyectoRepository.findByIdWithIntegrantes(1L)).thenReturn(Optional.of(p));

        ProyectoDetalleDto dto = cataloService.obtenerDetalle(1L);

        assertEquals("HISTORICO", dto.getOrigen());
        assertNull(dto.getEstado());
        assertNull(dto.getUrl());
        assertNull(dto.getComentarioEvaluacion());
        assertNull(dto.getIntegrantes());
        assertNull(dto.getCodigoGrupo());
    }

    @Test
    void obtenerDetalle_proyectoNuevo_conCamposDeGrupo() {
        Proyecto p = buildNuevo(2L, "App Reciclaje", "2026-I", Proyecto.Estado.EN_DESARROLLO);
        Grupo g = new Grupo();
        g.setCodigoGrupo("G-01");
        g.setIntegrantes(List.of());
        p.setGrupo(g);
        when(proyectoRepository.findByIdWithIntegrantes(2L)).thenReturn(Optional.of(p));

        ProyectoDetalleDto dto = cataloService.obtenerDetalle(2L);

        assertEquals("NUEVO", dto.getOrigen());
        assertEquals("EN_DESARROLLO", dto.getEstado());
        assertEquals("G-01", dto.getCodigoGrupo());
        assertNotNull(dto.getIntegrantes());
    }

    @Test
    void obtenerDetalle_idInexistente_lanzaNoEncontrado() {
        when(proyectoRepository.findByIdWithIntegrantes(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> cataloService.obtenerDetalle(99L));
        assertEquals("NO_ENCONTRADO", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    private Proyecto buildHistorico(Long id, String nombre, String ciclo) {
        Proyecto p = new Proyecto();
        p.setId(id);
        p.setOrigen(Proyecto.Origen.HISTORICO);
        p.setNombre(nombre);
        p.setDescripcion("Descripción de prueba.");
        p.setCiclo(ciclo);
        p.setCodigoSeccion("DSW-01");
        return p;
    }

    private Proyecto buildNuevo(Long id, String nombre, String ciclo, Proyecto.Estado estado) {
        Proyecto p = new Proyecto();
        p.setId(id);
        p.setOrigen(Proyecto.Origen.NUEVO);
        p.setNombre(nombre);
        p.setDescripcion("Descripción de prueba.");
        p.setCiclo(ciclo);
        p.setEstado(estado);
        return p;
    }
}
