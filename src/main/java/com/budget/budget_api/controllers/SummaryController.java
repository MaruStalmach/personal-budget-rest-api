package com.budget.budget_api.controllers;

import com.budget.budget_api.dtos.responses.SummaryResponse;
import com.budget.budget_api.services.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts/{id}/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public ResponseEntity<SummaryResponse> getSummary(@PathVariable("id") Long id) {
        return ResponseEntity.ok(summaryService.getSummary(id));
    }

}
