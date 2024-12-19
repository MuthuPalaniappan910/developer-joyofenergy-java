package uk.tw.energy.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.exception.NoPricePlanFoundException;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanCalculateService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
public class PriceCalculatorController {

    @Autowired
    private MeterReadingService meterReadingService;

    @Autowired
    private PricePlanCalculateService pricePlanCalculateService;


    @GetMapping("/{smartMeterId}/last-week-usage")
    public ResponseEntity<Double> readReadings(
            @PathVariable @NotBlank(message = "Smart Meter ID Can't be Empty/Blank") String smartMeterId) throws NoPricePlanFoundException {

        List<ElectricityReading> smartMeter = meterReadingService.checkIfSmartMeterIsPresentAndGetData(smartMeterId);

        return new ResponseEntity<> (pricePlanCalculateService.pricePlanCalculateService(smartMeter, smartMeterId), HttpStatus.OK);

    }
}
