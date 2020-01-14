package ru.hh.metrics.timinglogger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import ru.hh.nab.metrics.StatsDSender;
import ru.hh.nab.metrics.Tag;
import static java.util.Optional.ofNullable;

public final class Timings implements Closeable {
  private static ThreadLocal<Timings> INSTANCE_STORAGE = new ThreadLocal<>();

  private final static Tag WHOLE_TAG = new Tag("whole", "true");
  private final static String DEFAULT_TAG_NAME = "timing";

  private final List<TimingsLogger> loggers;
  private final Optional<String> overallMetric;
  private final List<Tag> tags;

  private StopWatch stopWatch;

  private Timings(List<TimingsLogger> loggers, String overallMetric, List<Tag> tags, boolean withThreadLocal) {
    this.loggers = loggers;
    this.overallMetric = ofNullable(overallMetric);
    this.tags = tags;
    if (withThreadLocal) {
      INSTANCE_STORAGE.set(this);
    }
    this.stopWatch = new StopWatch();
  }

  public static Timings get() {
    return ofNullable(INSTANCE_STORAGE.get()).orElseThrow(() -> new RuntimeException("ThreadLocal mode is not set or start is not called"));
  }

  public void timeWithDefaultTag(String value) {
    time(getOverallMetric(), new Tag(DEFAULT_TAG_NAME, value));
  }

  public void time(Tag... tags) {
    time(getOverallMetric(), tags);
  }

  public void time(String metricName, Tag... tags) {
    time(stopWatch.calcRecentDuration(), metricName, tags);
  }

  public void timeWholeWithDefaultTag(String value) {
    timeWhole(getOverallMetric(), new Tag(DEFAULT_TAG_NAME, value));
  }

  public void timeWhole(Tag... tags) {
    timeWhole(getOverallMetric(), tags);
  }

  public void timeWhole(String metricName, Tag... tags) {
    time(stopWatch.calcWholeDuration(), metricName, combineTagsIntoArray(List.of(WHOLE_TAG), tags));
  }

  public void resetTime() {
    stopWatch = new StopWatch();
  }

  public void close() {
    INSTANCE_STORAGE.remove();
  }

  private void time(long duration, String metricName, Tag... tags) {
    Tag[] tagsToSend = combineTagsIntoArray(this.tags, tags);
    loggers.forEach(sender -> sender.time(duration, metricName, tagsToSend));
  }

  private static Tag[] combineTagsIntoArray(List<Tag> list, Tag... array) {
    var tagsToSend = new ArrayList<>(Arrays.asList(array));
    tagsToSend.addAll(list);
    return tagsToSend.toArray(new Tag[]{});
  }

  private String getOverallMetric() {
    return overallMetric.orElseThrow(() -> new RuntimeException("metric name is not specified"));
  }

  public static class Builder {
    private StatsDSender statsDSender = null;
    private Logger slf4jLogger = null;
    private String metricName = null;
    private List<Tag> tags = new ArrayList<>();
    private boolean withThreadLocal = false;

    public Builder withStatsDSender(StatsDSender statsDSender) {
      this.statsDSender = statsDSender;
      return this;
    }

    public Builder withLogger(Logger logger) {
      this.slf4jLogger = logger;
      return this;
    }

    public Builder withMetric(String metricName) {
      this.metricName = metricName;
      return this;
    }

    public Builder withTag(Tag tag) {
      tags.add(tag);
      return this;
    }

    public Builder withThreadLocal() {
      this.withThreadLocal = true;
      return this;
    }

    public Timings start() {
      var loggers = new ArrayList<TimingsLogger>();

      if (statsDSender != null) {
        loggers.add(new StatsDTimingsLogger(statsDSender));
      }
      if (slf4jLogger != null) {
        loggers.add(new Slf4jTimingsLogger(slf4jLogger));
      }

      return new Timings(loggers, metricName, List.copyOf(tags), withThreadLocal);
    }
  }
}
