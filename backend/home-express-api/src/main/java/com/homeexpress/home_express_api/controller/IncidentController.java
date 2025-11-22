package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.incident.*;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.service.EvidenceService;
import com.homeexpress.home_express_api.service.IncidentService;
import com.homeexpress.home_express_api.util.AuthenticationUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createIncident(
            @Valid @RequestBody IncidentRequest request,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        if (user.getRole() != UserRole.CUSTOMER && user.getRole() != UserRole.TRANSPORT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Only CUSTOMER or TRANSPORT users can report incidents"));
        }

        IncidentResponse response = incidentService.createIncident(request, user.getUserId(), user.getRole());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Incident reported successfully",
            "incident", response
        ));
    }

    @GetMapping
    public ResponseEntity<?> getIncidents(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Severity severity,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        List<IncidentResponse> incidents = incidentService.getIncidents(
            bookingId, status, severity, user.getUserId(), user.getRole()
        );

        return ResponseEntity.ok(Map.of(
            "incidents", incidents,
            "count", incidents.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncidentById(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        IncidentResponse incident = incidentService.getIncidentById(id, user.getUserId(), user.getRole());
        
        List<EvidenceResponse> evidence = evidenceService.getEvidenceByIncident(id, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "incident", incident,
            "evidence", evidence
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateIncidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        if (user.getRole() != UserRole.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Only managers can update incident status"));
        }

        IncidentResponse response = incidentService.updateIncidentStatus(id, request, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "message", "Incident updated successfully",
            "incident", response
        ));
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<?> addEvidence(
            @PathVariable Long id,
            @Valid @RequestBody EvidenceRequest request,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        request.setIncidentId(id);
        
        EvidenceResponse response = evidenceService.uploadEvidence(request, user.getUserId(), user.getRole());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Evidence uploaded successfully",
            "evidence", response
        ));
    }

    @GetMapping("/{id}/evidence")
    public ResponseEntity<?> getIncidentEvidence(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        List<EvidenceResponse> evidence = evidenceService.getEvidenceByIncident(id, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "evidence", evidence,
            "count", evidence.size()
        ));
    }

    @DeleteMapping("/evidence/{evidenceId}")
    public ResponseEntity<?> deleteEvidence(
            @PathVariable Long evidenceId,
            Authentication authentication) {
        
        User user = AuthenticationUtils.getUser(authentication, userRepository);

        evidenceService.deleteEvidence(evidenceId, user.getUserId(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "message", "Evidence deleted successfully"
        ));
    }
}
