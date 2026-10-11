
package com.example.expensetracker.controller;

import com.example.expensetracker.service.FredService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financial-calendar")
public class FinancialCalendarController {

    private final FredService fredService;

    // Correct constructor
    public FinancialCalendarController(FredService fredService) {
        this.fredService = fredService;
    }

    @GetMapping("/economic-data")
    public ResponseEntity<?> getEconomicData(
            @RequestParam(defaultValue = "GDP") String seriesId) {

        if (!seriesId.matches("GDP|CPIAUCSL|UNRATE|FEDFUNDS")) {
            return ResponseEntity.badRequest()
                    .body("Unsupported economic data series.");
        }

        try {
            return ResponseEntity.ok(
                    fredService.getEconomicData(seriesId)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Unable to retrieve economic data.");
        }
    }
}
