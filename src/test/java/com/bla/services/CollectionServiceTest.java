package com.bla.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({SystemStubsExtension.class, MockitoExtension.class})
class CollectionServiceTest {
  @InjectMocks private CollectionService collectionService;

  @Mock private OkHttpClient okHttpClient;

  @Mock private ObjectMapper objectMapper;

  @Mock private JsonNode jsonNode;

  @Mock private JsonNode innerJsonNode;

  @Mock private Call call;
  @SystemStub private EnvironmentVariables environmentVariables;

  @BeforeEach
  void setup() {
    environmentVariables.set("collections", "collection_1,collection_2");
  }

  @Test
  void getCollectionsOfInterest_no_env_collections() {
    Assertions.assertThrows(
        NullPointerException.class, () -> collectionService.getCollectionsOfInterest());
  }

  @Test
  void getCollectionsOfInterest_opensea_timeout() throws IOException {
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenThrow(new SocketTimeoutException("ste"));

    Assertions.assertThrows(
        RuntimeException.class, () -> collectionService.getCollectionsOfInterest());
  }

  @Test
  void getCollectionsOfInterest_no_collections_of_interest() throws IOException {
    final var mockedRequest = new Request.Builder().url("http://testUrl.com").build();
    final var mockedResponse =
        new Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_2)
            .request(mockedRequest)
            .message("testMessage")
            .body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), "{}"))
            .build();
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(mockedResponse);
    when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
    when(jsonNode.at(anyString())).thenReturn(innerJsonNode);
    when(innerJsonNode.asDouble()).thenReturn(0.1);

    final var collections = collectionService.getCollectionsOfInterest();
    assertTrue(collections.isEmpty());
  }

  @Test
  void getCollectionsOfInterest_one_collection_of_interest() throws IOException {
    final var mockedRequest = new Request.Builder().url("http://testUrl.com").build();
    final var mockedResponse =
        new Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_2)
            .request(mockedRequest)
            .message("testMessage")
            .body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), "{}"))
            .build();
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(mockedResponse);
    when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
    when(jsonNode.at(anyString())).thenReturn(innerJsonNode);
    /*
    For the first collection, simulate a move in floorPrice + oneDayChange.
    The second collection should not be selected as a collection of interest.
    */
    when(innerJsonNode.asDouble())
        .thenReturn(99.0)
        .thenReturn(99.0) // first collection
        .thenReturn(0.1)
        .thenReturn(0.1); // second collection

    final var collections = collectionService.getCollectionsOfInterest();
    assertEquals(1, collections.size());
    assertEquals("collection_1", collections.get(0).getName());
  }
}
