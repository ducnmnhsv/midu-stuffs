# TradeX Stability Monitor Tool - Dashboard Mockups

> [!NOTE]
> This document showcases the visual design and layout of key Grafana dashboards for the TradeX Stability Monitor Tool.

---

## Dashboard Overview

The monitoring system includes **6 primary dashboards** organized by functional area:

1. **Infrastructure Monitoring Dashboard** - Real-time system health
2. **Job Monitoring Dashboard** - Init job status and management
3. **API Performance Dashboard** - Real-time API metrics
4. **API Historical Analysis Dashboard** - Trend analysis and reporting
5. **Remediation Dashboard** - Automated action tracking
6. **Overview Dashboard** - High-level system status

---

## 1. Infrastructure Monitoring Dashboard

### Purpose
Provides real-time visibility into all critical infrastructure components: Kafka, Redis, Database, and Application Servers.

### Dashboard Layout

![Infrastructure Monitoring Dashboard](/Users/ducnguyen/.gemini/antigravity/brain/f2fa44f8-f78f-49c3-bb40-935f31eed462/infrastructure_dashboard_1768793144395.png)

### Panel Descriptions

#### Top Row: Status Indicators
**Panel Type**: Stat Panels (4 panels)

| Panel | Metric | Threshold | Color Coding |
|-------|--------|-----------|--------------|
| Kafka Brokers | `count(up{job="kafka"})` | 3 expected | Green: all up, Red: any down |
| Redis Memory | `redis_memory_used_bytes / redis_memory_max_bytes * 100` | 90% warning | Green: <70%, Yellow: 70-90%, Red: >90% |
| DB Connections | `pg_stat_database_numbackends` | 100 max | Green: <80, Yellow: 80-95, Red: >95 |
| App Servers | `count(up{job="node-exporter"})` | 2 expected | Green: all up, Red: any down |

#### Second Row: Kafka & Redis Monitoring
**Left Panel**: Kafka Consumer Lag (Time Series)
- **Query**: `kafka_consumergroup_lag{topic=~".*"}`
- **Visualization**: Multi-line graph
- **Y-axis**: Lag count (messages)
- **Legend**: Consumer group names
- **Alert Threshold**: Lag > 10,000 messages

**Right Panel**: Redis Memory Usage (Area Chart)
- **Query**: `redis_memory_used_bytes / redis_memory_max_bytes * 100`
- **Visualization**: Area chart with threshold line
- **Y-axis**: Percentage (0-100%)
- **Threshold Line**: 90% (red)
- **Alert**: Memory > 90% triggers auto-remediation

#### Third Row: Database & Server Metrics
**Left Panel**: Database Connection Pool (Stacked Area)
- **Queries**:
  - Active: `pg_stat_database_numbackends{state="active"}`
  - Idle: `pg_stat_database_numbackends{state="idle"}`
  - Waiting: `pg_stat_database_numbackends{state="waiting"}`
- **Visualization**: Stacked area chart
- **Colors**: Blue (active), Green (idle), Yellow (waiting)

**Right Panel**: Application Server CPU Usage (Multi-line)
- **Query**: `100 - (avg by (instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)`
- **Visualization**: Line graph per server
- **Y-axis**: CPU percentage (0-100%)
- **Alert**: CPU > 80% for 5 minutes

#### Bottom Row: Disk Usage
**Panel Type**: Bar Gauge (Horizontal)
- **Query**: `(node_filesystem_size_bytes - node_filesystem_free_bytes) / node_filesystem_size_bytes * 100`
- **Visualization**: Horizontal bar gauge per server/mount
- **Thresholds**: 
  - Green: 0-70%
  - Yellow: 70-85%
  - Red: 85-100%
- **Display**: Shows both percentage and absolute values (e.g., "450 GB / 1 TB")

### Dashboard Settings
- **Refresh Rate**: 15 seconds (auto-refresh)
- **Time Range**: Last 6 hours (default)
- **Variables**: 
  - `$environment` - Filter by environment (prod, staging)
  - `$server` - Filter by specific server

---

## 2. Job Monitoring Dashboard

### Purpose
Monitor init job execution status, provide retry capability, and access detailed logs.

### Dashboard Layout

![Job Monitoring Dashboard](/Users/ducnguyen/.gemini/antigravity/brain/f2fa44f8-f78f-49c3-bb40-935f31eed462/job_monitoring_dashboard_1768793171858.png)

### Panel Descriptions

#### Top Row: Job Statistics
**Panel Type**: Stat Panels (4 panels)

