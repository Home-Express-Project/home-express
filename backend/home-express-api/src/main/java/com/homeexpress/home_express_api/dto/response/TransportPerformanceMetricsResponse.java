package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransportPerformanceMetricsResponse {
    private double acceptanceRate;
    private double acceptanceRateChange;
    private double avgResponseTimeMinutes;
    private double responseTimeChange;
    private double customerSatisfaction;
    private double satisfactionChange;
    private double onTimeDeliveryRate;
    private double onTimeChange;
    private double completionRate;
    private double completionChange;
    private double revenuePerJob;
    private double revenueChange;
    private long totalJobs;
    private long jobsChange;
    private long activeVehicles;

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public double getAcceptanceRateChange() {
        return acceptanceRateChange;
    }

    public void setAcceptanceRateChange(double acceptanceRateChange) {
        this.acceptanceRateChange = acceptanceRateChange;
    }

    public double getAvgResponseTimeMinutes() {
        return avgResponseTimeMinutes;
    }

    public void setAvgResponseTimeMinutes(double avgResponseTimeMinutes) {
        this.avgResponseTimeMinutes = avgResponseTimeMinutes;
    }

    public double getResponseTimeChange() {
        return responseTimeChange;
    }

    public void setResponseTimeChange(double responseTimeChange) {
        this.responseTimeChange = responseTimeChange;
    }

    public double getCustomerSatisfaction() {
        return customerSatisfaction;
    }

    public void setCustomerSatisfaction(double customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }

    public double getSatisfactionChange() {
        return satisfactionChange;
    }

    public void setSatisfactionChange(double satisfactionChange) {
        this.satisfactionChange = satisfactionChange;
    }

    public double getOnTimeDeliveryRate() {
        return onTimeDeliveryRate;
    }

    public void setOnTimeDeliveryRate(double onTimeDeliveryRate) {
        this.onTimeDeliveryRate = onTimeDeliveryRate;
    }

    public double getOnTimeChange() {
        return onTimeChange;
    }

    public void setOnTimeChange(double onTimeChange) {
        this.onTimeChange = onTimeChange;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getCompletionChange() {
        return completionChange;
    }

    public void setCompletionChange(double completionChange) {
        this.completionChange = completionChange;
    }

    public double getRevenuePerJob() {
        return revenuePerJob;
    }

    public void setRevenuePerJob(double revenuePerJob) {
        this.revenuePerJob = revenuePerJob;
    }

    public double getRevenueChange() {
        return revenueChange;
    }

    public void setRevenueChange(double revenueChange) {
        this.revenueChange = revenueChange;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getJobsChange() {
        return jobsChange;
    }

    public void setJobsChange(long jobsChange) {
        this.jobsChange = jobsChange;
    }

    public long getActiveVehicles() {
        return activeVehicles;
    }

    public void setActiveVehicles(long activeVehicles) {
        this.activeVehicles = activeVehicles;
    }
}

