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

This projects processes raw energy grid data,and established an event-driven microservice architecture, including asynchronous communication via Apache Kafka.

Insights are visualized in a `dashboard frontend` (interactive charts) and optionally augmented with brief `interpretative summaries` from a small language model (SLM).

---

## Demo

<div align="center">
  <video src="https://github.com/user-attachments/assets/cec77322-cd3b-4129-836a-30843b53d7f0" width="800"></video>
</div>

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

---

## Run Locally

### Prerequisites

Make sure you have the following installed:

- Docker
- Docker Compose
- ENTSO-E API Key (You can get one here: https://transparency.entsoe.eu/

The application is started using Docker Compose. All backend services, Kafka, and TimescaleDB are containerized.

### Configuration

Create a `.env` file in the ingestion's root folder:

```env
ENTSOE_API_KEY=<your_entsoe_api_key>
INGESTION_API_KEY=<your_ingestion_api_key>
```

### Start the Application
```bash
docker compose up -d --build
```

### Access the Application
```bash
http://localhost:4200
```

### Services
| Service       | URL                                         | Description                              |
| ------------- | ------------------------------------------- | ---------------------------------------- |
| Analytics API | http://localhost:8080                       | REST API Endpoints                       |
| Ingestion API | http://localhost:8000                       | Endpoints for backfills (API Key needed) |
| Swagger UI    | http://localhost:8080/swagger-ui/index.html | Interactive API documentation            |

