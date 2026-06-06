package reniec.sechura.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reniec.sechura.domain.Cita;
import reniec.sechura.domain.EstadoTramite;
import reniec.sechura.domain.Horario;
import reniec.sechura.domain.Tramite;
import reniec.sechura.repository.CitaRepository;
import reniec.sechura.repository.HorarioRepository;
import reniec.sechura.repository.TramiteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TramiteService {

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    public Tramite crearTramite(Tramite tramite) {
        tramite.setCodigoTramite("TRM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tramite.setEstado(EstadoTramite.PENDIENTE);
        return tramiteRepository.save(tramite);
    }

    public List<Tramite> listarTramitesPorCiudadano(Integer idCiudadano) {
        return tramiteRepository.findByCiudadanoIdUsuario(idCiudadano);
    }

    public Tramite obtenerTramite(Integer idTramite) {
        return tramiteRepository.findById(idTramite)
                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));
    }

    public Tramite actualizarEstadoTramite(Integer idTramite, EstadoTramite nuevoEstado) {
        Tramite tramite = obtenerTramite(idTramite);
        tramite.setEstado(nuevoEstado);
        return tramiteRepository.save(tramite);
    }

    @Transactional
    public Tramite cancelarTramite(Integer idTramite) {
        Tramite tramite = obtenerTramite(idTramite);

        if (tramite.getEstado() == EstadoTramite.ENTREGADO) {
            throw new RuntimeException("No se puede cancelar un trámite entregado.");
        }

        tramite.setEstado(EstadoTramite.CANCELADO);
        Tramite tramiteGuardado = tramiteRepository.save(tramite);

        Optional<Cita> citaOpt = citaRepository.findByTramiteIdTramite(idTramite);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            if (cita.getEstadoCita() == Cita.EstadoCita.RESERVADA) {
                liberarHorario(cita.getHorario());
            }
            cita.setEstadoCita(Cita.EstadoCita.CANCELADA);
            citaRepository.save(cita);
        }

        return tramiteGuardado;
    }

    private void liberarHorario(Horario horario) {
        if (horario == null) {
            return;
        }

        Integer citasActuales = horario.getCitasActuales() != null ? horario.getCitasActuales() : 0;
        horario.setCitasActuales(Math.max(0, citasActuales - 1));
        horario.setDisponible(true);
        horarioRepository.save(horario);
    }
}
