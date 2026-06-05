package reniec.sechura.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "idPersona")
public class Pescador extends Persona {

    private String licencia;
    private String embarcacion;

    // Getters and Setters
    public String getLicencia() { return licencia; }
    public void setLicencia(String licencia) { this.licencia = licencia; }

    public String getEmbarcacion() { return embarcacion; }
    public void setEmbarcacion(String embarcacion) { this.embarcacion = embarcacion; }
}
