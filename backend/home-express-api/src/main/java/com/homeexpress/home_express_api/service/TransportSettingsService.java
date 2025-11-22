package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.UpdateTransportSettingsRequest;
import com.homeexpress.home_express_api.dto.response.TransportSettingsResponse;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.TransportSettings;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.TransportSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransportSettingsService {

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private TransportSettingsRepository transportSettingsRepository;

    @Transactional(readOnly = true)
    public TransportSettingsResponse getSettings(Long transportId) {
        TransportSettings settings = transportSettingsRepository.findById(transportId)
                .orElseGet(() -> createDefaultSettings(transportId));
        return mapToResponse(settings);
    }

    @Transactional
    public TransportSettingsResponse updateSettings(Long transportId, UpdateTransportSettingsRequest request) {
        TransportSettings settings = transportSettingsRepository.findById(transportId)
                .orElseGet(() -> createDefaultSettings(transportId));

        if (request.getSearchRadiusKm() != null) {
            settings.setSearchRadiusKm(request.getSearchRadiusKm());
        }

        if (request.getMinJobValueVnd() != null) {
            settings.setMinJobValueVnd(request.getMinJobValueVnd());
        }

        if (request.getAutoAcceptJobs() != null) {
            settings.setAutoAcceptJobs(request.getAutoAcceptJobs());
        }

        if (request.getResponseTimeHours() != null) {
            settings.setResponseTimeHours(request.getResponseTimeHours());
        }

        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }

        if (request.getNewJobAlerts() != null) {
            settings.setNewJobAlerts(request.getNewJobAlerts());
        }

        if (request.getQuotationUpdates() != null) {
            settings.setQuotationUpdates(request.getQuotationUpdates());
        }

        if (request.getPaymentNotifications() != null) {
            settings.setPaymentNotifications(request.getPaymentNotifications());
        }

        if (request.getReviewNotifications() != null) {
            settings.setReviewNotifications(request.getReviewNotifications());
        }

        TransportSettings saved = transportSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private TransportSettings createDefaultSettings(Long transportId) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", "id", transportId));

        TransportSettings settings = new TransportSettings(transport);
        settings.setSearchRadiusKm(BigDecimal.valueOf(10));
        settings.setMinJobValueVnd(0L);
        settings.setAutoAcceptJobs(Boolean.FALSE);
        settings.setResponseTimeHours(BigDecimal.valueOf(2));
        settings.setEmailNotifications(Boolean.TRUE);
        settings.setNewJobAlerts(Boolean.TRUE);
        settings.setQuotationUpdates(Boolean.TRUE);
        settings.setPaymentNotifications(Boolean.TRUE);
        settings.setReviewNotifications(Boolean.TRUE);
        return transportSettingsRepository.save(settings);
    }

    private TransportSettingsResponse mapToResponse(TransportSettings settings) {
        TransportSettingsResponse response = new TransportSettingsResponse();

        // Include verification status from transport entity
        Transport transport = settings.getTransport();
        if (transport != null && transport.getVerificationStatus() != null) {
            response.setVerificationStatus(transport.getVerificationStatus().name());
        }

        response.setSearchRadiusKm(settings.getSearchRadiusKm());
        response.setMinJobValueVnd(settings.getMinJobValueVnd());
        response.setAutoAcceptJobs(settings.getAutoAcceptJobs());
        response.setResponseTimeHours(settings.getResponseTimeHours());
        response.setEmailNotifications(settings.getEmailNotifications());
        response.setNewJobAlerts(settings.getNewJobAlerts());
        response.setQuotationUpdates(settings.getQuotationUpdates());
        response.setPaymentNotifications(settings.getPaymentNotifications());
        response.setReviewNotifications(settings.getReviewNotifications());
        return response;
    }
}
