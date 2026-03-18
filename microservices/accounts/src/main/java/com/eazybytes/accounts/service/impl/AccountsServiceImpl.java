package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j // 1. Use Lombok for logging
public class AccountsServiceImpl  implements IAccountsService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;

    /**
     * @param customerDto - CustomerDto Object
     */
    @Override
    public void createAccount(CustomerDto customerDto) {
        log.debug("Logic: CreateAccount | Checking if customer exists for mobile: {}", customerDto.getMobileNumber());

        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()) {
            log.warn("Logic: CreateAccount | Failure: Customer already registered | Mobile: {}", customerDto.getMobileNumber());
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                    + customerDto.getMobileNumber());
        }

        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Customer savedCustomer = customerRepository.save(customer);
        log.debug("Logic: CreateAccount | Customer saved with ID: {}", savedCustomer.getCustomerId());

        Accounts newAccount = createNewAccount(savedCustomer);
        accountsRepository.save(newAccount);
        log.debug("Logic: CreateAccount | Account {} created for Customer ID: {}",
                newAccount.getAccountNumber(), savedCustomer.getCustomerId());
    }

    /**
     * @param customer - Customer Object
     * @return the new account details
     */
    private Accounts createNewAccount(Customer customer) {
        log.trace("Logic: createNewAccount | Generating random account number for ID: {}", customer.getCustomerId());
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Accounts Details based on a given mobileNumber
     */
    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        log.debug("Logic: FetchAccount | Querying DB for mobile: {}", mobileNumber);

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> {
                    log.warn("Logic: FetchAccount | NotFound: Customer | Mobile: {}", mobileNumber);
                    return new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber);
                }
        );

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> {
                    log.warn("Logic: FetchAccount | NotFound: Account | CustomerID: {}", customer.getCustomerId());
                    return new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString());
                }
        );

        return CustomerMapper.mapToCustomerDto(customer, new CustomerDto()); // abbreviated for length
    }

    /**
     * @param customerDto - CustomerDto Object
     * @return boolean indicating if the update of Account details is successful or not
     */
    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        log.debug("Logic: UpdateAccount | Updating records for account: {}", customerDto.getAccountsDto().getAccountNumber());
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto !=null ){
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            log.info("Logic: UpdateAccount | Success: Records synchronized for account: {}", accounts.getAccountNumber());
            isUpdated = true;
        }
        return  isUpdated;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of Account details is successful or not
     */
    @Override
    public boolean deleteAccount(String mobileNumber) {
        log.debug("Logic: DeleteAccount | Attempting deletion for mobile: {}", mobileNumber);
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        log.info("Logic: DeleteAccount | Success: Deleted customer {} and their accounts", customer.getCustomerId());
        return true;
    }


}
