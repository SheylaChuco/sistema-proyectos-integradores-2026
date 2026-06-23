package pe.tecsup.proyectos.propuesta;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.grupo.repository.GrupoIntegranteRepository;
import pe.tecsup.proyectos.propuesta.dto.CrearPropuestaRequest;
import pe.tecsup.proyectos.propuesta.dto.EditarPropuestaRequest;
import pe.tecsup.proyectos.propuesta.dto.PropuestaResponse;
import pe.tecsup.proyectos.propuesta.repository.PropuestaRepository;
import pe.tecsup.proyectos.propuesta.repository.PropuestaVersionRepository;
import pe.tecsup.proyectos.propuesta.service.PropuestaService;
import pe.tecsup.proyectos.shared.entity.*;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropuestaServiceTest {

    @Mock private EstudianteRepository estudianteRepository;
    @Mock private GrupoIntegranteRepository grupoIntegranteRepository;
    @Mock private PropuestaRepository propuestaRepository;
    @Mock private PropuestaVersionRepository propuestaVersionRepository;

    @InjectMocks private PropuestaService propuestaService;

    @Test
    void crearPropuesta_exitoso_creaConEstadoPendiente() {
        Estudiante estudiante = buildEstudiante(1L);
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, buildGrupo(10L, "G-01"));
        Propuesta propuestaGuardada = buildPropuesta(100L, gi.getGrupo(), Propuesta.Estado.PENDIENTE);

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.empty());
        when(propuestaRepository.save(any())).thenReturn(propuestaGuardada);

        CrearPropuestaRequest req = buildRequest("App Reciclaje", "Una app para reciclar.");
        PropuestaResponse resp = propuestaService.crearPropuesta(200L, req);

        assertNotNull(resp);
        assertEquals("PENDIENTE", resp.getEstado());
        assertEquals("G-01", resp.getCodigoGrupo());
    }

    @Test
    void crearPropuesta_sinGrupo_lanzaSinGrupo() {
        Estudiante estudiante = buildEstudiante(1L);
        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> propuestaService.crearPropuesta(200L, buildRequest("Nombre", "Desc.")));

        assertEquals("SIN_GRUPO", ex.getCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void crearPropuesta_propuestaActivaPendiente_lanzaPropuestaActiva() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);
        Propuesta existente = buildPropuesta(99L, grupo, Propuesta.Estado.PENDIENTE);

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.of(existente));

        ApiException ex = assertThrows(ApiException.class,
                () -> propuestaService.crearPropuesta(200L, buildRequest("Nombre", "Desc.")));

        assertEquals("PROPUESTA_ACTIVA", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void crearPropuesta_propuestaActivaAprobada_lanzaPropuestaActiva() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);
        Propuesta existente = buildPropuesta(99L, grupo, Propuesta.Estado.APROBADO);

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.of(existente));

        ApiException ex = assertThrows(ApiException.class,
                () -> propuestaService.crearPropuesta(200L, buildRequest("Nombre", "Desc.")));

        assertEquals("PROPUESTA_ACTIVA", ex.getCode());
    }

    @Test
    void getMiPropuesta_existente_retornaDatos() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);
        Propuesta propuesta = buildPropuesta(100L, grupo, Propuesta.Estado.OBSERVADO);
        propuesta.setComentarioObservacion("Ampliar el alcance.");

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.of(propuesta));

        PropuestaResponse resp = propuestaService.getMiPropuesta(200L);

        assertEquals("OBSERVADO", resp.getEstado());
        assertEquals("Ampliar el alcance.", resp.getComentarioObservacion());
    }

    @Test
    void getMiPropuesta_sinPropuesta_lanzaNoEncontrado() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> propuestaService.getMiPropuesta(200L));

        assertEquals("NO_ENCONTRADO", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    // --- HU-09: editar propuesta ---

    @Test
    void editarPropuesta_exitoso_guardaVersionYCambiaPendiente() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);
        Propuesta propuesta = buildPropuesta(100L, grupo, Propuesta.Estado.OBSERVADO);
        propuesta.setComentarioObservacion("Definir mejor el alcance.");

        Propuesta propuestaActualizada = buildPropuesta(100L, grupo, Propuesta.Estado.PENDIENTE);
        propuestaActualizada.setNombre("Nombre Corregido");
        propuestaActualizada.setDescripcion("Descripción corregida.");

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.of(propuesta));
        when(propuestaVersionRepository.save(any())).thenReturn(new PropuestaVersion());
        when(propuestaRepository.save(any())).thenReturn(propuestaActualizada);

        EditarPropuestaRequest req = buildEditarRequest("Nombre Corregido", "Descripción corregida.");
        PropuestaResponse resp = propuestaService.editarPropuesta(200L, 100L, req);

        assertNotNull(resp);
        assertEquals("PENDIENTE", resp.getEstado());
        assertEquals("Nombre Corregido", resp.getNombre());
    }

    @Test
    void editarPropuesta_estadoNoPendiente_lanzaEstadoInvalido() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);
        Propuesta propuesta = buildPropuesta(100L, grupo, Propuesta.Estado.PENDIENTE);

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.of(propuesta));

        ApiException ex = assertThrows(ApiException.class,
                () -> propuestaService.editarPropuesta(200L, 100L, buildEditarRequest("N", "D")));

        assertEquals("ESTADO_INVALIDO", ex.getCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
    }

    @Test
    void editarPropuesta_propuestaIdNoCoincide_lanzaNoEncontrado() {
        Estudiante estudiante = buildEstudiante(1L);
        Grupo grupo = buildGrupo(10L, "G-01");
        GrupoIntegrante gi = buildGrupoIntegrante(estudiante, grupo);
        Propuesta propuesta = buildPropuesta(100L, grupo, Propuesta.Estado.OBSERVADO);

        when(estudianteRepository.findByUsuarioId(200L)).thenReturn(Optional.of(estudiante));
        when(grupoIntegranteRepository.findFirstByEstudianteIdOrderByIdDesc(1L)).thenReturn(Optional.of(gi));
        when(propuestaRepository.findByGrupoId(10L)).thenReturn(Optional.of(propuesta));

        ApiException ex = assertThrows(ApiException.class,
                () -> propuestaService.editarPropuesta(200L, 999L, buildEditarRequest("N", "D")));

        assertEquals("NO_ENCONTRADO", ex.getCode());
    }

    // --- helpers ---

    private Estudiante buildEstudiante(Long id) {
        Usuario u = new Usuario();
        u.setId(200L);
        u.setNombre("Test Estudiante");
        Estudiante e = new Estudiante();
        e.setId(id);
        e.setCodigoEstudiante("2024001");
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

    private GrupoIntegrante buildGrupoIntegrante(Estudiante e, Grupo g) {
        GrupoIntegrante gi = new GrupoIntegrante();
        gi.setId(1L);
        gi.setEstudiante(e);
        gi.setGrupo(g);
        return gi;
    }

    private Propuesta buildPropuesta(Long id, Grupo grupo, Propuesta.Estado estado) {
        Propuesta p = new Propuesta();
        p.setId(id);
        p.setGrupo(grupo);
        p.setNombre("Proyecto Test");
        p.setDescripcion("Descripción del proyecto.");
        p.setEstado(estado);
        p.setFechaEnvio(OffsetDateTime.now());
        return p;
    }

    private CrearPropuestaRequest buildRequest(String nombre, String descripcion) {
        CrearPropuestaRequest r = new CrearPropuestaRequest();
        r.setNombre(nombre);
        r.setDescripcion(descripcion);
        return r;
    }

    private EditarPropuestaRequest buildEditarRequest(String nombre, String descripcion) {
        EditarPropuestaRequest r = new EditarPropuestaRequest();
        r.setNombre(nombre);
        r.setDescripcion(descripcion);
        return r;
    }
}
