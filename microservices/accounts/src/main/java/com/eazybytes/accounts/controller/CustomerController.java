package com.eazybytes.accounts.controller;


import com.eazybytes.accounts.dto.CustomerDetailsDto;
import com.eazybytes.accounts.dto.ErrorResponseDto;
import com.eazybytes.accounts.service.ICustomersService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Tag(
        name = "REST API for Customers in EazyBank",
        description = "REST APIs in EazyBank to FETCH customer details"
)
@RestController
@RequestMapping(path="/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class CustomerController {

    private final ICustomersService iCustomersService;

    public CustomerController(ICustomersService iCustomersService){
        this.iCustomersService = iCustomersService;
    }

    @Operation(
            summary = "Fetch Customer Details REST API",
            description = "REST API to fetch Customer details based on a mobile number"
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
    @GetMapping("/fetchCustomerDetails")
    public CompletableFuture<ResponseEntity<CustomerDetailsDto>> fetchCustomerDetails(@RequestParam
                                                                   @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                                   String mobileNumber) {
        return CompletableFuture.supplyAsync(() -> {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}

            CustomerDetailsDto customerDetailsDto =
                    iCustomersService.fetchCustomerDetails(mobileNumber);

            return ResponseEntity.ok(customerDetailsDto);});
    }

    public CompletableFuture<ResponseEntity<CustomerDetailsDto>> fetchDataBulkheadFallback(String mobileNumber, Throwable t) {
        // You can log the error here if needed
        // Example: logger.error("Bulkhead full or error occurred: {}", t.getMessage());

        return CompletableFuture.completedFuture(
                ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(null)
        );
    }


}