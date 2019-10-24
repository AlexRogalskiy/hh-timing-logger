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
    long duration = Duration.between(recentTimerStartedAt, Instant.now()).toMillis();
    recentTimerStartedAt = Instant.now();
    return duration;
  }

  public long calcWholeDuration() {
    long duration = Duration.between(overallTimerStartedAt, Instant.now()).toMillis();
    recentTimerStartedAt = Instant.now();
    return duration;
  }
}
