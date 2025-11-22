package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransportVehicleUtilizationResponse {
    private String vehicleName;
    private double utilizationRate;
    private long jobsCompleted;
    private double revenue;

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public double getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(double utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public long getJobsCompleted() {
        return jobsCompleted;
    }

    public void setJobsCompleted(long jobsCompleted) {
        this.jobsCompleted = jobsCompleted;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}

