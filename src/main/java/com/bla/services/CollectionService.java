package com.bla.services;

import com.bla.models.CollectionOfInterest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Call opensea and retrieve collection data based on input. Used in flow: cron -> lambda -> opensea
 * -> telegram. {@link TelegramService}
 *
 * <p>
 */
public class CollectionService {

  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;

  public CollectionService(final OkHttpClient okHttpClient, final ObjectMapper objectMapper) {
    this.okHttpClient = okHttpClient;
    this.objectMapper = objectMapper;
  }

  public List<CollectionOfInterest> getCollectionsOfInterest() {

    final var collections = System.getenv("COLLECTIONS").split(",");
    final var collectionsOfInterest = new ArrayList<CollectionOfInterest>();

    for (int index = 0; index < collections.length; index++) {
      processCollection(collectionsOfInterest, collections[index]);
      if (index % 4 == 0) {
        waitAMoment();
      }
    }
    return collectionsOfInterest;
  }

  private void processCollection(
      final ArrayList<CollectionOfInterest> collectionsOfInterest, final String collection) {

    final var url = System.getenv("OPENSEA_COLLECTION_URL") + collection + "/stats";
    System.out.println("Checking collection: " + url);
    final var request = new Request.Builder().url(url).get().build();

    try (final var response = okHttpClient.newCall(request).execute()) {
      handleResponse(collection, collectionsOfInterest, response);
    } catch (IOException ioException) {
      throw new RuntimeException("Processing failed for collection: " + collection, ioException);
    }
  }

  private void handleResponse(
      final String collectionName,
      final ArrayList<CollectionOfInterest> collectionsOfInterest,
      final Response response)
      throws IOException {

    final var body = response.body().string();
    final var collection = generateCollection(collectionName, body);

    if (isCollectionOfInterest(collection.getFloorPrice(), collection.getOneDayChange())) {
      // update by reference
      collectionsOfInterest.add(collection);
    }
  }

  private CollectionOfInterest generateCollection(final String collectionName, final String body)
      throws JsonProcessingException {

    final var floorPrice = objectMapper.readTree(body).at("/stats/floor_price").asDouble();
    final var oneDayChange = objectMapper.readTree(body).at("/stats/one_day_change").asDouble();

    final var collectionOfInterest = new CollectionOfInterest();
    collectionOfInterest.setName(collectionName);
    collectionOfInterest.setFloorPrice(
        new BigDecimal(floorPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());
    collectionOfInterest.setOneDayChange(
        new BigDecimal(oneDayChange).setScale(2, RoundingMode.HALF_UP).doubleValue());
    return collectionOfInterest;
  }

  private boolean isCollectionOfInterest(final double floorPrice, final double oneDayChange) {
    return floorPrice >= Double.parseDouble(System.getenv("MINIMUM_FLOOR_PRICE"))
        && oneDayChange > 0.25;
  }

  private void waitAMoment() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
