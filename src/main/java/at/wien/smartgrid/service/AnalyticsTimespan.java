package at.wien.smartgrid.service;

import java.time.Duration;
import java.util.Arrays;

public enum AnalyticsTimespan {
    ONE_HOUR("1h", Duration.ofHours(1)),
    SIX_HOURS("6h", Duration.ofHours(6)),
    TWENTY_FOUR_HOURS("24h", Duration.ofHours(24)),
    SEVEN_DAYS("7d", Duration.ofDays(7));

    private final String queryValue;
    private final Duration duration;

    AnalyticsTimespan(String queryValue, Duration duration) {
        this.queryValue = queryValue;
        this.duration = duration;
    }

    public static AnalyticsTimespan parse(String value) {
        return Arrays.stream(values())
                .filter(item -> item.queryValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported timespan '%s'. Allowed values: 1h, 6h, 24h, 7d".formatted(value)));
    }

    public String queryValue() {
        return queryValue;
    }

    public Duration duration() {
        return duration;
    }
}

