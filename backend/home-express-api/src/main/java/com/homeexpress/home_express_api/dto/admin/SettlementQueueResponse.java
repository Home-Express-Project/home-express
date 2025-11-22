package com.homeexpress.home_express_api.dto.admin;

import com.homeexpress.home_express_api.dto.SettlementDTO;

import java.util.List;
import java.util.Map;

public class SettlementQueueResponse {

    private Map<Long, TransportSettlementGroup> settlementsByTransport;
    private int totalTransports;
    private int totalSettlements;
    private long totalAmount;

    public SettlementQueueResponse() {
    }

    public Map<Long, TransportSettlementGroup> getSettlementsByTransport() {
        return settlementsByTransport;
    }

    public void setSettlementsByTransport(Map<Long, TransportSettlementGroup> settlementsByTransport) {
        this.settlementsByTransport = settlementsByTransport;
    }

    public int getTotalTransports() {
        return totalTransports;
    }

    public void setTotalTransports(int totalTransports) {
        this.totalTransports = totalTransports;
    }

    public int getTotalSettlements() {
        return totalSettlements;
    }

    public void setTotalSettlements(int totalSettlements) {
        this.totalSettlements = totalSettlements;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static class TransportSettlementGroup {
        private Long transportId;
        private String transportName;
        private List<SettlementDTO> settlements;
        private int count;
        private long totalAmount;

        public TransportSettlementGroup() {
        }

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

        public List<SettlementDTO> getSettlements() {
            return settlements;
        }

        public void setSettlements(List<SettlementDTO> settlements) {
            this.settlements = settlements;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(long totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
