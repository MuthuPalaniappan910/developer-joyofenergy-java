package uk.tw.energy.controller;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

@RestController
@RequestMapping("/price-plans")
@Validated
public class PricePlanComparatorController {

    public static final String PRICE_PLAN_ID_KEY = "pricePlanId";
    public static final String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";
    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public PricePlanComparatorController(PricePlanService pricePlanService, AccountService accountService) {
        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

    /**
     * Description: Get Actual Price based on Smart Meter ID along with all price plans
     *
     * Get Price Plan ID based on Account Service by passing smart meter id
     * Call Price Plan Service to get All plans with Units
     *
     * What happens in Price Plan Service?
     *
     * Get All Meter Readings based on Smart Meter Id
     *
     * Stream with the base plans to calculate cost of each
     *
     * CalculateCost has calculateAverageReading & calculateTimeElapsed
     *
     * calculateTimeElapsed - Get Min & Max of the time & divide with 1 Hr
     * calculateAverageReading -  Use reduce Method to accumulate all readings. Then it divides with List Size
     *
     * Comes back to CalculateCost - Calculate avearge by diving with time lapsed &
     * then return the result by multiplying with price plan unit rate
     *
     * Request in Path Variable -  smartMeterId
     * @return Best Price Plan
     *
     * If a new meter is added via Store value for price plan would be returned null as it was not added.
     * If only one value is added in list it causes Arithmetic exception in calculateTimeElapsed becoz
     * diff b/w max & min is 0
     */
    @GetMapping("/compare-all/{smartMeterId}")
    public ResponseEntity<Map<String, Object>> calculatedCostForEachPricePlan(
            @PathVariable @NotBlank(message = "Smart Meter ID Can't be Empty/Blank") String smartMeterId) {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> pricePlanComparisons = new HashMap<>();
        pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
        pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionsForPricePlans.get());

        return consumptionsForPricePlans.isPresent()
                ? ResponseEntity.ok(pricePlanComparisons)
                : ResponseEntity.notFound().build();
    }

    /**
     * Description: Get Actual Price based on Smart Meter ID along with all price plans & Limit the Map
     *
     * Similar to compare-all method but as an addon this API limits the response
     *
     * Request in Path Variable -  smartMeterId & limit
     * @return Price Plan based on Limit
     */
    @GetMapping("/recommend/{smartMeterId}")
    public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(
            @PathVariable @NotBlank(message = "Smart Meter ID Can't be Empty/Blank") String smartMeterId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<Map.Entry<String, BigDecimal>> recommendations =
                new ArrayList<>(consumptionsForPricePlans.get().entrySet());

        /*
           This can be used in single shot than using Sorting & Limit separately but limit is not passed
           results in NPE. So additional logic like below can be implemented

           int responseLimit = (limit != null) ? limit : recommendations.size();

           recommendations = recommendations
                   .sort((pricePlan1, pricePlan2) ->
                           pricePlan1.getValue().compareTo(pricePlan2.getValue()))
                   .limit(responseLimit)
                   .toList();
        */

        recommendations.sort(Comparator.comparing(Map.Entry::getValue));

        if (limit != null && limit < recommendations.size()) {
            recommendations = recommendations.subList(0, limit);
        }

        return ResponseEntity.ok(recommendations);
    }
}
