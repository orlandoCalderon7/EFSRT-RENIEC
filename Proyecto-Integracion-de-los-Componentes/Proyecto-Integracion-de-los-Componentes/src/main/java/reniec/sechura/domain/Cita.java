package reniec.sechura.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCita;

    @OneToOne
    @JoinColumn(name = "idTramite", nullable = false)
    private Tramite tramite;

    @ManyToOne
    @JoinColumn(name = "idHorario", nullable = false)
    private Horario horario;

    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String ticketDigital;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)")
    private EstadoCita estadoCita = EstadoCita.RESERVADA;

    public Integer getIdCita() { return idCita; }
    public void setIdCita(Integer idCita) { this.idCita = idCita; }

    public Tramite getTramite() { return tramite; }
    public void setTramite(Tramite tramite) { this.tramite = tramite; }

    public Horario getHorario() { return horario; }
    public void setHorario(Horario horario) { this.horario = horario; }

    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

    public LocalTime getHoraCita() { return horaCita; }
    public void setHoraCita(LocalTime horaCita) { this.horaCita = horaCita; }

    public String getTicketDigital() { return ticketDigital; }
    public void setTicketDigital(String ticketDigital) { this.ticketDigital = ticketDigital; }

    public EstadoCita getEstadoCita() { return estadoCita; }
    public void setEstadoCita(EstadoCita estadoCita) { this.estadoCita = estadoCita; }

    public enum EstadoCita {
        RESERVADA, CANCELADA, ATENDIDA, FINALIZADA
    }
}
