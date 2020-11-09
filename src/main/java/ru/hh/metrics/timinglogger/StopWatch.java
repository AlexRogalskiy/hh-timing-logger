package ru.hh.metrics.timinglogger;

import java.time.Duration;
import java.time.Instant;

final class StopWatch {
  private final Instant overallTimerStartedAt;
  private Instant recentTimerStartedAt;

  StopWatch() {
    overallTimerStartedAt = Instant.now();
    recentTimerStartedAt = overallTimerStartedAt;
  }

  public long calcRecentDuration() {
    Instant now = Instant.now();
    long duration = Duration.between(recentTimerStartedAt, now).toMillis();
    recentTimerStartedAt = now;
    return duration;
  }

  public long calcWholeDuration() {
    Instant now = Instant.now();
    long duration = Duration.between(overallTimerStartedAt, now).toMillis();
    recentTimerStartedAt = now;
    return duration;
  }
}
