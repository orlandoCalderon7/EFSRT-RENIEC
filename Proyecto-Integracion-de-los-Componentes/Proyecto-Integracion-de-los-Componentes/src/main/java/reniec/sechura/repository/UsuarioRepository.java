package reniec.sechura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reniec.sechura.domain.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
}
