package ru.hh.metrics.timinglogger;

import ru.hh.nab.metrics.StatsDSender;
import ru.hh.nab.metrics.Tag;

class StatsDTimingsLogger implements TimingsLogger {
  private final StatsDSender statsDSender;

  StatsDTimingsLogger(StatsDSender statsDSender) {
    this.statsDSender = statsDSender;
  }

  @Override
  public void time(long duration, String metricName, Tag... tags) {
    statsDSender.sendTime(metricName, duration, tags);
  }
}
