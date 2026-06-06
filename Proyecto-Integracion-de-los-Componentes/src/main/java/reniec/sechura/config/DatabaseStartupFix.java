package reniec.sechura.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseStartupFix {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseStartupFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ajustarColumnasEnum() {
        ejecutarSiExiste("ALTER TABLE tramite MODIFY estado VARCHAR(30)");
        ejecutarSiExiste("ALTER TABLE cita MODIFY estado_cita VARCHAR(30)");
    }

    private void ejecutarSiExiste(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            // Si la tabla aún no existe o la columna ya está ajustada, no detenemos el arranque.
        }
    }
}
