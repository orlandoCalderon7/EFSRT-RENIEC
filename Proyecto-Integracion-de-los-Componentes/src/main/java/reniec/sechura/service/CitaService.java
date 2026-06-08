// Servicio para gestionar la lógica de negocio relacionada con las citas en el sistema de RENIEC Sechura
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    public List<Horario> listarHorariosDisponibles(Integer idOficina) {
        return horarioRepository.findByOficinaIdOficinaAndDisponibleTrue(idOficina);
    }

    public List<Cita> listarCitasPorCiudadano(Integer idCiudadano) {
        normalizarCitasDeCiudadano(idCiudadano);
        return citaRepository.findByTramiteCiudadanoIdUsuario(idCiudadano);
    }

    public Optional<Cita> obtenerCitaActivaCiudadano(Integer idCiudadano) {
        normalizarCitasDeCiudadano(idCiudadano);
        return citaRepository.findByTramiteCiudadanoIdUsuario(idCiudadano)
                .stream()
                .filter(this::esCitaActiva)
                .min(Comparator
                        .comparing(Cita::getFechaCita, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Cita::getHoraCita, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    @Transactional
    public Cita reservarCita(Integer idTramite, Integer idHorario, LocalDate fechaCita) {
        Tramite tramite = tramiteRepository.findById(idTramite)
                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

        if (tramite.getEstado() == EstadoTramite.CANCELADO) {
            throw new RuntimeException("No se puede reservar cita para un trámite cancelado.");
        }

        Integer idCiudadano = tramite.getCiudadano().getIdUsuario();
        Optional<Cita> citaActivaCiudadano = obtenerCitaActivaCiudadano(idCiudadano);
        if (citaActivaCiudadano.isPresent()) {
            Cita citaActiva = citaActivaCiudadano.get();
            if (!citaActiva.getTramite().getIdTramite().equals(idTramite)) {
                throw new RuntimeException("El ciudadano ya tiene una cita pendiente. No puede registrar una nueva cita hasta que la cita actual culmine o sea reprogramada.");
            }
            throw new RuntimeException("Este trámite ya tiene una cita activa registrada.");
        }

        Horario horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        LocalDate fechaFinal = fechaCita != null ? fechaCita : calcularProximaFechaParaHorario(horario);
        validarFechaHoraFutura(fechaFinal, horario);

        Optional<Cita> citaExistenteOpt = citaRepository.findByTramiteIdTramite(idTramite);
        Cita cita;
        if (citaExistenteOpt.isPresent()) {
            cita = citaExistenteOpt.get();
            completarDatosCitaSiFaltan(cita);
            if (esCitaActiva(cita)) {
                throw new RuntimeException("El trámite ya tiene una cita activa registrada.");
            }
        } else {
            cita = new Cita();
            cita.setTramite(tramite);
        }

        ocuparHorario(horario);

        cita.setHorario(horario);
        cita.setFechaCita(fechaFinal);
        cita.setHoraCita(horario.getHoraInicio());
        cita.setEstadoCita(Cita.EstadoCita.RESERVADA);
        cita.setTicketDigital("QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return citaRepository.save(cita);
    }

    @Transactional
    public Cita reprogramarCita(Integer idCita, Integer idHorario, LocalDate fechaCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        completarDatosCitaSiFaltan(cita);

        if (cita.getTramite() != null && cita.getTramite().getEstado() == EstadoTramite.CANCELADO) {
            throw new RuntimeException("No se puede reprogramar una cita de un trámite cancelado.");
        }

        if (cita.getEstadoCita() == Cita.EstadoCita.CANCELADA) {
            throw new RuntimeException("No se puede reprogramar una cita cancelada. Debe iniciar un nuevo trámite.");
        }

        Horario nuevoHorario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        LocalDate fechaFinal = fechaCita != null ? fechaCita : calcularProximaFechaParaHorario(nuevoHorario);
        validarFechaHoraFutura(fechaFinal, nuevoHorario);

        Horario horarioAnterior = cita.getHorario();
        boolean citaEstabaReservada = cita.getEstadoCita() == Cita.EstadoCita.RESERVADA && !esCitaVencida(cita);
        boolean cambiaHorario = horarioAnterior == null
                || horarioAnterior.getIdHorario() == null
                || !horarioAnterior.getIdHorario().equals(nuevoHorario.getIdHorario());

        if (citaEstabaReservada && cambiaHorario) {
            liberarHorario(horarioAnterior);
        }

        if (!citaEstabaReservada || cambiaHorario) {
            ocuparHorario(nuevoHorario);
        }

        cita.setHorario(nuevoHorario);
        cita.setFechaCita(fechaFinal);
        cita.setHoraCita(nuevoHorario.getHoraInicio());
        cita.setEstadoCita(Cita.EstadoCita.RESERVADA);
        if (cita.getTicketDigital() == null || cita.getTicketDigital().isBlank()) {
            cita.setTicketDigital("QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        return citaRepository.save(cita);
    }

    private void normalizarCitasDeCiudadano(Integer idCiudadano) {
        List<Cita> citas = citaRepository.findByTramiteCiudadanoIdUsuario(idCiudadano);
        boolean huboCambios = false;

        for (Cita cita : citas) {
            if (completarDatosCitaSiFaltan(cita)) {
                huboCambios = true;
            }

            if (cita.getTramite() != null && cita.getTramite().getEstado() == EstadoTramite.CANCELADO
                    && cita.getEstadoCita() == Cita.EstadoCita.RESERVADA) {
                liberarHorario(cita.getHorario());
                cita.setEstadoCita(Cita.EstadoCita.CANCELADA);
                huboCambios = true;
            }

            if (cita.getEstadoCita() == Cita.EstadoCita.RESERVADA && esCitaVencida(cita)) {
                liberarHorario(cita.getHorario());
                cita.setEstadoCita(Cita.EstadoCita.FINALIZADA);
                huboCambios = true;
            }
        }

        if (huboCambios) {
            citaRepository.saveAll(citas);
        }
    }

    private boolean completarDatosCitaSiFaltan(Cita cita) {
        if (cita == null || cita.getHorario() == null) {
            return false;
        }

        boolean cambio = false;
        Horario horario = cita.getHorario();

        if (cita.getHoraCita() == null && horario.getHoraInicio() != null) {
            cita.setHoraCita(horario.getHoraInicio());
            cambio = true;
        }

        if (cita.getFechaCita() == null) {
            cita.setFechaCita(calcularProximaFechaParaHorario(horario));
            cambio = true;
        }

        return cambio;
    }

    private boolean esCitaActiva(Cita cita) {
        return cita.getEstadoCita() == Cita.EstadoCita.RESERVADA && !esCitaVencida(cita);
    }

    private boolean esCitaVencida(Cita cita) {
        if (cita.getFechaCita() == null || cita.getHoraCita() == null) {
            return false;
        }
        LocalDateTime fechaHoraCita = LocalDateTime.of(cita.getFechaCita(), cita.getHoraCita());
        return fechaHoraCita.isBefore(LocalDateTime.now());
    }

    private void validarFechaHoraFutura(LocalDate fecha, Horario horario) {
        if (fecha == null || horario == null || horario.getHoraInicio() == null) {
            throw new RuntimeException("La fecha y el horario de la cita son obligatorios.");
        }

        LocalDateTime fechaHora = LocalDateTime.of(fecha, horario.getHoraInicio());
        if (!fechaHora.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("La cita debe programarse para una fecha y hora futura.");
        }
    }

    private void ocuparHorario(Horario horario) {
        if (horario == null) {
            throw new RuntimeException("Horario no encontrado");
        }

        Integer citasActuales = horario.getCitasActuales() != null ? horario.getCitasActuales() : 0;
        Integer capacidadMax = horario.getCapacidadMax() != null ? horario.getCapacidadMax() : 0;

        if (Boolean.FALSE.equals(horario.getDisponible()) || citasActuales >= capacidadMax) {
            throw new RuntimeException("Horario sin capacidad disponible");
        }

        horario.setCitasActuales(citasActuales + 1);
        if (horario.getCitasActuales() >= capacidadMax) {
            horario.setDisponible(false);
        }
        horarioRepository.save(horario);
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

    private LocalDate calcularProximaFechaParaHorario(Horario horario) {
        LocalDate hoy = LocalDate.now();
        int diaObjetivo = convertirDiaSemanaANumero(horario.getDiaSemana());

        if (diaObjetivo == 0) {
            return hoy;
        }

        int diaActual = hoy.getDayOfWeek().getValue();
        int diferencia = diaObjetivo - diaActual;
        if (diferencia < 0) {
            diferencia += 7;
        }

        LocalDate fecha = hoy.plusDays(diferencia);
        LocalTime horaInicio = horario.getHoraInicio() != null ? horario.getHoraInicio() : LocalTime.MIN;
        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaInicio);

        if (!fechaHora.isAfter(LocalDateTime.now())) {
            fecha = fecha.plusWeeks(1);
        }

        return fecha;
    }

    private int convertirDiaSemanaANumero(String diaSemana) {
        if (diaSemana == null) {
            return 0;
        }

        String dia = diaSemana.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");

        switch (dia) {
            case "lunes": return 1;
            case "martes": return 2;
            case "miercoles": return 3;
            case "jueves": return 4;
            case "viernes": return 5;
            case "sabado": return 6;
            case "domingo": return 7;
            default: return 0;
        }
    }
}
