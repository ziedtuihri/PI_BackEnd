package com.internship.platform.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMarketService {

    @Value("${adzuna.app.id}")
    private String appId;

    @Value("${adzuna.app.key}")
    private String appKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> fetchJobMarketData(String location, String industry) {
        String url = "https://api.adzuna.com/v1/api/jobs/fr/search/1";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("app_id", appId)
                .queryParam("app_key", appKey)
                .queryParam("what", industry)
                .queryParam("where", location)
                .queryParam("content-type", "application/json");

        Map<String, Object> response = restTemplate.getForObject(builder.toUriString(), Map.class);

        if (response == null || !response.containsKey("results")) {
            return Collections.singletonMap("error", "No results found or API error");
        }

        // Extract total job count from response
        int totalJobs = (int) response.getOrDefault("count", 0);

        // Extract job results list
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        // Variables for salary calculations
        double salarySum = 0;
        int salaryCount = 0;
        Double salaryMin = null;
        Double salaryMax = null;

        // Company job posting counts
        Map<String, Integer> companyCounts = new HashMap<>();

        // Contract type counts (if available)
        Map<String, Integer> contractTypeCounts = new HashMap<>();

        // Iterate through jobs to calculate stats
        for (Map<String, Object> job : results) {

            // Salary calculation
            Double salaryMinJob = safeDouble(job.get("salary_min"));
            Double salaryMaxJob = safeDouble(job.get("salary_max"));

            if (salaryMinJob != null && salaryMaxJob != null) {
                double avgSalary = (salaryMinJob + salaryMaxJob) / 2;
                salarySum += avgSalary;
                salaryCount++;

                // Update global min/max salary
                if (salaryMin == null || salaryMinJob < salaryMin) {
                    salaryMin = salaryMinJob;
                }
                if (salaryMax == null || salaryMaxJob > salaryMax) {
                    salaryMax = salaryMaxJob;
                }
            }

            // Company counts (check if company name exists)
            Map<String, Object> company = (Map<String, Object>) job.get("company");
            if (company != null) {
                String companyName = (String) company.get("display_name");
                if (companyName != null) {
                    companyCounts.put(companyName, companyCounts.getOrDefault(companyName, 0) + 1);
                }
            }

            // Contract type counts (e.g. full_time, part_time, contract)
            String contractType = (String) job.get("contract_type");
            if (contractType != null && !contractType.isEmpty()) {
                contractTypeCounts.put(contractType, contractTypeCounts.getOrDefault(contractType, 0) + 1);
            }
        }

        // Calculate average salary
        Double averageSalary = salaryCount > 0 ? salarySum / salaryCount : null;

        // Get top 5 companies by job count
        List<Map.Entry<String, Integer>> topCompanies = companyCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Prepare a clean response map
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalJobs", totalJobs);
        kpis.put("averageSalary", averageSalary);
        kpis.put("salaryMin", salaryMin);
        kpis.put("salaryMax", salaryMax);
        kpis.put("topCompanies", topCompanies);
        kpis.put("contractTypeDistribution", contractTypeCounts);

        return kpis;
    }

    // Helper to safely convert to Double
    private Double safeDouble(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
