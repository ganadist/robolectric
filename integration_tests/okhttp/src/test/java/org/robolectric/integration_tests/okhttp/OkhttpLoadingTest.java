package org.robolectric.integration_tests.okhttp;

import java.io.IOException;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS )
public class OkhttpLoadingTest {

  @Test
  public void testOkHttpClientBulid() throws IOException {
    OkHttpClient client = new OkHttpClient.Builder().build();
  }
}
