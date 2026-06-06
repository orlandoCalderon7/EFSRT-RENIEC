package reniec.sechura.domain;

import jakarta.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "idUsuario")
public class Administrador extends Usuario {

    private String idEmpleado;
    private String ventanilla;

    // Getters and Setters

    public String getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(String idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getVentanilla() { return ventanilla; }
    public void setVentanilla(String ventanilla) { this.ventanilla = ventanilla; }
}
