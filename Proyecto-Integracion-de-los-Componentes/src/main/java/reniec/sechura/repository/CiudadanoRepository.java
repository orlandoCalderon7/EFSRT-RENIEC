package reniec.sechura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reniec.sechura.domain.Ciudadano;
import java.util.Optional;

public interface CiudadanoRepository extends JpaRepository<Ciudadano, Integer> {
    Optional<Ciudadano> findByDatosPersonalesDni(String dni);
}
