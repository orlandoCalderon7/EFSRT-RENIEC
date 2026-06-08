// Entidad que representa un horario disponible para citas en el sistema de RENIEC Sechura
package reniec.sechura.domain;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHorario;

    @ManyToOne
    @JoinColumn(name = "idOficina", nullable = false)
    private OficinaRENIEC oficina;

    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer capacidadMax;
    private Integer citasActuales = 0;
    private Boolean disponible = true;

    // Getters and Setters

    public Integer getIdHorario() { return idHorario; }
    public void setIdHorario(Integer idHorario) { this.idHorario = idHorario; }

    public OficinaRENIEC getOficina() { return oficina; }
    public void setOficina(OficinaRENIEC oficina) { this.oficina = oficina; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Integer getCapacidadMax() { return capacidadMax; }
    public void setCapacidadMax(Integer capacidadMax) { this.capacidadMax = capacidadMax; }

    public Integer getCitasActuales() { return citasActuales; }
    public void setCitasActuales(Integer citasActuales) { this.citasActuales = citasActuales; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}
