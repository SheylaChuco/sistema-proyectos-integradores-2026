package pe.tecsup.proyectos.grupo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.grupo.dto.BuscarEstudianteDto;
import pe.tecsup.proyectos.grupo.dto.CrearGrupoRequest;
import pe.tecsup.proyectos.grupo.dto.GrupoResponse;
import pe.tecsup.proyectos.grupo.repository.GrupoIntegranteRepository;
import pe.tecsup.proyectos.grupo.repository.GrupoRepository;
import pe.tecsup.proyectos.grupo.service.GrupoService;
import pe.tecsup.proyectos.shared.entity.Estudiante;
import pe.tecsup.proyectos.shared.entity.Grupo;
import pe.tecsup.proyectos.shared.entity.Usuario;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private GrupoRepository grupoRepository;
    @Mock
    private GrupoIntegranteRepository grupoIntegranteRepository;

    @InjectMocks
    private GrupoService grupoService;

    @Test
    void buscarEstudiantes_conNombre_retornaCoincidencias() {
        Estudiante e = buildEstudiante(1L, "Ana Torres", "2024001");
        when(estudianteRepository.buscarPorNombre("Ana")).thenReturn(List.of(e));
        when(grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(anyLong(), anyString())).thenReturn(false);

        List<BuscarEstudianteDto> result = grupoService.buscarEstudiantes("Ana");

        assertEquals(1, result.size());
        assertEquals("Ana Torres", result.get(0).nombre());
        assertEquals("2024001", result.get(0).codigoEstudiante());
    }

    @Test
    void crearGrupo_exitoso_creaGrupoConIntegrantes() {
        Estudiante creador = buildEstudiante(1L, "Maria Lopez", "2024001");
        Estudiante companero = buildEstudiante(2L, "Juan Perez", "2024002");
        Grupo grupoGuardado = buildGrupo(10L, "GRP-ABC123");

        when(estudianteRepository.findByUsuarioId(100L)).thenReturn(Optional.of(creador));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(companero));
        when(grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(anyLong(), anyString())).thenReturn(false);
        when(grupoRepository.save(any())).thenReturn(grupoGuardado);
        when(grupoIntegranteRepository.saveAll(any())).thenReturn(List.of());

        GrupoResponse response = grupoService.crearGrupo(100L, buildRequest(List.of(2L)));

        assertNotNull(response);
        assertNotNull(response.getCodigoGrupo());
        assertEquals(2, response.getIntegrantes().size());
    }

    @Test
    void crearGrupo_companeroInexistente_lanzaNoEncontrado() {
        Estudiante creador = buildEstudiante(1L, "Maria Lopez", "2024001");
        when(estudianteRepository.findByUsuarioId(100L)).thenReturn(Optional.of(creador));
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> grupoService.crearGrupo(100L, buildRequest(List.of(99L))));

        assertEquals("NO_ENCONTRADO", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void crearGrupo_creadorYaTieneGrupo_lanzaEstudianteYaTieneGrupo() {
        Estudiante creador = buildEstudiante(1L, "Maria Lopez", "2024001");
        when(estudianteRepository.findByUsuarioId(100L)).thenReturn(Optional.of(creador));
        when(grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(eq(1L), anyString())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> grupoService.crearGrupo(100L, buildRequest(List.of())));

        assertEquals("ESTUDIANTE_YA_TIENE_GRUPO", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void crearGrupo_companeroYaTieneGrupo_lanzaEstudianteYaTieneGrupo() {
        Estudiante creador = buildEstudiante(1L, "Maria Lopez", "2024001");
        Estudiante companero = buildEstudiante(2L, "Juan Perez", "2024002");
        when(estudianteRepository.findByUsuarioId(100L)).thenReturn(Optional.of(creador));
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(companero));
        when(grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(eq(1L), anyString())).thenReturn(false);
        when(grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(eq(2L), anyString())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> grupoService.crearGrupo(100L, buildRequest(List.of(2L))));

        assertEquals("ESTUDIANTE_YA_TIENE_GRUPO", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    private Estudiante buildEstudiante(Long id, String nombre, String codigo) {
        Usuario u = new Usuario();
        u.setId(100L + id);
        u.setNombre(nombre);
        u.setRol(Usuario.Rol.ESTUDIANTE);
        Estudiante e = new Estudiante();
        e.setId(id);
        e.setCodigoEstudiante(codigo);
        e.setUsuario(u);
        return e;
    }

    private Grupo buildGrupo(Long id, String codigo) {
        Grupo g = new Grupo();
        g.setId(id);
        g.setCodigoGrupo(codigo);
        g.setPeriodo("2026-I");
        return g;
    }

    private CrearGrupoRequest buildRequest(List<Long> integranteIds) {
        CrearGrupoRequest r = new CrearGrupoRequest();
        r.setIntegranteIds(integranteIds);
        return r;
    }
}
