package com.bla.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.bla.services.CollectionService;
import com.bla.services.MessageService;
import com.bla.services.TelegramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;

public class CronHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  private final CollectionService collectionService;
  private final TelegramService telegramService;
  private final MessageService messageService;

  public CronHandler() {
    final var okHttpClient = new OkHttpClient();
    this.collectionService = new CollectionService(okHttpClient, new ObjectMapper());
    this.telegramService = new TelegramService(okHttpClient);
    this.messageService = new MessageService();
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      final APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, final Context context) {

    final var lambdaLogger = context.getLogger();
    final var headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");
    final var response = new APIGatewayProxyResponseEvent().withHeaders(headers);

    // Gather collections
    final var collectionsOfInterest = collectionService.getCollectionsOfInterest();
    lambdaLogger.log("Found " + collectionsOfInterest.size() + " collections of interest.");

    if (collectionsOfInterest.size() > 0) {
      telegramService.sendMessage(messageService.generateMessage(collectionsOfInterest));
    } else {
      telegramService.sendMessage(
          "Nothing to report regarding collections: " + System.getenv("COLLECTIONS"));
    }

    try {
      final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
      return response
          .withStatusCode(200)
          .withBody("pageContents: " + pageContents + ". Collections: " + collectionsOfInterest);
    } catch (IOException e) {
      return response.withBody("Server error.").withStatusCode(500);
    }
  }

  private String getPageContents(final String address) throws IOException {
    final var url = new URL(address);
    try (final BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(url.openStream()))) {
      return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
