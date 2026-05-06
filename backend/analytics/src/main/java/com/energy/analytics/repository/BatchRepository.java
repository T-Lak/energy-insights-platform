package com.energy.analytics.repository;

import java.util.List;

public interface BatchRepository<T> {
    void upsertBatch(List<T> items);
}
