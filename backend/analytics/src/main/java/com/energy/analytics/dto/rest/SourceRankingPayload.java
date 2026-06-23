package com.energy.analytics.dto.rest;

import com.energy.analytics.model.projection.SourceContribution;

import java.util.List;

public record SourceRankingPayload(
        List<SourceContribution> energy,
        List<SourceContribution> carbon
) {}
