package reniec.sechura.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reniec.sechura.domain.Ciudadano;
import reniec.sechura.domain.VerificacionBiofacial;
import reniec.sechura.repository.VerificacionBiofacialRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class BiofacialService {

    private static final String PROVEEDOR_SIMULADO = "SIMULADOR_RENIEC_BIOFACIAL";
    private static final double UMBRAL_APROBACION = 85.0;

    @Autowired
    private VerificacionBiofacialRepository verificacionBiofacialRepository;

    public Map<String, Object> verificarRostro(Ciudadano ciudadano, String imagenBase64) {
        if (ciudadano == null || ciudadano.getIdUsuario() == null) {
            throw new RuntimeException("Primero se debe validar el DNI del ciudadano.");
        }

        if (imagenBase64 == null || imagenBase64.isBlank()) {
            return registrarResultado(ciudadano, false, 0.0, "No se recibió imagen facial para validar.");
        }

        String imagenLimpia = imagenBase64.trim();
        boolean formatoValido = imagenLimpia.startsWith("data:image/jpeg;base64,")
                || imagenLimpia.startsWith("data:image/jpg;base64,")
                || imagenLimpia.startsWith("data:image/png;base64,");

        if (!formatoValido) {
            return registrarResultado(ciudadano, false, 20.0, "La imagen enviada no tiene un formato válido.");
        }

        String contenidoBase64 = imagenLimpia.substring(imagenLimpia.indexOf(',') + 1);
        if (contenidoBase64.length() < 5000) {
            return registrarResultado(ciudadano, false, 45.0, "La imagen facial no tiene suficiente calidad para la verificación.");
        }

        // Simulación controlada: en una implementación real aquí se consumiría el
        // web service biométrico facial de RENIEC usando certificados digitales.
        double puntaje = calcularPuntajeSimulado(contenidoBase64.length());
        boolean aprobado = puntaje >= UMBRAL_APROBACION;
        String mensaje = aprobado
                ? "El rostro capturado corresponde al titular del DNI consultado."
                : "El rostro capturado no supera el umbral mínimo de coincidencia.";

        return registrarResultado(ciudadano, aprobado, puntaje, mensaje);
    }

    private double calcularPuntajeSimulado(int longitudImagen) {
        double base = 91.0;
        double ajuste = Math.min(7.5, longitudImagen / 120000.0);
        return Math.round((base + ajuste) * 10.0) / 10.0;
    }

    private Map<String, Object> registrarResultado(Ciudadano ciudadano, boolean aprobado, double puntaje, String mensaje) {
        VerificacionBiofacial verificacion = new VerificacionBiofacial();
        verificacion.setCiudadano(ciudadano);
        verificacion.setAprobado(aprobado);
        verificacion.setPuntajeCoincidencia(puntaje);
        verificacion.setProveedor(PROVEEDOR_SIMULADO);
        verificacion.setMensaje(mensaje);

        VerificacionBiofacial guardada = verificacionBiofacialRepository.save(verificacion);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idVerificacion", guardada.getIdVerificacion());
        resultado.put("aprobado", aprobado);
        resultado.put("puntajeCoincidencia", puntaje);
        resultado.put("proveedor", PROVEEDOR_SIMULADO);
        resultado.put("mensaje", mensaje);
        return resultado;
    }
}
