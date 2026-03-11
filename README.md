# Energy Insights Platform

This projects processes energy grid data, and correlates it with both `weather phenomena` (e.g. wind droughts, cloud coverage, etc.) and `carbon intensity data` to investigate
real-time effects of weather phenomenons on the grid. For example: `wind drought + high cloud coverage → more non-renewable energy required → higher carbon intensity`.

Insights are visualized in a `dashboard frontend` (interactive charts) and optionally augmented with brief `interpretative summaries` from a small language model (SLM).

---

## Project Structure
```text
├── frontend       # Dashboard, charts, UI code
├── backend        # Java service exposing processed metrics to frontend
├── ingestion      # Python service fetching APIs, storing raw data
├── docker         # Dockerfiles, docker-compose configs
└── docs           # Documentation, diagrams, design notes
    └── C4 Model   # C4 diagrams and event diagrams
```
