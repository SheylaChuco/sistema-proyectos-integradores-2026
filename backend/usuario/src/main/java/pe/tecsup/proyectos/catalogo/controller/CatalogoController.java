package pe.tecsup.proyectos.catalogo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.tecsup.proyectos.catalogo.dto.ProyectoDetalleDto;
import pe.tecsup.proyectos.catalogo.dto.ProyectoListaDto;
import pe.tecsup.proyectos.catalogo.service.CatalogoService;
import pe.tecsup.proyectos.shared.response.ApiResponse;
import pe.tecsup.proyectos.shared.response.PaginaDto;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService cataloService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginaDto<ProyectoListaDto>>> listar(
            @RequestParam(required = false) String ciclo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int pagina) {
        PaginaDto<ProyectoListaDto> data = cataloService.listarProyectos(ciclo, estado, busqueda, pagina);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProyectoDetalleDto>> detalle(@PathVariable Long id) {
        ProyectoDetalleDto data = cataloService.obtenerDetalle(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
