# Metrics/Statistics

- data is fetched by an hourly rate
- if an API endpoint provides 15-minute data (ENTSO-E)[https://transparency.entsoe.eu/], these values are aggregated as follows:
    - $x_{hour} = \frac{x_1 + x_2 + x_3 + x_4}{4}$
    - (applies to generation load, and renewable production)

## Operational Metrics

- describe the state of the power grid

1. Generation Mix
- shows how electricity is produced over time
- chart: stack area, x: time, y: MW, series: production types
- api: ENTSO-E, 
    - endpoint: actual generation per production type

2. Total Load vs Generation
- detect supply/demand imbalances
- chart: line chart (2 lines: load and total generation) 
- api: ENSTO-E
    - endpoint: actual total load & actual generation per production type
    - derived metric: total generation = sum(all production types)

3. Renewable Share
- 
- chart: line chart (% renewable)
- api: ENTSO-E
    - endpoint: actual generation per production type
- formula: renewables = solar + wind + hydro + biomass
           renewable share = renewables / total generation

4. Carbon Intensity 
-
- chart: line chart (gCO₂/kWh)
- api: [Electricity Maps](https://app.electricitymaps.com/developer-hub/api)
    - endpoint: carbon-intensity

5. Cross-Border Electricity Flows 
- shows dependency between countries
- chart: Map (with directional flow arrows)
- api: Electricity Maps
    - endpoint: power-flows
- formula: imports = sum(all incoming flows)
           exports = sum(all outgoing flows)

---

## Derived Operational Metrics

6. Net Load
-
- chart: line chart
- api: ENTSO-E
    - endpoint: not needed
- formula: net load = total load - renewable generation

7. Import Dependency 
- shows energy security risk
- chart: line chart
- api: Electricity Maps
    - endpoint: power-flows or exchange
- formula: imports / total load
           imports = sum(all incoming flows)
           import dependency = imports / total load

---

## Correlation Metrics

Meteorological events (wind & temperature) are calculated country-wide. This is done by computing an average from different
locations - namely: Hamburg, Berlin, Munich, Cologne, Frankfurt (i.e. north, east, south, west, center). Wind speed is taken 
from 100m altitude, since turbines roughly operate in 80-120m. 

8. Renewable Generation vs Meteorological Events 
-
- chart: Scatter plot 
    - wind: 
        - x: avg_wind_speed_100m
        - y: wind_generation
    - solar:
        - x: shortwave_radiation
        - y: solar_generation
- api: ENSTO-E, OpenWeather/Open-Meteo
    - endpoint: ?

9. Renewable Share vs Carbon Intensity
-
- chart: scatter plot 
    - x: renewable %
    - y: carbon intensity
- api: ENTSO-E & Electricity Maps
    - endpoint: ?

10. Imports vs Carbon Intensity
- shows when imports increase emissions
- chart: scatter plot
    - x: imports
    - y: carbon_intensity
- api: Electricity Maps
    - endpoint: ?

11. Load vs Temperature
- electricity demand strongly correlates with temperature
- chart: scatter plot (x: temperature, y: load)
    - x: avg_temperature
    - y: total_load
- api: ENTSO-E & Open-Meteo



