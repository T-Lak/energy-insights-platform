package com.energy.analytics.ingestion.repository;

import java.util.List;

public interface BatchRepository<T> {
    void upsertBatch(List<T> items);
}
