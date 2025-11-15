package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.UpdateCustomerSettingsRequest;
import com.homeexpress.home_express_api.dto.response.CustomerSettingsResponse;
import com.homeexpress.home_express_api.entity.Customer;
import com.homeexpress.home_express_api.entity.CustomerSettings;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.CustomerRepository;
import com.homeexpress.home_express_api.repository.CustomerSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerSettingsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerSettingsRepository customerSettingsRepository;

    @Transactional(readOnly = true)
    public CustomerSettingsResponse getSettings(Long customerId) {
        CustomerSettings settings = customerSettingsRepository.findById(customerId)
                .orElseGet(() -> createDefaultSettings(customerId));
        return mapToResponse(settings);
    }

    @Transactional
    public CustomerSettingsResponse updateSettings(Long customerId, UpdateCustomerSettingsRequest request) {
        CustomerSettings settings = customerSettingsRepository.findById(customerId)
                .orElseGet(() -> createDefaultSettings(customerId));

        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getBookingUpdates() != null) {
            settings.setBookingUpdates(request.getBookingUpdates());
        }
        if (request.getQuotationAlerts() != null) {
            settings.setQuotationAlerts(request.getQuotationAlerts());
        }
        if (request.getPromotions() != null) {
            settings.setPromotions(request.getPromotions());
        }
        if (request.getNewsletter() != null) {
            settings.setNewsletter(request.getNewsletter());
        }
        if (request.getProfileVisibility() != null) {
            settings.setProfileVisibility(request.getProfileVisibility());
        }
        if (request.getShowPhone() != null) {
            settings.setShowPhone(request.getShowPhone());
        }
        if (request.getShowEmail() != null) {
            settings.setShowEmail(request.getShowEmail());
        }

        CustomerSettings saved = customerSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    private CustomerSettings createDefaultSettings(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        CustomerSettings settings = new CustomerSettings(customer);
        return customerSettingsRepository.save(settings);
    }

    private CustomerSettingsResponse mapToResponse(CustomerSettings settings) {
        CustomerSettingsResponse response = new CustomerSettingsResponse();
        response.setCustomerId(settings.getCustomerId());
        response.setLanguage(settings.getLanguage());
        response.setEmailNotifications(settings.getEmailNotifications());
        response.setBookingUpdates(settings.getBookingUpdates());
        response.setQuotationAlerts(settings.getQuotationAlerts());
        response.setPromotions(settings.getPromotions());
        response.setNewsletter(settings.getNewsletter());
        response.setProfileVisibility(settings.getProfileVisibility());
        response.setShowPhone(settings.getShowPhone());
        response.setShowEmail(settings.getShowEmail());
        return response;
    }
}

