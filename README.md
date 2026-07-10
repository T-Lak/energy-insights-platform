# Energy Insights Platform

<p align="left">
  <img src="https://img.shields.io/badge/Backend-Java%20Spring%20Boot-6db33f">
  <img src="https://img.shields.io/badge/Backend-Python%20FastAPI-05998b">
  <img src="https://img.shields.io/badge/Frontend-Angular-dd0031">
  <img src="https://img.shields.io/badge/Language-Java-007396">
  <img src="https://img.shields.io/badge/Language-Python-3776ab">
  <img src="https://img.shields.io/badge/Language-TypeScript-3178c6">
</p>
<p align="left">
  <img src="https://img.shields.io/badge/Database-PostgreSQL-336791">
  <img src="https://img.shields.io/badge/Timeseries-TimescaleDB-f29111">
  <img src="https://img.shields.io/badge/Data-ENTSO--E%20API-3a83c8">
  <img src="https://img.shields.io/badge/Chart-amCharts%205-000000">
  <img src="https://img.shields.io/badge/Chart-AG%20Charts-2196f3">
  <img src="https://img.shields.io/badge/License-MIT-f5e598.svg">
</p>

<p align="left">
  <img src="left_stretching.gif" width="300"/>
  <img src="standing-rotation.gif" width="300"/>
</p>

This projects processes raw energy grid data,and established an event-driven microservice architecture, including asynchronous communication via Apache Kafka.

Insights are visualized in a `dashboard frontend` (interactive charts) and optionally augmented with brief `interpretative summaries` from a small language model (SLM).

---

## Project Structure
```text
├── .github/workflows/        # CI pipelines
├── api/                      # API specifications
├── frontend/                 # Angular Dashboard, including charts and tables
├── backend/                  # Ingestion and analytics microservices
│   ├── analytics/            # Java service exposing derived analytics via REST and Web Sockets
│   └── ingestion/            # Python service fetching APIs, publishing raw data 
└── docs/                     # Documentation, diagrams, design notes
    └── C4 Model/             # C4 diagrams and event diagrams
```

---

## Project Overview

This project follows a microservice architecture. Key components:

- **Backend**: Ingestion, analytics, and interpreation microservices
- **Frontend**: Dashboard visualizations (charts, bubble charts, scatter plots).
- **Messaging**: Kafka topics manage data flow between services.


#### Container View
<div align="center">
  <br>
  <img src="docs/c4-model/container.svg" width="800">
  <br>
</div>
