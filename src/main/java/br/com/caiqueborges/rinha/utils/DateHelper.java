package br.com.caiqueborges.rinha.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateHelper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private DateHelper() {
    }

    public static String getNowIsoUtc(long epochMillis) {
        return FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    public static long parseIsoUtcToEpochMillis(String isoUtcString) {
        return Instant.from(FORMATTER.parse(isoUtcString)).toEpochMilli();
    }
}
