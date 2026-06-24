package pe.tecsup.proyectos.catalogo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.tecsup.proyectos.catalogo.dto.ProyectoDetalleDto;
import pe.tecsup.proyectos.catalogo.dto.ProyectoListaDto;
import pe.tecsup.proyectos.catalogo.service.CatalogoService;
import pe.tecsup.proyectos.shared.response.ApiResponse;
import pe.tecsup.proyectos.shared.response.PaginaDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService cataloService;

    @GetMapping("/ciclos")
    public ResponseEntity<ApiResponse<List<String>>> ciclos() {
        return ResponseEntity.ok(ApiResponse.success(cataloService.listarCiclos()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginaDto<ProyectoListaDto>>> listar(
            @RequestParam(required = false) String ciclo,
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PaginaDto<ProyectoListaDto> data = cataloService.listarProyectos(ciclo, origen, busqueda, page, size);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProyectoDetalleDto>> detalle(@PathVariable Long id) {
        ProyectoDetalleDto data = cataloService.obtenerDetalle(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/mi-proyecto")
    public ResponseEntity<ApiResponse<ProyectoDetalleDto>> miProyecto(
            @AuthenticationPrincipal String subject) {
        Long usuarioId = Long.parseLong(subject);
        return ResponseEntity.ok(ApiResponse.success(cataloService.getMiProyecto(usuarioId)));
    }

    @PutMapping("/{id}/url")
    public ResponseEntity<ApiResponse<ProyectoDetalleDto>> actualizarUrl(
            @AuthenticationPrincipal String subject,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long usuarioId = Long.parseLong(subject);
        String url = body.getOrDefault("url", "").strip();
        return ResponseEntity.ok(ApiResponse.success(cataloService.actualizarUrl(usuarioId, id, url)));
    }
}
