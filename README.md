# Energy Insights Platform

This projects processes energy grid data, and correlates it with both `weather phenomena` (e.g. wind droughts, cloud coverage, etc.) and `carbon intensity data` to investigate
real-time effects of weather phenomena on the grid. For example: `wind drought + high cloud coverage → more non-renewable energy required → higher carbon intensity` 
 (see [metrics](./docs/metrics.md)). 

Insights are visualized in a `dashboard frontend` (interactive charts) and optionally augmented with brief `interpretative summaries` from a small language model (SLM).

---

## Project Structure
```text
├── frontend/                 # Dashboard, charts, UI code
├── backend/                  # Ingestion and analytics microservices
│   ├── correlation-service/  # Java service exposing derived analytics 
│   └── ingestion/            # Python service fetching APIs, storing raw data 
├── docker/                   # Dockerfiles, docker-compose configs
└── docs/                     # Documentation, diagrams, design notes
    └── C4 Model/             # C4 diagrams and event diagrams
```

---

## Project Overview

This project follows a microservice architecture. Key components:

- **Backend**: Ingestion and analytics microservices
- **Frontend**: Dashboard visualizations (charts, bubble charts, scatter plots).
- **Messaging**: Kafka topics manage data flow between services.

<div align="center">
  <br>
  <img src="docs/c4-model/container.svg" width="800">
  <br>
</div>
