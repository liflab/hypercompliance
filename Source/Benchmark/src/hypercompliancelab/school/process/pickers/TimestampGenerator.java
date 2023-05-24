package hypercompliancelab.school.process.pickers;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/***
 * A utility class that generates unique timestamps for individual case ids.
 */
public class TimestampGenerator {
    private static Map<String, Long> lastGeneratedTime = new HashMap<>();

    public static long generate(String id) {
        long currentTimeMillis;
        if (lastGeneratedTime.containsKey(id)) {
            currentTimeMillis = lastGeneratedTime.get(id);
        } else {
            currentTimeMillis = generateRandomTimeBetweenNowAndXDays(1);
            lastGeneratedTime.put(id, currentTimeMillis);
            return currentTimeMillis;
        }

        long randomDelayInMillis = ThreadLocalRandom.current().nextLong(Duration.ofDays(1).toMillis(), Duration.ofDays(2).toMillis());

        long nextTime = currentTimeMillis + randomDelayInMillis;
        lastGeneratedTime.put(id, nextTime);

        return nextTime;
    }

    private static long generateRandomTimeBetweenNowAndXDays(int x) {
        long now = Instant.now().toEpochMilli();
        long tomorrow = Instant.now().plus(x, ChronoUnit.DAYS).toEpochMilli();
        return ThreadLocalRandom.current().nextLong(now, tomorrow);
    }

    public static void main(String[] args) {
        String id1 = "123";
        String id2 = "456";

        long time1 = TimestampGenerator.generate(id1);
        long time2 = TimestampGenerator.generate(id2);
        long time3 = TimestampGenerator.generate(id1);

        System.out.println("Time 1: " + time1);
        System.out.println("Time 2: " + time2);
        System.out.println("Time 3: " + time3);

        // Assert that the times are in ascending order
        assert time1 <= time2;
        assert time2 <= time3;

        // Assert that the delay between time1 and time3 is between 1 and 2 days
        long delayDays = (time3 - time1) / (1000 * 60 * 60 * 24);
        assert delayDays >= 1 && delayDays <= 2;
    }

}
