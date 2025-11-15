package com.homeexpress.home_express_api.exception;

/**
 * Exception thrown when a contract is not found
 */
public class ContractNotFoundException extends ResourceNotFoundException {
    
    public ContractNotFoundException(Long contractId) {
        super("Contract", "id", contractId);
    }
    
    public ContractNotFoundException(String contractNumber) {
        super(String.format("Contract not found with number: %s", contractNumber));
    }
}

