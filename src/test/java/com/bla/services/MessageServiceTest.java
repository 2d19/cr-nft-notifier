package com.bla.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bla.models.CollectionOfInterest;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageServiceTest {

  private final MessageService messageService = new MessageService();

  @Test
  void generateMessage_success() {
    final var collectionOfInterest_1 = new CollectionOfInterest();
    collectionOfInterest_1.setOneDayChange(1.0);
    collectionOfInterest_1.setName("test1");
    collectionOfInterest_1.setFloorPrice(2.0);
    final var collectionOfInterest_2 = new CollectionOfInterest();
    collectionOfInterest_2.setOneDayChange(2.0);
    collectionOfInterest_2.setName("test2");
    collectionOfInterest_2.setFloorPrice(3.0);

    final var collectionsOfInterest = List.of(collectionOfInterest_1, collectionOfInterest_2);
    final var outcome = messageService.generateMessage(collectionsOfInterest);
    assertEquals(
        "%3Cb%3ECollections+of+interest%3C%2Fb%3E%3A+%0A1.+%3Ca+href%3D%22https%3A%2F%2Fopensea.io%2Fcollection%2Ftest1%22%3Etest1%3C%2Fa%3E%2C+%3Ci%3Efloor%3C%2Fi%3E+%3D+2.0%2C+%3Ci%3Echange%3C%2Fi%3E+%3D+1.0+%0A2.+%3Ca+href%3D%22https%3A%2F%2Fopensea.io%2Fcollection%2Ftest2%22%3Etest2%3C%2Fa%3E%2C+%3Ci%3Efloor%3C%2Fi%3E+%3D+3.0%2C+%3Ci%3Echange%3C%2Fi%3E+%3D+2.0+%0A%0AData+gathered+from+%3Ca+href%3D%22https%3A%2F%2Fopensea.io%2F%22%3Eopensea%3C%2Fa%3E",
        outcome);
  }
}
