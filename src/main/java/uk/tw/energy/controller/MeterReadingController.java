package uk.tw.energy.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.service.MeterReadingService;

@RestController
@RequestMapping("/readings")
@Validated
public class MeterReadingController {

    // Autowiring can be done by removing Constructor Injection
    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    /**
     * Description: Store Readings for the meter id
     *
     * Request : meterReadings object which accepts smart meter id & list of
     * Electricity reading which has time & reading
     * @return ok
     *
     * isMeterReadingsValid - Validates Not Null & Not Empty for Meter Id & List
     *
     * If success calls meterReadingService to store
     *
     * If Meter is already present data gets added along with existing ones
     * else
     * new meter gets created & then data gets added
     */
    @PostMapping("/store")
    public ResponseEntity storeReadings(@Valid @RequestBody MeterReadings meterReadings) {
        // This condition is redundant. Ideally it should be added as request validation.
        if (!isMeterReadingsValid(meterReadings)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        meterReadingService.storeReadings(meterReadings.smartMeterId(), meterReadings.electricityReadings());
        return ResponseEntity.ok().build();
    }

    private boolean isMeterReadingsValid(MeterReadings meterReadings) {
        String smartMeterId = meterReadings.smartMeterId();
        List<ElectricityReading> electricityReadings = meterReadings.electricityReadings();
        return smartMeterId != null
                && !smartMeterId.isEmpty()
                && electricityReadings != null
                && !electricityReadings.isEmpty();
    }

    /**
     * Description: Return the readings based on the input smart meter id
     *
     * Request in Path Variable -  smartMeterId
     * Response - List of electricity readings if Smart meter id is found (200) else an empty list (404)
     */
    @GetMapping("/read/{smartMeterId}")
    public ResponseEntity readReadings(
            @PathVariable @NotBlank(message = "Smart Meter ID Can't be Empty/Blank") String smartMeterId) {
        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
        return readings.isPresent()
                ? ResponseEntity.ok(readings.get())
                : ResponseEntity.notFound().build();
    }
}
