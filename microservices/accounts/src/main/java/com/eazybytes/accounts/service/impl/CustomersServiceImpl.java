package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CardsDto;
import com.eazybytes.accounts.dto.CustomerDetailsDto;
import com.eazybytes.accounts.dto.LoansDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.ICustomersService;
import com.eazybytes.accounts.service.client.CardsFeignClient;
import com.eazybytes.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        log.info("Action: FetchCustomerDetails | Status: IN_PROGRESS | Mobile: {}", mobileNumber);

        // 1. Internal DB Calls
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> {
                    log.warn("Action: FetchCustomerDetails | Status: NOT_FOUND | Entity: Customer | Mobile: {}", mobileNumber);
                    return new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber);
                }
        );

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> {
                    log.warn("Action: FetchCustomerDetails | Status: NOT_FOUND | Entity: Account | CustomerID: {}", customer.getCustomerId());
                    return new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString());
                }
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        // 2. External Feign Call: Loans
        log.debug("Action: FetchCustomerDetails | Status: CALLING_EXTERNAL | Service: Loans | Mobile: {}", mobileNumber);
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        if(null != loansDtoResponseEntity) {
            customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
            log.debug("Action: FetchCustomerDetails | Status: EXTERNAL_SUCCESS | Service: Loans");
        }

        // 3. External Feign Call: Cards
        log.debug("Action: FetchCustomerDetails | Status: CALLING_EXTERNAL | Service: Cards | Mobile: {}", mobileNumber);
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        if(null != cardsDtoResponseEntity){
            customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
            log.debug("Action: FetchCustomerDetails | Status: EXTERNAL_SUCCESS | Service: Cards");
        }

        log.info("Action: FetchCustomerDetails | Status: SUCCESS | Mobile: {}", mobileNumber);
        return customerDetailsDto;
    }
}
