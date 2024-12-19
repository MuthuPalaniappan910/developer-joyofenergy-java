package uk.tw.energy.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MeterReadings(
        // RegExp can also be added for Smart Meter ID
        @NotBlank(message = "Smart Meter ID Can't be Empty/Blank") String smartMeterId,
        @NotNull(message = "ElectricityReading Can't be Null") @Size(min = 1, message = "Minimum Reading should be Present in Request")
                List<ElectricityReading> electricityReadings) {}
