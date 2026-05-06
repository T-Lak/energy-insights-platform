package com.energy.analytics.model.entity;

import com.energy.analytics.model.keys.SlidingWindowKey;
import com.energy.analytics.model.keys.SnapshotKey;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "energy_metrics")
@IdClass(RawMetricId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMetric {
    @Id
    private Instant timestamp;
    @Id
    private String region;
    @Id
    private String metric;
    @Id
    private String source;
    @Id
    private String category;

    private Double value;

    public SlidingWindowKey toWindowKey() {
        return new SlidingWindowKey(region, metric, source, category);
    }

    public SnapshotKey toSnapshotKey() {
        return new SnapshotKey(metric, source, category);
    }
}
