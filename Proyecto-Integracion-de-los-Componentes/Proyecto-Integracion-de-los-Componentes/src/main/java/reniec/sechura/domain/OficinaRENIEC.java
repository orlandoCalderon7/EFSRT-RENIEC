package reniec.sechura.domain;

import jakarta.persistence.*;

@Entity
public class OficinaRENIEC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOficina;

    @Column(nullable = false)
    private String nombre;

    private String distrito;
    private String provincia;
    private String direccion;
    private String telefono;
    private Integer capacidadDiaria;

    // Getters and Setters

    public Integer getIdOficina() { return idOficina; }
    public void setIdOficina(Integer idOficina) { this.idOficina = idOficina; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getCapacidadDiaria() { return capacidadDiaria; }
    public void setCapacidadDiaria(Integer capacidadDiaria) { this.capacidadDiaria = capacidadDiaria; }
}
