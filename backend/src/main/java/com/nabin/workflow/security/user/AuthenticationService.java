package com.nabin.workflow.security.user;

import com.nabin.workflow.dto.request.UserLoginDTO;
import com.nabin.workflow.dto.response.LoginResponseDTO;
import com.nabin.workflow.dto.response.UserResponseDTO;
import com.nabin.workflow.entities.RefreshToken;
import com.nabin.workflow.entities.User;
import com.nabin.workflow.exception.UnauthorizedException;
import com.nabin.workflow.mapper.DTOMapper;
import com.nabin.workflow.repository.UserRepository;
import com.nabin.workflow.security.jwt.JwtTokenProvider;
import com.nabin.workflow.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final DTOMapper dtoMapper;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponseDTO authenticateUser(UserLoginDTO loginDTO, HttpServletRequest request) {
        log.info("Authenticating user: {}", loginDTO.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            // Get user principal
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Get user from database
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            // Check if account is enabled
            if (!user.getEnabled()) {
                log.warn("Login attempt for disabled account: {}", loginDTO.getEmail());
                throw new UnauthorizedException("Account is disabled");
            }

            // Generate JWT access token
            String accessToken = jwtTokenProvider.generateTokenFromUserPrincipal(userPrincipal);

            // Generate refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), request);

            log.info("User authenticated successfully: {}", loginDTO.getEmail());

            UserResponseDTO userResponse = dtoMapper.toUserResponseDTO(user);

            return LoginResponseDTO.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .type("Bearer")
                    .user(userResponse)
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginDTO.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}