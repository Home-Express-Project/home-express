package com.homeexpress.home_express_api.controller.transport;

import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/transport")
public class TransportEventController {

    private final UserRepository userRepository;

    public TransportEventController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<SseEmitter> subscribeToTransportEvents(Authentication authentication) {
        if (!isTransport(authentication)) {
            return ResponseEntity.status(403).build();
        }
        SseEmitter emitter = new SseEmitter(0L);
        emitter.complete();
        return ResponseEntity.ok(emitter);
    }

    @GetMapping(path = "/jobs/{bookingId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('TRANSPORT')")
    public ResponseEntity<SseEmitter> subscribeToJobEvents(Authentication authentication, @PathVariable Long bookingId) {
        if (!isTransport(authentication)) {
            return ResponseEntity.status(403).build();
        }
        SseEmitter emitter = new SseEmitter(0L);
        emitter.complete();
        return ResponseEntity.ok(emitter);
    }

    private boolean isTransport(Authentication authentication) {
        User user = AuthenticationUtils.getUser(authentication, userRepository);
        return user != null && user.getRole() == UserRole.TRANSPORT;
    }
}

