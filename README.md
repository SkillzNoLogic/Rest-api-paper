# RestStatsAPI

A lightweight and configurable REST API plugin for Paper servers that exposes Minecraft player statistics through a simple JSON API.

RestStatsAPI allows server owners to create live leaderboards, websites, dashboards, Discord bots, and external tools by connecting Minecraft data directly to external applications.

Instead of creating custom integrations for every plugin, RestStatsAPI uses PlaceholderAPI to automatically retrieve and expose any placeholder value you configure.

## Features

- 🌐 **REST API**
  - Provides player data through HTTP endpoints in JSON format.
  - Designed for websites, dashboards, and external applications.

- 🧩 **PlaceholderAPI Integration**
  - Supports any PlaceholderAPI-compatible plugin.
  - Add new statistics without changing the plugin code.

- ⚙️ **Dynamic Configuration**
  - Define your own placeholders in `config.yml`.
  - The API automatically detects and displays configured values.

- 🔄 **Automatic Data Updates**
  - Player statistics are refreshed automatically at configurable intervals.
  - Keeps external applications synchronized with your server.

- 👥 **Player Database**
  - Tracks all players who have joined the server.
  - Supports offline player statistics where supported by placeholders.

- 🚀 **Lightweight Performance**
  - Minimal server impact.
  - Uses caching to reduce repeated placeholder calculations.
 
## Supported platforms

Pick the jar for your server from the [latest release](https://github.com/SkillzNoLogic/Rest-api-paper/releases/latest):

| Jar | Server | Minecraft | Java | Stats source |
|-----|--------|-----------|------|--------------|
| `RestStatsAPI-<ver>-paper-mc1.20-26.x.jar` | Paper (and forks) | 1.20 – 26.x | 17+ | PlaceholderAPI |
| `RestStatsAPI-<ver>-purpur-mc1.20-26.x.jar` | Purpur | 1.20 – 26.x | 17+ | PlaceholderAPI |
| `RestStatsAPI-<ver>-spigot-mc1.20-26.x.jar` | Spigot | 1.20 – 26.x | 17+ | PlaceholderAPI |
| `RestStatsAPI-<ver>-bukkit-mc1.8-26.x.jar` | **Any** Bukkit server (CraftBukkit, Spigot, Paper, Purpur, ...) | 1.8 – 26.x | 8+ | PlaceholderAPI |
| `RestStatsAPI-<ver>-fabric-mc1.20-26.x.jar` | Fabric (server-side only) | 1.20 – 26.x | 17+ | Vanilla statistics |
| `RestStatsAPI-<ver>-forge-mc1.20-26.x.jar` | Forge (server-side only) | 1.20 – 26.x | 17+ | Vanilla statistics |

### What's different per platform

- **All Bukkit builds** resolve placeholders a few players per tick, using no more than `tick-budget-ms` (2 ms by default) each tick. Large player lists therefore never cause a lag spike. The JSON is built once per refresh, so API requests are effectively free.
- **Paper / Purpur** also pause refreshing while the server's average tick time is above 45 ms (Paper API).
- **Spigot** targets the modern Spigot API (`api-version: 1.20`).
- **Universal Bukkit** is Java 8 bytecode built against the 1.8.8 API. It runs on anything Bukkit-based. Use it for old servers or uncommon forks.
- **Fabric / Forge** have no PlaceholderAPI. Instead they serve vanilla statistics, read from the world's stats files on a background thread, so the server thread is never touched. Only files that changed since the last refresh are re-read. Minecraft writes these files on autosave and when a player logs out. The mods don't reference any Minecraft classes, so one jar covers 1.20 – 26.x. They're only needed on the server, not on clients.

## Installation

**Bukkit / Spigot / Paper / Purpur**
1. Put the jar in `plugins/` and install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) plus the expansions you want.
2. Restart the server, then edit `plugins/RestStatsAPI/config.yml`.

**Fabric / Forge**
1. Put the jar in `mods/` on the server.
2. Restart the server, then edit `config/reststatsapi.json`.

## API Endpoints

- `GET /`: status, e.g. `{"status":"online","plugin":"RestStatsAPI","platform":"paper"}`
- `GET /players`: all cached player statistics

## Configuration

Bukkit platforms (`config.yml`):

```yaml
server:
  host: 0.0.0.0
  port: 8080
update-interval: 30      # seconds between refreshes
tick-budget-ms: 2.0      # max main-thread time per tick spent on placeholders
placeholders:
  rank: "%rankedsmp_rank%"
  kills: "%statistic_player_kills%"
```

Fabric / Forge (`config/reststatsapi.json`). Stats are written as `category/stat`, and the `minecraft:` prefix is optional:

```json
{
  "host": "0.0.0.0",
  "port": 8080,
  "updateInterval": 30,
  "stats": {
    "kills": "custom/player_kills",
    "deaths": "custom/deaths",
    "play_time_ticks": "custom/play_time",
    "diamonds_mined": "mined/diamond_ore"
  }
}
```

## Building

```
./gradlew releaseJars    # every platform jar -> build/release/
```

Modules: `core` (HTTP server + JSON), `bukkit-common`, `vanilla` (stats reader), and one module per platform.

## Example Uses

RestStatsAPI can power:

- Minecraft server websites
- Live leaderboards
- Player profile pages
- Discord server bots
- Community dashboards
- Custom server applications
- Tournament/stat tracking systems

## Example API Response

```json
[
  {
    "uuid": "2205c335-c33d-48a3-b811-3a9859fbc09c",
    "name": "Skillz_No_Logic_",
    "stats": {
      "rank": "Diamond",
      "kills": "52",
      "deaths": "14"
    }
  }
]
```
