/*
 * Copyright 2021 Crown Copyright (Single Trade Window)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.testing.FakeTicker;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cache.caffeine.CaffeineCache;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.constants.CacheConstants;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConfigTestUtils {
  public static CaffeineCache stubCaffeineCacheWithCacheDuration(String duration) {
    final Cache<Object, Object> cache =
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.parse(duration))
            .ticker(Ticker.systemTicker())
            .build();
    return new CaffeineCache(CacheConstants.SUPER_HEADERS_CACHE, cache);
  }

  public static CaffeineCache stubCaffeineCacheWithCacheDuration(
      String duration, FakeTicker fakeTicker) {
    final Cache<Object, Object> cache =
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.parse(duration))
            .ticker(fakeTicker::read)
            .build();
    return new CaffeineCache(CacheConstants.SUPER_HEADERS_CACHE, cache);
  }
}
