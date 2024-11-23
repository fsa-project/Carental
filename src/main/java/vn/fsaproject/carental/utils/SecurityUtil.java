package vn.fsaproject.carental.utils;

import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.fsaproject.carental.dto.response.ResLoginDTO;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Service
public class SecurityUtil {

    private final JwtEncoder jwtEncoder;

    public SecurityUtil(final JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${jesse.jwt.base64-secret}")
    private String jwtKey;

    @Value("${jesse.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${jesse.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public String createAccessToken(String email, ResLoginDTO dto) {

        ResLoginDTO.UserInsideToken userInsideToken = new ResLoginDTO.UserInsideToken();
        userInsideToken.setEmail(dto.getUser().getEmail());
        userInsideToken.setName(dto.getUser().getName());
        userInsideToken.setId(dto.getUser().getId());

        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        //hardcode permission
        List<String> listAuthority = new ArrayList<String>();

        listAuthority.add("ROLE_USER");
        listAuthority.add("ROLE_ADMIN");


        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userInsideToken)
                .claim("authorities", listAuthority)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    public String createRefreshToken(String email, ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userInsideToken = new ResLoginDTO.UserInsideToken();
        userInsideToken.setEmail(dto.getUser().getEmail());
        userInsideToken.setName(dto.getUser().getName());
        userInsideToken.setId(dto.getUser().getId());

        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userInsideToken)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(refreshToken);
        } catch (Exception e) {
            System.out.println(">>> Refresh token error: " + e.getMessage());
            throw e;
        }

    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }


    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof Jwt)
                .map(authentication -> (String) authentication.getCredentials());
    }

    public static  Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }
    // Lấy id của User đang trong phiên đăng nhập
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, Object> user = jwt.getClaim("user");
        return Long.valueOf(user.get("id").toString()); // Retrieve userId directly
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

//    public static boolean isAuthenticated() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication != null && getAuthorities(authentication).noneMatch(AuthoritiesCon)
//    }

//    private static Stream<String> getAuthorities(Authentication authentication) {
//        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
//    }
//
//    public static boolean hasCurrentUserThisAuthority(String authority) {
//        return hasCurrentUserAnyOfAuthorities(authority);
//    }
//
//    private static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return (
//                authentication != null && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority))
//                );
//    }
//
//    public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
//        return !hasCurrentUserAnyOfAuthorities(authorities);
//    }
}