| Panel | Metric | Calculation |
|-------|--------|-------------|
| Total Jobs | `count(tradex_job_status)` | Count of all jobs |
| Success Rate | `count(tradex_job_status{status="OK"}) / count(tradex_job_status) * 100` | Percentage |
| Failed Jobs | `count(tradex_job_status{status="FAIL"})` | Count |
| Running Jobs | `count(tradex_job_status{status="RUNNING"})` | Count |

#### Second Row: Job Status Table
**Panel Type**: Table with Transformations

**Columns**:
1. **Job Name** - From label `job_name`
2. **Status** - Icon + text based on `tradex_job_status`
   - ✓ OK (green)
   - ✗ FAIL (red)
   - ⟳ RUNNING (blue)
   - ⏸ WAITING (gray)
3. **Start Time** - From `tradex_job_start_timestamp`
4. **Duration** - Calculated: `time() - tradex_job_start_timestamp`
5. **Last Run** - Relative time since last execution
6. **Actions** - Data links:
   - **Retry**: Webhook to `http://remediation-service/api/jobs/{job_name}/retry`
   - **View Logs**: Link to Loki logs filtered by job name

**Queries**:
```promql
tradex_job_status
tradex_job_start_timestamp
tradex_job_duration_seconds
tradex_job_last_success_timestamp
```

**Table Features**:
- Alternating row colors for readability
- Status column with conditional formatting
- Clickable action buttons
- Sort by any column
- Search/filter capability

#### Third Row: Job Trends
**Left Panel**: Job Duration Trends (Time Series)
- **Query**: `tradex_job_duration_seconds{job_name=~".*"}`
- **Visualization**: Multi-line graph (one line per job)
- **Y-axis**: Duration in seconds
- **Purpose**: Identify jobs getting slower over time

**Right Panel**: Job Execution Timeline (Bar Chart)
- **Query**: `tradex_job_start_timestamp`
- **Visualization**: Horizontal bar chart (Gantt-style)
- **X-axis**: Time of day
- **Y-axis**: Job names
- **Color Coding**:
  - Green: Successful runs
  - Red: Failed runs
  - Blue: Currently running

#### Bottom Row: Recent Job Logs
**Panel Type**: Logs (Loki)
- **Query**: `{job="tradex-jobs"} |= ""`
- **Visualization**: Log panel with syntax highlighting
- **Features**:
  - Color-coded log levels:
    - INFO: Blue
    - WARN: Yellow
    - ERROR: Red
  - Expandable entries for stack traces
  - Full-text search
  - Filter by job name
  - Time range selection

### Dashboard Settings
- **Refresh Rate**: 15 seconds
- **Time Range**: Last 24 hours (default)
- **Variables**:
  - `$job_name` - Filter by specific job
  - `$status` - Filter by job status

---

## 3. API Performance Dashboard

### Purpose
Real-time monitoring of API performance metrics with detailed endpoint-level visibility.

### Dashboard Layout

![API Performance Dashboard](/Users/ducnguyen/.gemini/antigravity/brain/f2fa44f8-f78f-49c3-bb40-935f31eed462/api_performance_dashboard_1768793198831.png)

### Panel Descriptions

#### Top Row: Summary Statistics
**Panel Type**: Stat Panels (6 panels)

| Panel | Query | Format |
|-------|-------|--------|
| Total Requests | `sum(increase(http_requests_total[24h]))` | Number with comma separator |
| Success Rate | `sum(rate(http_requests_total{status=~"2.."}[5m])) / sum(rate(http_requests_total[5m])) * 100` | Percentage (1 decimal) |
| Error Rate | `sum(rate(http_requests_total{status=~"[45].."}[5m])) / sum(rate(http_requests_total[5m])) * 100` | Percentage (1 decimal) |
| Avg Response Time | `avg(rate(http_request_duration_seconds_sum[5m]) / rate(http_request_duration_seconds_count[5m])) * 1000` | Milliseconds |
| Min Response Time | `min(http_request_duration_seconds_bucket{le="0.01"}) * 1000` | Milliseconds |
| Max Response Time | `max(http_request_duration_seconds_bucket) * 1000` | Milliseconds |

#### Second Row: Request & Latency Trends
**Left Panel**: Request Rate (Area Chart)
- **Query**: `sum(rate(http_requests_total[1m]))`
- **Visualization**: Area chart
- **Y-axis**: Requests per second
- **Purpose**: Monitor traffic patterns

**Right Panel**: Response Time Percentiles (Multi-line)
- **Queries**:
  - P50: `histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m]))`
  - P95: `histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))`
  - P99: `histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))`
- **Visualization**: Line graph
- **Y-axis**: Latency in milliseconds
- **Colors**: Green (P50), Yellow (P95), Red (P99)

