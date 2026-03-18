package com.eazybytes.loans.service.impl;

import com.eazybytes.loans.constants.LoansConstants;
import com.eazybytes.loans.dto.LoansDto;
import com.eazybytes.loans.entity.Loans;
import com.eazybytes.loans.exception.LoanAlreadyExistsException;
import com.eazybytes.loans.exception.ResourceNotFoundException;
import com.eazybytes.loans.mapper.LoansMapper;
import com.eazybytes.loans.repository.LoansRepository;
import com.eazybytes.loans.service.ILoansService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class LoansServiceImpl implements ILoansService {

    private LoansRepository loansRepository;

    /**
     * @param mobileNumber - Mobile Number of the Customer
     */
    @Override
    public void createLoan(String mobileNumber) {
        log.debug("Logic: CreateLoan | Checking status for Mobile: {}", mobileNumber);

        Optional<Loans> optionalLoans = loansRepository.findByMobileNumber(mobileNumber);
        if(optionalLoans.isPresent()){
            log.warn("Logic: CreateLoan | Failure: Duplicate entry found | Mobile: {}", mobileNumber);
            throw new LoanAlreadyExistsException("Loan already registered with given mobileNumber " + mobileNumber);
        }

        Loans newLoan = createNewLoan(mobileNumber);
        loansRepository.save(newLoan);
        log.info("Logic: CreateLoan | Success: LoanNo: {} assigned to Mobile: {}", newLoan.getLoanNumber(), mobileNumber);
    }

    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new loan details
     */
    private Loans createNewLoan(String mobileNumber) {
        log.trace("Logic: createNewLoan | Initializing Home Loan parameters for Mobile: {}", mobileNumber);
        Loans newLoan = new Loans();
        long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(LoansConstants.HOME_LOAN);
        newLoan.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
        return newLoan;
    }

    /**
     *
     * @param mobileNumber - Input mobile Number
     * @return Loan Details based on a given mobileNumber
     */
    @Override
    public LoansDto fetchLoan(String mobileNumber) {
        log.debug("Logic: FetchLoan | Database query for Mobile: {}", mobileNumber);

        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> {
                    log.warn("Logic: FetchLoan | NOT_FOUND | Mobile: {}", mobileNumber);
                    return new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber);
                }
        );
        return LoansMapper.mapToLoansDto(loans, new LoansDto());
    }

    /**
     *
     * @param loansDto - LoansDto Object
     * @return boolean indicating if the update of loan details is successful or not
     */
    @Override
    public boolean updateLoan(LoansDto loansDto) {
        log.debug("Logic: UpdateLoan | Database query for LoanNo: {}", loansDto.getLoanNumber());

        Loans loans = loansRepository.findByLoanNumber(loansDto.getLoanNumber()).orElseThrow(
                () -> {
                    log.error("Logic: UpdateLoan | FAILED | LoanNo: {} not found", loansDto.getLoanNumber());
                    return new ResourceNotFoundException("Loan", "LoanNumber", loansDto.getLoanNumber());
                }
        );

        LoansMapper.mapToLoans(loansDto, loans);
        loansRepository.save(loans);

        log.info("Logic: UpdateLoan | Success: LoanNo: {} status updated", loans.getLoanNumber());
        return true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of loan details is successful or not
     */
    @Override
    public boolean deleteLoan(String mobileNumber) {
        log.debug("Logic: DeleteLoan | Database query for Mobile: {}", mobileNumber);

        Loans loans = loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber)
        );

        loansRepository.deleteById(loans.getLoanId());
        log.info("Logic: DeleteLoan | Success: Record deleted for Mobile: {}", mobileNumber);
        return true;
    }


}
