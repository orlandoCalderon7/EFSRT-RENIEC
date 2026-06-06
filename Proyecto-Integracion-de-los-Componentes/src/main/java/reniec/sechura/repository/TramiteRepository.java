package reniec.sechura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reniec.sechura.domain.Tramite;
import java.util.Optional;
import java.util.List;

public interface TramiteRepository extends JpaRepository<Tramite, Integer> {
    Optional<Tramite> findByCodigoTramite(String codigoTramite);
    List<Tramite> findByCiudadanoIdUsuario(Integer idUsuario);
}
