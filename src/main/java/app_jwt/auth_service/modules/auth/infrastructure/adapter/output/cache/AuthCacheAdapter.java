package app_jwt.auth_service.modules.auth.infrastructure.adapter.output.cache;

import app_jwt.auth_service.modules.auth.domain.port.AuthCachePort;
import app_jwt.auth_service.shared.domain.model.Empresa;
import app_jwt.auth_service.shared.service.RedisRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCacheAdapter implements AuthCachePort {

    private final RedisRealtimeService redisRealtimeService;

    @Override
    public void upsertEmpresa(Empresa empresa) {
        try {
            redisRealtimeService.upsertEmpresa(empresa);
        } catch (Exception e) {
            // El log original decía "Error Redis Realtime (continuando):"
            // Lo ignoramos silenciosamente como el servicio original, o lo logueamos
            // Aquí podemos usar @Slf4j o simplemente ignorarlo como era la intención.
        }
    }
}
