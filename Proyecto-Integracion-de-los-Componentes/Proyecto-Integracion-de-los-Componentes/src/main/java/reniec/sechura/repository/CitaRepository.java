package reniec.sechura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reniec.sechura.domain.Cita;

import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    Optional<Cita> findByTramiteIdTramite(Integer idTramite);
    List<Cita> findByTramiteCiudadanoIdUsuario(Integer idUsuario);
}
