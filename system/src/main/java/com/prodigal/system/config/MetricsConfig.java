package com.prodigal.system.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 指标配置：统一标签 + 直方图桶定义
 */
@Configuration
public class MetricsConfig {

    public static final String METRIC_UPLOAD_TOTAL = "pictures.upload.total";
    public static final String METRIC_UPLOAD_SIZE = "pictures.upload.size.bytes";
    public static final String METRIC_UPLOAD_DURATION = "pictures.upload.duration";
    public static final String METRIC_DELETE_TOTAL = "pictures.delete.total";
    public static final String METRIC_OUTPAINTING_TOTAL = "pictures.outpainting.total";
    public static final String METRIC_CLEANUP_DURATION = "pictures.cleanup.last.duration";
    public static final String METRIC_CLEANUP_DELETED = "pictures.cleanup.deleted.total";

    public static final String TAG_STATUS = "status";
    public static final String TAG_REASON = "reason";

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return registry -> registry.config()
                .commonTags("application", "prodigal-picture")
                .meterFilter(new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(
                            io.micrometer.core.instrument.Meter.Id id,
                            DistributionStatisticConfig config) {
                        if (id.getName().startsWith("pictures.")) {
                            return DistributionStatisticConfig.builder()
                                    .percentiles(0.5, 0.95, 0.99)
                                    .build()
                                    .merge(config);
                        }
                        return config;
                    }
                });
    }
}
