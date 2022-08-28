package com.bla.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.bla.services.CollectionService;
import com.bla.services.MessageService;
import com.bla.services.TelegramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

public class CronHandler {

  /**
   * Act upon incoming calls (via cron) to this lambda.
   *
   * @param context aws context
   */
  public Void handleRequest(final Context context) {
    final var lambdaLogger = context.getLogger();
    final var okHttpClient = new OkHttpClient();
    final var collectionService = new CollectionService(okHttpClient, new ObjectMapper());
    final var telegramService = new TelegramService(okHttpClient);
    final var messageService = new MessageService();

    // Gather collections
    final var collectionsOfInterest = collectionService.getCollectionsOfInterest();
    lambdaLogger.log("Found " + collectionsOfInterest.size() + " collections of interest.");

    if (collectionsOfInterest.size() > 0) {
      telegramService.sendMessage(messageService.generateMessage(collectionsOfInterest));
    } else {
      telegramService.sendMessage(
          "Nothing to report regarding collections: " + System.getenv("collections"));
    }
    return null;
  }
}
