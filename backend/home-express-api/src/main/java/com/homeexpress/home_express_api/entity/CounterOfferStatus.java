package com.homeexpress.home_express_api.entity;

/**
 * Status of a counter-offer in the negotiation process
 */
public enum CounterOfferStatus {
    /**
     * Counter-offer has been made and is awaiting response
     */
    PENDING,

    /**
     * Counter-offer has been accepted by the other party
     */
    ACCEPTED,

    /**
     * Counter-offer has been rejected by the other party
     */
    REJECTED,

    /**
     * Counter-offer has expired without response
     */
    EXPIRED,

    /**
     * Counter-offer has been superseded by a new counter-offer
     */
    SUPERSEDED
}

