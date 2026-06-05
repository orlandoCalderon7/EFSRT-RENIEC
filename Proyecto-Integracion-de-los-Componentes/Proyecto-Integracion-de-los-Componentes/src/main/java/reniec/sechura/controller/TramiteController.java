package reniec.sechura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reniec.sechura.domain.EstadoTramite;
import reniec.sechura.domain.Tramite;
import reniec.sechura.service.TramiteService;

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    @Autowired
    private TramiteService tramiteService;

    @PostMapping
    public ResponseEntity<?> crearTramite(@RequestBody Tramite tramite) {
        try {
            Tramite nuevoTramite = tramiteService.crearTramite(tramite);
            return ResponseEntity.ok(nuevoTramite);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/ciudadano/{idCiudadano}")
    public ResponseEntity<List<Tramite>> listarTramitesCiudadano(@PathVariable Integer idCiudadano) {
        return ResponseEntity.ok(tramiteService.listarTramitesPorCiudadano(idCiudadano));
    }

    @GetMapping("/{idTramite}")
    public ResponseEntity<?> obtenerTramite(@PathVariable Integer idTramite) {
        try {
            return ResponseEntity.ok(tramiteService.obtenerTramite(idTramite));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{idTramite}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer idTramite, @RequestParam EstadoTramite estado) {
        try {
            return ResponseEntity.ok(tramiteService.actualizarEstadoTramite(idTramite, estado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{idTramite}/cancelar")
    public ResponseEntity<?> cancelarTramite(@PathVariable Integer idTramite) {
        try {
            return ResponseEntity.ok(tramiteService.cancelarTramite(idTramite));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
