package reniec.sechura.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class VerificacionBiofacial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVerificacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUsuario")
    private Ciudadano ciudadano;

    private LocalDateTime fechaVerificacion;
    private Boolean aprobado;
    private Double puntajeCoincidencia;
    private String proveedor;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @PrePersist
    protected void onCreate() {
        this.fechaVerificacion = LocalDateTime.now();
    }

    public Integer getIdVerificacion() { return idVerificacion; }
    public void setIdVerificacion(Integer idVerificacion) { this.idVerificacion = idVerificacion; }

    public Ciudadano getCiudadano() { return ciudadano; }
    public void setCiudadano(Ciudadano ciudadano) { this.ciudadano = ciudadano; }

    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }

    public Boolean getAprobado() { return aprobado; }
    public void setAprobado(Boolean aprobado) { this.aprobado = aprobado; }

    public Double getPuntajeCoincidencia() { return puntajeCoincidencia; }
    public void setPuntajeCoincidencia(Double puntajeCoincidencia) { this.puntajeCoincidencia = puntajeCoincidencia; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
