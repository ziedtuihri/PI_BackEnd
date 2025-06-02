package com.internship.platform.Controllers;

import com.internship.platform.services.JobMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jobmarket")
public class JobMarketController {

    private final JobMarketService jobMarketService;

    @Autowired
    public JobMarketController(JobMarketService jobMarketService) {
        this.jobMarketService = jobMarketService;
    }

    // GET /api/jobmarket/search?location=Paris&industry=IT
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchJobs(
            @RequestParam String location,
            @RequestParam String industry) {
        Map<String, Object> data = jobMarketService.fetchJobMarketData(location, industry);
        return ResponseEntity.ok(data);
    }
}

