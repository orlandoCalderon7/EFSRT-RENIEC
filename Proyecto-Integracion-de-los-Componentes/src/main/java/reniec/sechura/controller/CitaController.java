package reniec.sechura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reniec.sechura.domain.Cita;
import reniec.sechura.domain.Horario;
import reniec.sechura.service.CitaService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping("/horarios/{idOficina}")
    public ResponseEntity<List<Horario>> listarHorarios(@PathVariable Integer idOficina) {
        return ResponseEntity.ok(citaService.listarHorariosDisponibles(idOficina));
    }

    @GetMapping("/ciudadano/{idCiudadano}")
    public ResponseEntity<List<Cita>> listarCitasCiudadano(@PathVariable Integer idCiudadano) {
        return ResponseEntity.ok(citaService.listarCitasPorCiudadano(idCiudadano));
    }

    @GetMapping("/ciudadano/{idCiudadano}/activa")
    public ResponseEntity<Map<String, Object>> obtenerCitaActiva(@PathVariable Integer idCiudadano) {
        Optional<Cita> citaActiva = citaService.obtenerCitaActivaCiudadano(idCiudadano);
        Map<String, Object> response = new HashMap<>();
        response.put("tieneCitaActiva", citaActiva.isPresent());
        response.put("cita", citaActiva.orElse(null));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservarCita(
            @RequestParam Integer idTramite,
            @RequestParam Integer idHorario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCita) {
        try {
            Cita cita = citaService.reservarCita(idTramite, idHorario, fechaCita);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{idCita}/reprogramar")
    public ResponseEntity<?> reprogramarCita(
            @PathVariable Integer idCita,
            @RequestParam Integer idHorario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCita) {
        try {
            Cita cita = citaService.reprogramarCita(idCita, idHorario, fechaCita);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
