package vn.fsaproject.carental.service;

import vn.fsaproject.carental.dto.request.AuthenticationDTO;
import vn.fsaproject.carental.dto.request.IntrospectDTO;
import vn.fsaproject.carental.dto.response.AuthenticationResponse;
import vn.fsaproject.carental.dto.response.IntrospectResponse;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.exception.AppException;
import vn.fsaproject.carental.exception.ErrorCode;
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

@Service
public class AuthenticationService {
    @Autowired
    UserDAO userDAO;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    @Value("${jesse.jwt.base64-secret}")
    private String SIGN_KEY;
    @Autowired
    @Value("${jesse.jwt.token-validity-in-seconds}")
    private int expireTime;
    public AuthenticationResponse login(AuthenticationDTO request){
        var user = userDAO.findByName(request.getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        String token = generateToken(user);
        return new AuthenticationResponse(authenticated,token);
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getName())
                .issuer("car_rental.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(expireTime, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope","phan quyen o day")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject object = new JWSObject(header,payload);

        try {
            object.sign(new MACSigner(SIGN_KEY.getBytes(StandardCharsets.UTF_8)));
            return object.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
    public IntrospectResponse introspect(IntrospectDTO request)
            throws ParseException, JOSEException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes(StandardCharsets.UTF_8));

        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean verified = signedJWT.verify(verifier);

        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        return IntrospectResponse.builder()
                .valid(verified && expiredTime.after(new Date()))
                .build();
    }

}
