package com.zawser.DATN.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.zawser.DATN.dto.request.AuthenticationRequest;
import com.zawser.DATN.dto.request.IntrospectRequest;
import com.zawser.DATN.dto.request.LogoutRequest;
import com.zawser.DATN.dto.response.AuthenticationResponse;
import com.zawser.DATN.dto.response.IntrospectResponse;
import com.zawser.DATN.entity.InvalidatedToken;
import com.zawser.DATN.entity.User;
import com.zawser.DATN.exception.AppException;
import com.zawser.DATN.exception.ErrorCode;
import com.zawser.DATN.repository.InvalidatedTokenRepository;
import com.zawser.DATN.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;


    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request ) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        boolean authenticated = passwordEncoder.matches(request.getPassword(),
                user.getPassword());


        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
                isValid = false;
        }


        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

    }

    private String generateToken( User user ) {
//        Tạo Header cho JWT
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

//        Tạo Body cho JWT
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("zawser.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();


//        Tao Payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

//        Tạo JWT
        JWSObject jwsObject = new JWSObject(header, payload);

//        Ký
        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot sign JWT object", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope( User user ) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }


    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {


        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified =  signedJWT.verify(verifier);
        if(!(verified && expiration.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        return signedJWT;
    }
}
