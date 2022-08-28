package com.bla.handlers;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CronHandlerTest {

  private final CronHandler cronHandler = new CronHandler();
  @Mock private Context context;

  @Test
  void handleRequest() {}
}
