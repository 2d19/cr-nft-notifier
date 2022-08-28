package com.bla.services;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TelegramService {

  private final OkHttpClient okHttpClient;

  public TelegramService(final OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
  }

  public void sendMessage(final String message) {
    final var request = prepareTelegramRequest(message);

    try (final var response = okHttpClient.newCall(request).execute()) {
      System.out.println("Response code from telegram post: " + response.code());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Request prepareTelegramRequest(final String message) {
    final var queryParams =
        "?chat_id=" + System.getenv("TG_CHAT_ID") + "&parse_mode=html&text=" + message;
    final var telegramUrl = System.getenv("TG_URL") + "sendMessage";

    return new Request.Builder()
        .url(telegramUrl + queryParams)
        .post(RequestBody.create("", null))
        .build();
  }
}
