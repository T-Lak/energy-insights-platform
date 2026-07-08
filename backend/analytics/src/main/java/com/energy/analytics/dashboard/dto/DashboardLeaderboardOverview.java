package com.energy.analytics.dashboard.dto;

import com.energy.analytics.crossborder.model.projection.SourceContribution;

import java.util.List;

public record DashboardLeaderboardOverview(
        List<SourceContribution> energy,
        List<SourceContribution> carbon
) {}
