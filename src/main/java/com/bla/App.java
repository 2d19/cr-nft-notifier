package com.bla;

import com.bla.services.CollectionService;
import com.bla.services.MessageService;
import com.bla.services.TelegramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

/** Used for running locally only. */
public class App {
  private final CollectionService collectionService;
  private final TelegramService telegramService;
  private final MessageService messageService;

  App(
      final CollectionService collectionService,
      final TelegramService telegramService,
      final MessageService messageService) {
    this.collectionService = collectionService;
    this.telegramService = telegramService;
    this.messageService = messageService;
  }

  public static void main(String[] args) {
    final var okHttpClient = new OkHttpClient();
    final var app =
        new App(
            new CollectionService(okHttpClient, new ObjectMapper()),
            new TelegramService(okHttpClient),
            new MessageService());
    final var collectionsOfInterest = app.collectionService.getCollectionsOfInterest();

    System.out.println("\nFound " + collectionsOfInterest.size() + " collections of interest.\n");
    if (collectionsOfInterest.size() > 0) {
      app.telegramService.sendMessage(app.messageService.generateMessage(collectionsOfInterest));
    }
  }
}
