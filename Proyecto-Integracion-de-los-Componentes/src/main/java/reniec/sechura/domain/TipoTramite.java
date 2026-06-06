package reniec.sechura.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class TipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTipo;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private BigDecimal costo;

    private Integer diasProceso;

    @Column(columnDefinition = "TEXT")
    private String requisitos;

    // Getters and Setters

    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public Integer getDiasProceso() { return diasProceso; }
    public void setDiasProceso(Integer diasProceso) { this.diasProceso = diasProceso; }

    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
}
