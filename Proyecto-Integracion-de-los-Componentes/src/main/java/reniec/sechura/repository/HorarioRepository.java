// Repositorio para gestionar los horarios disponibles en el sistema de RENIEC Sechura
package reniec.sechura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reniec.sechura.domain.Horario;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    List<Horario> findByOficinaIdOficinaAndDisponibleTrue(Integer idOficina);
}
