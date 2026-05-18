package app_jwt.auth_service.shared.infrastructure.security;

import app_jwt.auth_service.shared.domain.model.Usuario;
import app_jwt.auth_service.shared.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String getToken(UserDetails user, Usuario usuario) {
        if (usuario.getEmpresaId() == null) {
            throw new IllegalStateException("No se puede generar token sin empresaId");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("empresaId", usuario.getEmpresaId());
        claims.put("role", usuario.getRole().name());
        claims.put("email", usuario.getCorreo());
        claims.put("nombre", usuario.getNombre());
        claims.put("apellido", usuario.getApellido());

        return generateToken(claims, user.getUsername());
    }

    private String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret demasiado corto para HS256 (min 256 bits en base64)");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            Long empresaId = getEmpresaId(token);
            return username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token) &&
                    empresaId != null;
        } catch (Exception e) {
            return false;
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Long getEmpresaId(String token) {
        Number n = getClaim(token, c -> c.get("empresaId", Number.class));
        return n != null ? n.longValue() : null;
    }

    public Long getUserId(String token) {
        Number n = getClaim(token, c -> c.get("userId", Number.class));
        return n != null ? n.longValue() : null;
    }

    public String getRole(String token) {
        return getClaim(token, c -> c.get("role", String.class));
    }

    private boolean isTokenExpired(String token) {
        return getClaimFromToken(token, Claims::getExpiration).before(new Date());
    }
}
