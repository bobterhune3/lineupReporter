# LineupReporter

Java 17 Spring Boot web UI for Strat-O-Matic lineup and roster reports. Port of the LineupEngine2 C# library.

This project ported A C# desktop UI and two C# libraries that I had previously written and used cursor AI to port it to a Java Spring Boot with javascript UI.

## Requirements

- Java 17+
- Maven 3.6+

## Build

```bash
mvn clean package
```

## Run

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/LineupReporter-1.0.0-SNAPSHOT.jar
```

Then open http://localhost:8080

## Usage

1. **Upload a roster report**  
   Create a Strat-O-Matic League Roster Report (Team → Display Reports → Roster Report → Each Team → Print to File). Upload the `.prt` (or `.txt`) file on the home page.

2. **View teams**  
   After loading, the list of teams appears. Click a team to see pitchers, batters, and balance (L/R IP by balance type).

3. **Balance report**  
   Use the Balance Report page to pick a team and see pitcher balance counts (vs L / vs R) by balance level (9L … E … 9R).

## Configuration

Optional `config.properties` in the working directory:

- `ESTIMATE_AB_MULTIPLIER` – AB multiplier (default 1.0)
- `ESTIMATE_IP_MULTIPLIER` – IP multiplier (default 1.0)
- `AB_MINIMUM` – minimum AB (default 50)
- `IP_MINIMUM` – minimum IP (default 30)
- Team name → abbreviation mappings: `FULL TEAM NAME=ABB`

## Data

Lineup and team info are stored under `lineupreporter-data/` (by default in the working directory). Override with:

```properties
lineupreporter.data-dir=/path/to/data
```

## Project structure

- `domain/` – Team, Player, Defense, Game, LineupData, LineupBalanceItem, TeamLineup, TeamInfo
- `config/` – AppConfig (loads config.properties, implements IConfig for SOM reports)
- `service/` – SomTeamReportFile (report parser), LineupEngine, persistence, calculators
- `util/` – TeamUtils (team abbreviation mapping), RecordIndex (ID generators), ReportUtil (rounding)
- `somreport/` – **Ported from somReportUtils (C#)**  
  - Report, TeamReport, ComparisonReport, PrimaryStatsReport  
  - DataStore, IConfig, ISOMReportFile, PlayerSortingUtil  
  - output: SOMRecord, IOutput, NoOpOutput  
  Use these to parse SOM comparison/primary stats reports and compute usage/targets; wire your own ISOMReportFile implementation to read report files.
- `web/` – HomeController, ReportController
- `templates/` – Thymeleaf (index, team, balance)
- `static/css/` – Styles
