# Energy Insights Platform

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

- **Backend**: Ingestion and analytics microservices
- **Frontend**: Dashboard visualizations (charts, bubble charts, scatter plots).
- **Messaging**: Kafka topics manage data flow between services.


#### Container View
<div align="center">
  <br>
  <img src="docs/c4-model/container.svg" width="800">
  <br>
</div>
