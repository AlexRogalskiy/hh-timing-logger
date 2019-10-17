package ru.hh.metrics.timinglogger;

import ru.hh.nab.metrics.Tag;

interface TimingsLogger {

  void time(long duration, String metricName, Tag... tags);
}
