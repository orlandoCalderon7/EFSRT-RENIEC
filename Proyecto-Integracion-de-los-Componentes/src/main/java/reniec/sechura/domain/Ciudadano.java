package reniec.sechura.domain;

import jakarta.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "idUsuario")
public class Ciudadano extends Usuario {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idPersona", referencedColumnName = "idPersona")
    private Persona datosPersonales;

    @Column(columnDefinition = "TEXT")
    private String historialDNI;

    // Getters and Setters

    public Persona getDatosPersonales() { return datosPersonales; }
    public void setDatosPersonales(Persona datosPersonales) { this.datosPersonales = datosPersonales; }

    public String getHistorialDNI() { return historialDNI; }
    public void setHistorialDNI(String historialDNI) { this.historialDNI = historialDNI; }
}
