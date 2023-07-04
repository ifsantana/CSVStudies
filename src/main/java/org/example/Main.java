package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class Main {
  public static void main(String[] args) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String fileName = "/Users/italosantana/Desktop/Distilled/CSVStudies/src/main/java/org/example/parking.csv";
    var reader = new FileReader(fileName);
    List<ParkingData> items = new CsvToBeanBuilder(reader)
        .withType(ParkingData.class)
        .build()
        .parse();

    printAvailableZoneIds();
    ZonedDateTime zdt = getZonedDateTimeForTest();

    var total = items.stream()
        .mapToDouble(parkingData -> parkingData.totalPrice)
        .sum();

    var totalBeforeSevenAm = items.stream()
        .filter(parkingData -> parkingData.entry.isAfter(zdt))
        .mapToDouble(parkingData -> parkingData.totalPrice)
        .sum();

    items.forEach(parkingData -> {
      try {
        System.out.println(
            mapper.writeValueAsString(parkingData)
        );
      } catch (JsonProcessingException ex) {
          throw new RuntimeException(ex.getMessage());
      }
    });
  }

  private static void printAvailableZoneIds() {
    ZoneId.getAvailableZoneIds().forEach(System.out::println);
  }

  private static ZonedDateTime getZonedDateTimeForTest() {
    ZoneId zoneId = ZoneId.of("GMT");
    return ZonedDateTime.of(2023, 5, 8, 6, 5, 59, 1234, zoneId);
  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class ParkingData implements Serializable {
    @CsvBindByName(column = "id")
    private Long id;

//    @CsvBindByName(column = "entry")
//    @CsvDate(
//        value = "dd-MM-yyyy HH:mm:ss Z",
//        writeFormat = "dd-MM-yyyy HH:mm:ss Z",
//        writeFormatEqualsReadFormat = false
//    )
    @CsvCustomBindByName(column = "entry", converter = LocalDateTimeConverter.class)
    private ZonedDateTime entry;


//    @CsvBindByName(column = "exit")
//    @CsvDate(
//        value = "dd-MM-yyyy HH:mm:ss Z",
//        writeFormat = "dd-MM-yyyy HH:mm:ss Z",
//        writeFormatEqualsReadFormat = false
//    )
    @CsvCustomBindByName(column = "exit", converter = LocalDateTimeConverter.class)
    private ZonedDateTime exit;

    @CsvBindByName(column = "total_price")
    private Double totalPrice;

    @CsvBindByName(column = "user_id")
    private Long userId;

    @CsvBindByName(column = "site")
    private String site;

  }

  public static class LocalDateTimeConverter extends AbstractBeanField {
    @Override
    protected ZonedDateTime convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss Z");
      formatter.withZone(ZoneId.of("GMT"));
      return ZonedDateTime.parse(s, formatter);
    }
  }
}