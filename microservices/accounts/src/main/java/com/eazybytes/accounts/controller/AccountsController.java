package com.eazybytes.accounts.controller;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.*;
import com.eazybytes.accounts.service.IAccountsService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Praneeth
 */

@Tag(
        name = "CRUD REST APIs for Accounts in EazyBank",
        description = "CRUD REST APIs in EazyBank to CREATE, UPDATE, FETCH AND DELETE account details"
)

@RestController
@RequestMapping(path="/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Validated
@Slf4j
public class AccountsController {

    private final IAccountsService iAccountsService;
    private final AccountsContactInfoDto accountsContactInfoDto;




    @Operation(
            summary = "Create Account REST API",
            description = "REST API to create new Customer &  Account inside EazyBank"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createAccount(@Valid @RequestBody CustomerDto customerDto) {
        log.info("Action: CreateAccount | Status: IN_PROGRESS | Customer: {}", customerDto.getName());
        iAccountsService.createAccount(customerDto);
        log.info("Action: CreateAccount | Status: SUCCESS | Mobile: {}", customerDto.getMobileNumber());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AccountsConstants.STATUS_201, AccountsConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Fetch Account Details REST API",
            description = "REST API to fetch Customer &  Account details based on a mobile number"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )

    @Bulkhead(name = "fetchDataBulkhead", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "fetchDataBulkheadFallback")
    @GetMapping("/fetch")
    public CompletableFuture<ResponseEntity<CustomerDto>> fetchAccountDetails(@RequestParam
                                                               @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                               String mobileNumber) {

        log.info("Action: FetchAccount | Status: IN_PROGRESS | Mobile: {}", mobileNumber);
        return CompletableFuture.supplyAsync(() -> {
            // Logging inside the async block verifies trace context propagation
            CustomerDto customerDto = iAccountsService.fetchAccount(mobileNumber);
            log.info("Action: FetchAccount | Status: SUCCESS | Mobile: {}", mobileNumber);
            return ResponseEntity.status(HttpStatus.OK).body(customerDto);
        });
    }

    public CompletableFuture<ResponseEntity<CustomerDto>> fetchDataBulkheadFallback(String mobileNumber, Throwable t) {

        log.error("Action: FetchAccount | Status: FALLBACK | Reason: Bulkhead Full/Error | Message: {}", t.getMessage());
        return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.OK).body(null)
        );
    }

    @Operation(
            summary = "Update Account Details REST API",
            description = "REST API to update Customer &  Account details based on a account number"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "417",
                    description = "Expectation Failed"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @PutMapping("/update")
    public ResponseEntity<ResponseDto> updateAccountDetails(@Valid @RequestBody CustomerDto customerDto) {
        log.info("Action: UpdateAccount | Status: IN_PROGRESS | Mobile: {}", customerDto.getMobileNumber());
        boolean isUpdated = iAccountsService.updateAccount(customerDto);
        if(isUpdated) {
            log.info("Action: UpdateAccount | Status: SUCCESS | Mobile: {}", customerDto.getMobileNumber());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountsConstants.STATUS_200, AccountsConstants.MESSAGE_200));
        } else {
            log.error("Action: UpdateAccount | Status: FAILED | Mobile: {}", customerDto.getMobileNumber());
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(AccountsConstants.STATUS_417, AccountsConstants.MESSAGE_417_UPDATE));
        }
    }

    @Operation(
            summary = "Delete Account & Customer Details REST API",
            description = "REST API to delete Customer &  Account details based on a mobile number"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "417",
                    description = "Expectation Failed"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteAccountDetails(@RequestParam
                                                                @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                                String mobileNumber) {
        log.info("Action: DeleteAccount | Status: IN_PROGRESS | Mobile: {}", mobileNumber);
        boolean isDeleted = iAccountsService.deleteAccount(mobileNumber);
        if(isDeleted) {
            log.info("Action: DeleteAccount | Status: SUCCESS | Mobile: {}", mobileNumber);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountsConstants.STATUS_200, AccountsConstants.MESSAGE_200));
        } else {
            log.error("Action: DeleteAccount | Status: FAILED | Mobile: {}", mobileNumber);
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(AccountsConstants.STATUS_417, AccountsConstants.MESSAGE_417_DELETE));
        }
    }

    @RateLimiter(name = "contactInfo", fallbackMethod = "contactInfoRateLimitFallback")
    @Retry(name= "contactInfo", fallbackMethod = "contactInfoFallback")
    @GetMapping("/contact-info")
    public ResponseEntity<AccountsContactInfoDto> getContactInfo() {
        log.debug("Action: GetContactInfo | Status: TRIGGERED");
        return ResponseEntity.status(HttpStatus.OK).body(accountsContactInfoDto);
    }

    public ResponseEntity<AccountsContactInfoDto> contactInfoRateLimitFallback(Throwable throwable){
        log.warn("Action: GetContactInfo | Status: RATELIMIT_FALLBACK | Reason: {}", throwable.getMessage());
        AccountsContactInfoDto fallback = new AccountsContactInfoDto();
        fallback.setMessage("Rate limit exceeded. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(fallback);
    }

    //fallback method for contactInfo
    public ResponseEntity<AccountsContactInfoDto> contactInfoFallback(Throwable throwable){
        // 1. Log the retry failure with the specific error that caused the final fail
        log.warn("Action: GetContactInfo | Status: RETRY_EXHAUSTED | Reason: {} | TraceID: {}",
                throwable.getMessage(),
                org.slf4j.MDC.get("trace_id"));

        AccountsContactInfoDto accountsContactInfoDtoFallbackObj = new AccountsContactInfoDto();
        accountsContactInfoDtoFallbackObj.setMessage("This is Accounts Retry fallback Message value");

        Map<String, String> contactDetails = new HashMap<>();
        contactDetails.put("phNo", "8989898988");
        contactDetails.put("landline", "0144-43");

        accountsContactInfoDtoFallbackObj.setContactDetails(contactDetails);
        accountsContactInfoDtoFallbackObj.setOnCallSupport(List.of("8383838383", "64636464646"));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountsContactInfoDtoFallbackObj);
    }


}
