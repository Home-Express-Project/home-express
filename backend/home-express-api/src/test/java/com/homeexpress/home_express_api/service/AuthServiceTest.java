package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.config.JwtTokenProvider;
import com.homeexpress.home_express_api.dto.request.LoginRequest;
import com.homeexpress.home_express_api.dto.request.RegisterRequest;
import com.homeexpress.home_express_api.dto.response.AuthResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.repository.CustomerRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest customerRegisterRequest;
    private RegisterRequest transportRegisterRequest;
    private LoginRequest loginRequest;
    private User mockUser;
    private Customer mockCustomer;
    private Transport mockTransport;
    private UserSession mockSession;

    @BeforeEach
    void setUp() {
        // Setup customer register request
        customerRegisterRequest = new RegisterRequest();
        customerRegisterRequest.setEmail("customer@test.com");
        customerRegisterRequest.setPassword("password123");
        customerRegisterRequest.setRole("CUSTOMER");
        customerRegisterRequest.setFullName("John Doe");
        customerRegisterRequest.setPhone("0901234567");
        customerRegisterRequest.setAddress("123 Test Street");

        // Setup transport register request
        transportRegisterRequest = new RegisterRequest();
        transportRegisterRequest.setEmail("transport@test.com");
        transportRegisterRequest.setPassword("password123");
        transportRegisterRequest.setRole("TRANSPORT");
        transportRegisterRequest.setCompanyName("Test Transport Co.");
        transportRegisterRequest.setBusinessLicenseNumber("BL123456");
        transportRegisterRequest.setPhone("0912345678");
        transportRegisterRequest.setAddress("456 Business Ave");
        transportRegisterRequest.setCity("Ho Chi Minh City");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("customer@test.com");
        loginRequest.setPassword("password123");

        // Setup mock user
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setEmail("customer@test.com");
        mockUser.setPasswordHash("$2a$10$hashedPassword");
        mockUser.setRole(UserRole.CUSTOMER);
        mockUser.setIsActive(true);
        mockUser.setIsVerified(false);

        // Setup mock customer
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);
        mockCustomer.setFullName("John Doe");
        mockCustomer.setPhone("0901234567");

        // Setup mock transport
        mockTransport = new Transport();
        mockTransport.setTransportId(2L);
        mockTransport.setCompanyName("Test Transport Co.");
        mockTransport.setBusinessLicenseNumber("BL123456");
        mockTransport.setPhone("0912345678");

        // Setup mock session
        mockSession = new UserSession();
        mockSession.setSessionId("session-uuid-1");
        mockSession.setUser(mockUser);
        mockSession.setPlainRefreshToken("mock-refresh-token");
    }

    @Test
    void testRegisterCustomer_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(customerRepository.existsByPhone(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .thenReturn("mock-access-token");
        when(userSessionService.createSession(any(User.class), anyString(), anyString(), anyString()))
                .thenReturn(mockSession);

        // When
        AuthResponse response = authService.register(
                customerRegisterRequest, 
                "127.0.0.1", 
                "test-agent", 
                "device-123"
        );

        // Then
        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("Registration successful", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getUserId());

        verify(userRepository, times(1)).existsByEmail("customer@test.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(jwtTokenProvider, times(1)).generateAccessToken(anyLong(), anyString(), anyString());
    }

    @Test
    void testRegisterTransport_Success() {
        // Given
        User transportUser = new User();
        transportUser.setUserId(2L);
        transportUser.setEmail("transport@test.com");
        transportUser.setPasswordHash("$2a$10$hashedPassword");
        transportUser.setRole(UserRole.TRANSPORT);
        transportUser.setIsActive(true);

        UserSession transportSession = new UserSession();
        transportSession.setSessionId("session-uuid-2");
        transportSession.setUser(transportUser);
        transportSession.setPlainRefreshToken("transport-refresh-token");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(transportUser);
        when(transportRepository.existsByBusinessLicenseNumber(anyString())).thenReturn(false);
        when(transportRepository.save(any(Transport.class))).thenReturn(mockTransport);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .thenReturn("transport-access-token");
        when(userSessionService.createSession(any(User.class), anyString(), anyString(), anyString()))
                .thenReturn(transportSession);

        // When
        AuthResponse response = authService.register(
                transportRegisterRequest,
                "127.0.0.1",
                "test-agent",
                "device-456"
        );

        // Then
        assertNotNull(response);
        assertEquals("transport-access-token", response.getAccessToken());
        assertEquals("transport-refresh-token", response.getRefreshToken());
        assertEquals("Registration successful", response.getMessage());
        assertNotNull(response.getUser());

        verify(userRepository, times(1)).existsByEmail("transport@test.com");
        verify(transportRepository, times(1)).existsByBusinessLicenseNumber("BL123456");
        verify(transportRepository, times(1)).save(any(Transport.class));
    }

    @Test
    void testLoginCustomer_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .thenReturn("login-access-token");
        when(userSessionService.createSession(any(User.class), anyString(), anyString(), anyString()))
                .thenReturn(mockSession);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(mockCustomer));

        // When
        AuthResponse response = authService.login(
                loginRequest,
                "127.0.0.1",
                "test-agent",
                "device-login"
        );

        // Then
        assertNotNull(response);
        assertEquals("login-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("customer@test.com", response.getUser().getEmail());

        verify(userRepository, times(1)).findByEmail("customer@test.com");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedPassword");
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void testLoginTransport_Success() {
        // Given
        User transportUser = new User();
        transportUser.setUserId(2L);
        transportUser.setEmail("transport@test.com");
        transportUser.setPasswordHash("$2a$10$hashedPassword");
        transportUser.setRole(UserRole.TRANSPORT);
        transportUser.setIsActive(true);

        LoginRequest transportLogin = new LoginRequest();
        transportLogin.setEmail("transport@test.com");
        transportLogin.setPassword("password123");

        UserSession transportSession = new UserSession();
        transportSession.setSessionId("session-uuid-transport");
        transportSession.setUser(transportUser);
        transportSession.setPlainRefreshToken("transport-login-token");

        when(userRepository.findByEmail("transport@test.com")).thenReturn(Optional.of(transportUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(transportUser);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .thenReturn("transport-login-access");
        when(userSessionService.createSession(any(User.class), anyString(), anyString(), anyString()))
                .thenReturn(transportSession);
        when(transportRepository.findById(anyLong())).thenReturn(Optional.of(mockTransport));

        // When
        AuthResponse response = authService.login(
                transportLogin,
                "127.0.0.1",
                "test-agent",
                "transport-device"
        );

        // Then
        assertNotNull(response);
        assertEquals("transport-login-access", response.getAccessToken());
        assertEquals("transport-login-token", response.getRefreshToken());
        assertEquals("Login successful", response.getMessage());

        verify(userRepository, times(1)).findByEmail("transport@test.com");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void testLoginManager_Success() {
        // Given
        User managerUser = new User();
        managerUser.setUserId(3L);
        managerUser.setEmail("manager@test.com");
        managerUser.setPasswordHash("$2a$10$hashedPassword");
        managerUser.setRole(UserRole.MANAGER);
        managerUser.setIsActive(true);

        LoginRequest managerLogin = new LoginRequest();
        managerLogin.setEmail("manager@test.com");
        managerLogin.setPassword("password123");

        UserSession managerSession = new UserSession();
        managerSession.setSessionId("session-uuid-manager");
        managerSession.setUser(managerUser);
        managerSession.setPlainRefreshToken("manager-token");

        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .thenReturn("manager-access-token");
        when(userSessionService.createSession(any(User.class), anyString(), anyString(), anyString()))
                .thenReturn(managerSession);

        // When
        AuthResponse response = authService.login(
                managerLogin,
                "127.0.0.1",
                "test-agent",
                "manager-device"
        );

        // Then
        assertNotNull(response);
        assertEquals("manager-access-token", response.getAccessToken());
        assertEquals("manager-token", response.getRefreshToken());
        assertEquals("Login successful", response.getMessage());
        assertEquals(UserRole.MANAGER.name(), response.getUser().getRole());

        verify(userRepository, times(1)).findByEmail("manager@test.com");
    }
}
