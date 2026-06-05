package reniec.sechura.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import reniec.sechura.domain.Ciudadano;
import reniec.sechura.domain.Persona;
import reniec.sechura.domain.Usuario;
import reniec.sechura.repository.CiudadanoRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private CiudadanoRepository ciudadanoRepository;

    private final String API_TOKEN = "cGVydWRldnMucHJvZHVjdGlvbi5maXRjb2RlcnMuNmExMjkxOTExYzlhY2M1YmI0MjI2Yjg1";
    private final String API_URL = "https://api.perudevs.com/api/v1/dni/simple";

    @SuppressWarnings("unchecked")
    public Ciudadano autenticarPorDni(String dni) {
        // 1. Consultar si ya existe en nuestra BD local
        Optional<Ciudadano> ciudadanoOpt = ciudadanoRepository.findByDatosPersonalesDni(dni);
        if (ciudadanoOpt.isPresent()) {
            return ciudadanoOpt.get(); // Retorna el ciudadano existente
        }

        // 2. Si no existe, validar con la nueva API externa de PeruDevs
        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + "?document=" + dni + "&key=" + API_TOKEN;

        try {

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                // Extraer el resultado (asumiendo que viene en un campo "resultado" o está directo)
                Map<String, String> data;
                if (body.containsKey("resultado")) {
                    data = (Map<String, String>) body.get("resultado");
                } else {
                    data = (Map<String, String>) (Map) body;
                }

                if (data != null && data.containsKey("nombres")) {
                    // 3. Registrar automáticamente al nuevo ciudadano
                    Persona persona = new Persona();
                    persona.setDni(dni);
                    persona.setNombres(data.get("nombres"));
                    persona.setApellidos(data.get("apellido_paterno") + " " + data.get("apellido_materno"));

                    Ciudadano nuevoCiudadano = new Ciudadano();
                    nuevoCiudadano.setUsername(dni); // DNI como identificador de sesión
                    nuevoCiudadano.setPasswordHash("AUTO_LOGIN");
                    nuevoCiudadano.setRol("CIUDADANO");
                    nuevoCiudadano.setDatosPersonales(persona);

                    return ciudadanoRepository.save(nuevoCiudadano);
                } else {
                    throw new RuntimeException("DNI no encontrado en la base externa.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión al consultar el DNI: " + e.getMessage());
        }

        throw new RuntimeException("No se pudo validar el DNI proporcionado.");
    }
}
