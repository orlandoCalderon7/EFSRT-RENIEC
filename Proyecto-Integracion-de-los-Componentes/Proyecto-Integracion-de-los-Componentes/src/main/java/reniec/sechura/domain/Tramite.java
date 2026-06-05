package reniec.sechura.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTramite;

    @ManyToOne
    @JoinColumn(name = "idCiudadano", nullable = false)
    private Ciudadano ciudadano;

    @ManyToOne
    @JoinColumn(name = "idTipoTramite", nullable = false)
    private TipoTramite tipoTramite;

    @Column(unique = true, nullable = false)
    private String codigoTramite;

    private LocalDate fechaSolicitud;
    private LocalDate fechaEntregaEst;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)")
    private EstadoTramite estado = EstadoTramite.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "idAdminGestor")
    private Administrador adminGestor;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDate.now();
    }

    public Integer getIdTramite() { return idTramite; }
    public void setIdTramite(Integer idTramite) { this.idTramite = idTramite; }

    public Ciudadano getCiudadano() { return ciudadano; }
    public void setCiudadano(Ciudadano ciudadano) { this.ciudadano = ciudadano; }

    public TipoTramite getTipoTramite() { return tipoTramite; }
    public void setTipoTramite(TipoTramite tipoTramite) { this.tipoTramite = tipoTramite; }

    public String getCodigoTramite() { return codigoTramite; }
    public void setCodigoTramite(String codigoTramite) { this.codigoTramite = codigoTramite; }

    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDate getFechaEntregaEst() { return fechaEntregaEst; }
    public void setFechaEntregaEst(LocalDate fechaEntregaEst) { this.fechaEntregaEst = fechaEntregaEst; }

    public EstadoTramite getEstado() { return estado; }
    public void setEstado(EstadoTramite estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Administrador getAdminGestor() { return adminGestor; }
    public void setAdminGestor(Administrador adminGestor) { this.adminGestor = adminGestor; }
}