#### Third Row: Endpoint Analysis
**Left Panel**: Error Rate by Endpoint (Horizontal Bar)
- **Query**: `sum by (endpoint) (rate(http_requests_total{status=~"[45].."}[5m])) / sum by (endpoint) (rate(http_requests_total[5m])) * 100`
- **Visualization**: Horizontal bar chart
- **X-axis**: Error rate percentage
- **Threshold**: Highlight endpoints with >1% error rate in red

**Right Panel**: Top 10 Slowest Endpoints (Table)
- **Columns**:
  - Endpoint Path
  - Avg Time (ms)
  - P95 Time (ms)
  - Request Count
- **Query**: `topk(10, avg by (endpoint) (rate(http_request_duration_seconds_sum[5m]) / rate(http_request_duration_seconds_count[5m])))`
- **Sort**: By Avg Time (descending)

#### Fourth Row: Response Time Heatmap
**Panel Type**: Heatmap
- **Query**: `sum(rate(http_request_duration_seconds_bucket[5m])) by (le, endpoint)`
- **X-axis**: Time
- **Y-axis**: API endpoints
- **Color Scale**: 
  - Green: Fast (<100ms)
  - Yellow: Medium (100-500ms)
  - Orange: Slow (500-1000ms)
  - Red: Very slow (>1000ms)
- **Purpose**: Visualize performance patterns across time and endpoints

#### Bottom Row: API Type Comparison
**Panel Type**: Stat Groups (2 groups)

**TradeX Internal APIs**:
- Total Requests: `sum(increase(http_requests_total{api_type="tradex"}[24h]))`
- Avg Response Time: `avg(rate(http_request_duration_seconds_sum{api_type="tradex"}[5m]) / rate(http_request_duration_seconds_count{api_type="tradex"}[5m])) * 1000`
- Success Rate: `sum(rate(http_requests_total{api_type="tradex",status=~"2.."}[5m])) / sum(rate(http_requests_total{api_type="tradex"}[5m])) * 100`

**Lotte External APIs**:
- Same metrics as above, filtered by `api_type="lotte"`

### Dashboard Settings
- **Refresh Rate**: 30 seconds
- **Time Range**: Last 24 hours (default)
- **Variables**:
  - `$endpoint` - Filter by specific endpoint
  - `$api_type` - Filter by TradeX/Lotte
  - `$status_code` - Filter by HTTP status code

---

## 4. API Historical Analysis Dashboard

### Purpose
Long-term trend analysis and performance comparison over time.

### Key Features
- Date range picker (last 7 days, 30 days, custom)
- Day-over-day comparison
- Week-over-week comparison
- Export to CSV/Excel functionality

### Panel Types
1. **Trend Graphs**: Show metric changes over selected time range
2. **Comparison Tables**: Side-by-side comparison of different time periods
3. **Aggregation Tables**: Per-endpoint statistics with sorting/filtering

### Example Panels
- **Daily Request Volume**: Bar chart showing requests per day
- **Latency Trends**: Line graph showing P95 latency over time
- **Error Rate Trends**: Area chart showing error percentage over time
- **Endpoint Performance Table**: Sortable table with all metrics per endpoint

---

## 5. Remediation Dashboard

### Purpose
Track automated remediation actions and their outcomes.

### Key Panels
1. **Remediation Summary**:
   - Total actions taken (last 24h)
   - Success rate
   - Failed remediations requiring attention

2. **Remediation Timeline**:
   - Time-series graph showing when remediations occurred
   - Color-coded by action type (Redis cleanup, Kafka scaling, etc.)

3. **Remediation Audit Table**:
   - Timestamp
   - Action type
   - Trigger condition
   - Result (success/failure)
   - Duration
   - Link to detailed logs

4. **Redis Memory Remediation**:
   - Memory before/after cleanup
   - Keys evicted
   - Time to complete

### Loki Integration
- All remediation actions logged to Loki
- Searchable audit trail
- 90-day retention for compliance

---

## 6. Overview Dashboard

### Purpose
High-level system health at a glance for executives and stakeholders.

### Layout
**Single-page overview with**:
- System health score (0-100)
- Critical alerts count
- Service availability (uptime %)
- Key metrics summary:
  - Infrastructure status (all green/yellow/red)
  - Job success rate (last 24h)
  - API performance (avg latency, error rate)
  - Recent remediation actions

**Visual Design**:
- Large, easy-to-read numbers
- Traffic light color coding
- Minimal detail, maximum clarity
- Auto-refresh every 30 seconds

---

## Design Guidelines

### Color Palette

