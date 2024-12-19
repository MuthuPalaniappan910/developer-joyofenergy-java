package uk.tw.energy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class PricePlanCalculateService {

    @Autowired
    private PricePlanService pricePlanService;

    @Autowired
    private AccountService accountService;

    public Double pricePlanCalculateService(List<ElectricityReading> smartMeter, String plan) {

        List<ElectricityReading> readingsForLast7Days = smartMeter.stream()
                .filter(electricityReading -> electricityReading.time()
                        .isAfter(Instant.now().minus(Duration.ofDays(7))))
                .toList();

        pricePlanService.calculateCost(readingsForLast7Days, getPricePlan(plan));

    }

    private PricePlan getPricePlan(String plan) {
        return pricePlanService.getPricePlanDetails(plan);
    }
}
