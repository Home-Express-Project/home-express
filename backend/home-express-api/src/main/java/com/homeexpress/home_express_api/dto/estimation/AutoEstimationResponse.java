package com.homeexpress.home_express_api.dto.estimation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutoEstimationResponse {

    private boolean success;

    private String message;

    @JsonProperty("distance_km")
    private double distanceKm;

    @JsonProperty("estimated_weight_kg")
    private double estimatedWeightKg;

    @JsonProperty("recommended_vehicle_type")
    private String recommendedVehicleType;

    @JsonProperty("estimations")
    private List<TransportEstimate> estimations;

    @JsonProperty("price_range")
    private PriceRange priceRange;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getEstimatedWeightKg() {
        return estimatedWeightKg;
    }

    public void setEstimatedWeightKg(double estimatedWeightKg) {
        this.estimatedWeightKg = estimatedWeightKg;
    }

    public String getRecommendedVehicleType() {
        return recommendedVehicleType;
    }

    public void setRecommendedVehicleType(String recommendedVehicleType) {
        this.recommendedVehicleType = recommendedVehicleType;
    }

    public List<TransportEstimate> getEstimations() {
        return estimations;
    }

    public void setEstimations(List<TransportEstimate> estimations) {
        this.estimations = estimations;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(PriceRange priceRange) {
        this.priceRange = priceRange;
    }

    public static class PriceRange {

        private long lowest;
        private long highest;
        private long average;

        public PriceRange() {
        }

        public PriceRange(long lowest, long highest, long average) {
            this.lowest = lowest;
            this.highest = highest;
            this.average = average;
        }

        public long getLowest() {
            return lowest;
        }

        public void setLowest(long lowest) {
            this.lowest = lowest;
        }

        public long getHighest() {
            return highest;
        }

        public void setHighest(long highest) {
            this.highest = highest;
        }

        public long getAverage() {
            return average;
        }

        public void setAverage(long average) {
            this.average = average;
        }
    }

    public static class TransportEstimate {

        @JsonProperty("transport_id")
        private Long transportId;

        @JsonProperty("transport_name")
        private String transportName;

        private double rating;

        @JsonProperty("completed_jobs")
        private int completedJobs;

        @JsonProperty("vehicle_id")
        private Long vehicleId;

        @JsonProperty("vehicle_type")
        private String vehicleType;

        @JsonProperty("vehicle_name")
        private String vehicleName;

        @JsonProperty("license_plate")
        private String licensePlate;

        @JsonProperty("total_price")
        private long totalPrice;

        @JsonProperty("estimated_duration")
        private int estimatedDuration;

        @JsonProperty("rank_score")
        private double rankScore;

        private Breakdown breakdown;

        public Long getTransportId() {
            return transportId;
        }

        public void setTransportId(Long transportId) {
            this.transportId = transportId;
        }

        public String getTransportName() {
            return transportName;
        }

        public void setTransportName(String transportName) {
            this.transportName = transportName;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public int getCompletedJobs() {
            return completedJobs;
        }

        public void setCompletedJobs(int completedJobs) {
            this.completedJobs = completedJobs;
        }

        public Long getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(Long vehicleId) {
            this.vehicleId = vehicleId;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public void setVehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
        }

        public String getVehicleName() {
            return vehicleName;
        }

        public void setVehicleName(String vehicleName) {
            this.vehicleName = vehicleName;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public long getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(long totalPrice) {
            this.totalPrice = totalPrice;
        }

        public int getEstimatedDuration() {
            return estimatedDuration;
        }

        public void setEstimatedDuration(int estimatedDuration) {
            this.estimatedDuration = estimatedDuration;
        }

        public double getRankScore() {
            return rankScore;
        }

        public void setRankScore(double rankScore) {
            this.rankScore = rankScore;
        }

        public Breakdown getBreakdown() {
            return breakdown;
        }

        public void setBreakdown(Breakdown breakdown) {
            this.breakdown = breakdown;
        }
    }

    public static class Breakdown {

        @JsonProperty("base_price")
        private long basePrice;

        @JsonProperty("distance_price")
        private long distancePrice;

        @JsonProperty("items_price")
        private long itemsPrice;

        @JsonProperty("floor_fees")
        private long floorFees;

        @JsonProperty("multiplier")
        private double multiplier;

        @JsonProperty("subtotal")
        private long subtotal;

        public long getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(long basePrice) {
            this.basePrice = basePrice;
        }

        public long getDistancePrice() {
            return distancePrice;
        }

        public void setDistancePrice(long distancePrice) {
            this.distancePrice = distancePrice;
        }

        public long getItemsPrice() {
            return itemsPrice;
        }

        public void setItemsPrice(long itemsPrice) {
            this.itemsPrice = itemsPrice;
        }

        public long getFloorFees() {
            return floorFees;
        }

        public void setFloorFees(long floorFees) {
            this.floorFees = floorFees;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public long getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(long subtotal) {
            this.subtotal = subtotal;
        }
    }
}
