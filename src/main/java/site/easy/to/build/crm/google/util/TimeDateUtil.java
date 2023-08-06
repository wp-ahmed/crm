package site.easy.to.build.crm.google.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeDateUtil {

    public static List<TimeZoneLabel> getTimeZonesWithLabels() {
        List<TimeZoneLabel> timeZoneLabels = new ArrayList<>();
        Instant now = Instant.now();

        ZoneId.getAvailableZoneIds().stream()
                .distinct()
                .sorted()
                .map(ZoneId::of)
                .forEach(zoneId -> {
                    ZonedDateTime zonedDateTime = now.atZone(zoneId);
                    ZoneOffset zoneOffset = zonedDateTime.getOffset();
                    String offset = DateTimeFormatter.ofPattern("XXX").format(zoneOffset);

                    // Format the label as desired, e.g., "GMT+03:00 (Country Name)"
                    String label = zoneId.getId() + " (GMT" + offset + ")";

                    timeZoneLabels.add(new TimeZoneLabel(label, offset));
                });

        return timeZoneLabels;
    }

    public static Map<String, String> extractDateTime(String dateTime) {
        Map<String, String> dateTimeParts = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime, formatter);

        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

        String date = localDateTime.toLocalDate().toString();
        String time = localDateTime.toLocalTime().toString();
        String timeZone = zonedDateTime.getZone().toString();

        dateTimeParts.put("date", date);
        dateTimeParts.put("time", time);
        dateTimeParts.put("timeZone", timeZone);

        return dateTimeParts;
    }
}
