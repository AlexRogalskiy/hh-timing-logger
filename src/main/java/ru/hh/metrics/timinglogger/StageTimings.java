package ru.hh.metrics.timinglogger;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import ru.hh.nab.metrics.Histograms;
import ru.hh.nab.metrics.StatsDSender;
import ru.hh.nab.metrics.Tag;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public final class StageTimings<T extends Enum<T>> {
  private final Histograms histograms;
  private final Map<T, Tag> tags;
  //per instance threadlocal
  private final ThreadLocal<StopWatch> instanceStorage = new ThreadLocal<>();

  private StageTimings(Histograms histograms, Map<T, Tag> tags) {
    this.histograms = histograms;
    this.tags = new EnumMap<>(tags);
  }

  public void start() {
    instanceStorage.set(new StopWatch());
  }

  public void markStage(T stage) {
    int duration = ofNullable(instanceStorage.get()).map(StopWatch::calcRecentDuration).map(Number::intValue)
      .orElseThrow(() -> new RuntimeException("No instance in threadlocal - maybe start is not called"));
    histograms.save(duration, tags.get(stage));
  }

  public static class Builder<T extends Enum<T>> {
    private final Class<T> tagSourceEnumClass;
    private final String metricName;
    private String tagName;
    private int maxHistogramSize;
    private int[] percentiles = StatsDSender.DEFAULT_PERCENTILES;

    public Builder(String metricName, Class<T> tagSourceEnumClass) {
      this.tagSourceEnumClass = tagSourceEnumClass;
      this.metricName = metricName;
    }

    public Builder<T> withTagName(String tagName) {
      this.tagName = tagName;
      return this;
    }

    public Builder<T> withMaxHistogramSize(int maxHistogramSize) {
      this.maxHistogramSize = maxHistogramSize;
      return this;
    }

    public Builder<T> withPercentiles(int... percentiles) {
      this.percentiles = percentiles;
      return this;
    }

    public StageTimings<T> startOn(StatsDSender statsDSender, int sendIntervalMs) {
      var enumConstants = EnumSet.allOf(tagSourceEnumClass);
      Histograms timings = new Histograms(maxHistogramSize, enumConstants.size());
      var tagMap = enumConstants.stream().collect(
        toMap(Function.identity(), enumElement -> new Tag(ofNullable(tagName).orElse(metricName), enumElement.name()))
      );
      var stages = new StageTimings<>(timings, tagMap);
      statsDSender.sendPeriodically(() -> statsDSender.sendHistograms(metricName, stages.histograms, percentiles), sendIntervalMs);
      return stages;
    }
  }
}
