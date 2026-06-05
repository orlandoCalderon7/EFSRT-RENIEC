package reniec.sechura.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotif;

    @ManyToOne
    @JoinColumn(name = "idTramite", nullable = false)
    private Tramite tramite;

    @ManyToOne
    @JoinColumn(name = "idCiudadano", nullable = false)
    private Ciudadano ciudadano;

    private String tipoNotif; // SMS, EMAIL, WEB

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    private LocalDateTime fechaEnvio;
    private Boolean leida = false;

    // Getters and Setters

    public Integer getIdNotif() { return idNotif; }
    public void setIdNotif(Integer idNotif) { this.idNotif = idNotif; }

    public Tramite getTramite() { return tramite; }
    public void setTramite(Tramite tramite) { this.tramite = tramite; }

    public Ciudadano getCiudadano() { return ciudadano; }
    public void setCiudadano(Ciudadano ciudadano) { this.ciudadano = ciudadano; }

    public String getTipoNotif() { return tipoNotif; }
    public void setTipoNotif(String tipoNotif) { this.tipoNotif = tipoNotif; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }
}
