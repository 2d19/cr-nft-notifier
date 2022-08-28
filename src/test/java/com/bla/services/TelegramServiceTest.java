package com.bla.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketTimeoutException;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({SystemStubsExtension.class, MockitoExtension.class})
class TelegramServiceTest {

  @InjectMocks private TelegramService telegramService;

  @Mock private OkHttpClient okHttpClient;

  @Mock private Call call;

  @SystemStub private EnvironmentVariables environmentVariables;

  @Test
  void sendMessage_success() throws IOException {
    environmentVariables.set("TG_CHAT_ID", "chatId");
    environmentVariables.set("TG_URL", "http://tg.bla");
    final var mockedRequest = new Request.Builder().url("http://tg.bla").build();
    final var mockedResponse =
        new Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_2)
            .request(mockedRequest)
            .message("post ok.")
            .body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), "{}"))
            .build();
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(mockedResponse);

    telegramService.sendMessage("testMessage");

    final var requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(okHttpClient).newCall(requestCaptor.capture());
    final var requestCaptorValue = requestCaptor.getValue();
    assertTrue(requestCaptorValue.url().toString().contains("&parse_mode=html&text="));
    assertTrue(
        requestCaptorValue
            .url()
            .toString()
            .contains("sendmessage")); // Request works with either sendMessage or sendmessage.
  }

  @Test
  void sendMessage_telegram_timeout() throws IOException {
    environmentVariables.set("TG_CHAT_ID", "chatId");
    environmentVariables.set("TG_URL", "http://tg.bla");
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenThrow(new SocketTimeoutException("ste"));

    Assertions.assertThrows(
        RuntimeException.class, () -> telegramService.sendMessage("testMessage"));
  }
}
