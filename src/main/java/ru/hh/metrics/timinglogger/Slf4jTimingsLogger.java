package ru.hh.metrics.timinglogger;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import ru.hh.nab.metrics.Tag;

class Slf4jTimingsLogger implements TimingsLogger {
  private final Logger logger;

  Slf4jTimingsLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void time(long duration, String metricName, Tag... tags) {
    String message = metricName +
        "." +
        Arrays.stream(tags).map(tag -> tag.name + "=" + tag.value).collect(Collectors.joining(".")) +
        " - Duration: " +
        duration;
    logger.debug(message);
  }
}
