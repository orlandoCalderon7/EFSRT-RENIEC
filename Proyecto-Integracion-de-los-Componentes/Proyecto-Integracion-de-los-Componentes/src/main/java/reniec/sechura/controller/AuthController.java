package reniec.sechura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reniec.sechura.domain.Ciudadano;
import reniec.sechura.service.BiofacialService;
import reniec.sechura.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BiofacialService biofacialService;

    @PostMapping("/login")
    public ResponseEntity<?> loginDni(@RequestParam String dni) {
        try {
            Ciudadano ciudadano = usuarioService.autenticarPorDni(dni);
            return ResponseEntity.ok(ciudadano);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/prevalidar")
    public ResponseEntity<?> prevalidarDni(@RequestParam String dni) {
        try {
            validarFormatoDni(dni);
            Ciudadano ciudadano = usuarioService.autenticarPorDni(dni);

            Map<String, Object> response = new HashMap<>();
            response.put("ciudadano", ciudadano);
            response.put("requiereBiofacial", true);
            response.put("mensaje", "DNI validado. Continúa con la validación biofacial.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/biofacial/verificar")
    public ResponseEntity<?> verificarBiofacial(@RequestBody BiofacialRequest request) {
        try {
            validarFormatoDni(request.getDni());
            Ciudadano ciudadano = usuarioService.autenticarPorDni(request.getDni());
            Map<String, Object> resultado = biofacialService.verificarRostro(ciudadano, request.getImagenBase64());
            resultado.put("ciudadano", ciudadano);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void validarFormatoDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new RuntimeException("El DNI debe tener 8 dígitos numéricos.");
        }
    }

    public static class BiofacialRequest {
        private String dni;
        private String imagenBase64;

        public String getDni() { return dni; }
        public void setDni(String dni) { this.dni = dni; }

        public String getImagenBase64() { return imagenBase64; }
        public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
    }
}
