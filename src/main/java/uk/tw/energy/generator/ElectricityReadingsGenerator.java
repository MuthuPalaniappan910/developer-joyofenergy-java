package uk.tw.energy.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import uk.tw.energy.domain.ElectricityReading;

public class ElectricityReadingsGenerator {

    public List<ElectricityReading> generate(int number) {
        List<ElectricityReading> readings = new ArrayList<>();
        Instant now = Instant.now();

        Random readingRandomiser = new Random();

        /*

            Simplified method to refactor sorting using Java 8

            IntStream.range(0, number)
                    .forEach( i -> {
                        double positiveRandomValue = Math.abs(readingRandomiser.nextGaussian());
                        BigDecimal randomReading = BigDecimal.valueOf(positiveRandomValue).setScale(4, RoundingMode.CEILING);
                        ElectricityReading electricityReading = new ElectricityReading(now.minusSeconds(i * 10L), randomReading);
                        readings.add(electricityReading);
                    });

            readings
                    .sort((electricityReading1, electricityReading2) ->
                        electricityReading1.time().compareTo(electricityReading2.time())
                    );
         */

        for (int i = 0; i < number; i++) {
            double positiveRandomValue = Math.abs(readingRandomiser.nextGaussian());
            BigDecimal randomReading = BigDecimal.valueOf(positiveRandomValue).setScale(4, RoundingMode.CEILING);
            ElectricityReading electricityReading = new ElectricityReading(now.minusSeconds(i * 10L), randomReading);
            readings.add(electricityReading);
        }

        readings.sort(Comparator.comparing(ElectricityReading::time));
        return readings;
    }
}