**Status Colors**:
- 🟢 Green: Healthy, OK, Success (`#73BF69`)
- 🟡 Yellow: Warning, Degraded (`#F2CC0C`)
- 🔴 Red: Critical, Failed, Error (`#E02F44`)
- 🔵 Blue: Running, In Progress (`#5794F2`)
- ⚪ Gray: Waiting, Idle, Unknown (`#6E6E6E`)

**Chart Colors** (for multi-line graphs):
- Line 1: `#7EB26D` (Green)
- Line 2: `#EAB839` (Yellow)
- Line 3: `#6ED0E0` (Cyan)
- Line 4: `#EF843C` (Orange)
- Line 5: `#E24D42` (Red)
- Line 6: `#1F78C1` (Blue)

### Typography
- **Dashboard Title**: 24px, Bold
- **Panel Title**: 16px, Semi-bold
- **Stat Value**: 32-48px, Bold
- **Stat Label**: 12px, Regular
- **Table Text**: 13px, Regular

### Layout Principles
1. **Grid System**: Use 24-column grid for consistent alignment
2. **Panel Heights**: 
   - Stat panels: 4 units
   - Graphs: 8-10 units
   - Tables: 10-12 units
3. **Spacing**: 8px gap between panels
4. **Responsive**: Panels stack vertically on mobile

### Interaction Patterns
1. **Drill-down**: Click on panel title to expand to full screen
2. **Time Range**: Global time picker in top-right
3. **Filters**: Dashboard variables for filtering data
4. **Export**: Download button for CSV/Excel export
5. **Alerts**: Bell icon shows active alerts

---

## Dashboard Variables

### Common Variables Across Dashboards

| Variable | Type | Query | Purpose |
|----------|------|-------|---------|
| `$environment` | Query | `label_values(up, env)` | Filter by environment |
| `$server` | Query | `label_values(up{job="node-exporter"}, instance)` | Filter by server |
| `$time_range` | Interval | `1m,5m,15m,1h,6h,24h` | Aggregation interval |
| `$job_name` | Query | `label_values(tradex_job_status, job_name)` | Filter by job |
| `$endpoint` | Query | `label_values(http_requests_total, endpoint)` | Filter by API endpoint |

### Variable Usage
Variables are displayed as dropdowns at the top of each dashboard, allowing users to filter data dynamically without editing queries.

---

## Alert Annotations

### Purpose
Display alert events directly on time-series graphs to correlate alerts with metric changes.

### Configuration
- **Data Source**: Grafana Alerting
- **Display**: Vertical line with alert icon
- **Tooltip**: Shows alert name, severity, and message
- **Color**: Matches alert severity (red for critical, yellow for warning)

### Example
When a "High Redis Memory" alert fires, a red vertical line appears on the Redis Memory Usage graph at the exact time the alert triggered.

---

## Dashboard Permissions

### Role-Based Access

| Role | Permissions | Dashboards |
|------|-------------|------------|
| Admin | View, Edit, Create | All |
| Developer | View, Edit | All except Remediation |
| Operations | View, Edit | Infrastructure, Jobs, Remediation |
| Viewer | View only | Overview, Infrastructure, API Performance |

### Folder Organization
```
TradeX Monitoring/
├── Overview/
│   └── System Overview Dashboard
├── Infrastructure/
│   └── Infrastructure Monitoring Dashboard
├── Jobs/
│   └── Job Monitoring Dashboard
├── APIs/
│   ├── API Performance Dashboard
│   └── API Historical Analysis Dashboard
└── Automation/
    └── Remediation Dashboard
```

---

## Implementation Checklist

### Dashboard Creation
- [ ] Create dashboard JSON templates
- [ ] Import dashboards to Grafana
- [ ] Configure data sources
- [ ] Set up dashboard variables
- [ ] Configure alert rules
- [ ] Set up notification channels
- [ ] Configure dashboard permissions
- [ ] Create dashboard documentation
- [ ] Train team on dashboard usage

### Testing
- [ ] Verify all panels load data correctly
- [ ] Test dashboard variables and filters
- [ ] Validate alert annotations appear
- [ ] Test drill-down links
- [ ] Verify export functionality
- [ ] Test on different screen sizes
- [ ] Performance test with high data volume

---

## Next Steps

1. **Review mockups** with stakeholders
2. **Gather feedback** on layout and metrics
3. **Create dashboard JSON** templates
4. **Import to Grafana** staging environment
5. **Iterate based on** user testing
6. **Deploy to production** after approval

---

## Resources

- [Grafana Dashboard Best Practices](https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/best-practices/)
- [Grafana Panel Plugins](https://grafana.com/grafana/plugins/?type=panel)
- [PromQL Query Examples](https://prometheus.io/docs/prometheus/latest/querying/examples/)
- [Grafana Alerting](https://grafana.com/docs/grafana/latest/alerting/)
