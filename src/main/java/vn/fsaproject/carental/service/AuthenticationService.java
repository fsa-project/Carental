package vn.fsaproject.carental.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.fsaproject.carental.dto.request.AuthenticationDTO;
import vn.fsaproject.carental.dto.request.IntrospectDTO;
import vn.fsaproject.carental.dto.request.LogoutDTO;
import vn.fsaproject.carental.dto.request.RefreshDTO;
import vn.fsaproject.carental.dto.response.AuthenticationResponse;
import vn.fsaproject.carental.dto.response.IntrospectResponse;
import vn.fsaproject.carental.entities.InvalidToken;
import vn.fsaproject.carental.entities.Role;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.exception.AppException;
import vn.fsaproject.carental.exception.ErrorCode;
import vn.fsaproject.carental.repository.InvalidTokenDAO;
import vn.fsaproject.carental.repository.UserDAO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticationService {
    InvalidTokenDAO invalidTokenDAO;
    UserDAO userDAO;

    @NonFinal
    @Value("${jesse.jwt.refreshable-duration}")
    protected int refreshabel_duration;
    @NonFinal
    @Value("${jesse.jwt.base64-secret}")
    protected String SIGN_KEY;
    @NonFinal
    @Value("${jesse.jwt.token-validity-in-seconds}")
    protected int expireTime;

    public AuthenticationResponse login(AuthenticationDTO request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        var user = userDAO.findByName(request.getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        String token = generateToken(user);
        return new AuthenticationResponse(true, token);
    }

    public void logout(LogoutDTO request) throws ParseException, JOSEException {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidToken invalidToken =
                    InvalidToken.builder().id(jit).expireTime(expiryTime).build();

            invalidTokenDAO.save(invalidToken);
            log.info("invalid token saved");

    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getName())
                .issuer("car_rental.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(expireTime, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject object = new JWSObject(header, payload);

        try {
            object.sign(new MACSigner(SIGN_KEY.getBytes(StandardCharsets.UTF_8)));
            return object.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
    public AuthenticationResponse refreshToken(RefreshDTO request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken =
                InvalidToken.builder().id(jit).expireTime(expiryTime).build();

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userDAO.findByName(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        stringJoiner.add(user.getRole().getName());
        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(refreshabel_duration, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidTokenDAO.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AuthenticationServiceException(ErrorCode.EXPIRED_TOKEN.getMessage());

        return signedJWT;
    }

    public IntrospectResponse introspect(IntrospectDTO request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }
    public Role getUserRole(Long userId) {
        return userDAO.findById(userId)
                .map(User::getRole)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
