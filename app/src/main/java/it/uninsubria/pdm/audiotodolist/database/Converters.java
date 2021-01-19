package it.uninsubria.pdm.audiotodolist.database;

import androidx.room.TypeConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Class that allows mapping and conversion of complex types to types that can be stored in an SQLite database.
 */
public class Converters {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    @TypeConverter
    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(dateTimeFormatter);
    }

    @TypeConverter
    public static LocalDateTime dateTimeFromString(String toParse) {
        return toParse == null ? null : LocalDateTime.parse(toParse, dateTimeFormatter);
    }

    @TypeConverter
    public static Long durationToLong(Duration duration) {
        return duration == null ? null : duration.toMillis();
    }

    @TypeConverter
    public static Duration durationFromLong(Long millis) {
        return millis == null ? null : Duration.ofMillis(millis);
    }
}
