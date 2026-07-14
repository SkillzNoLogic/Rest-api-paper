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
 
  Requirements
Paper 1.21+
Java 25+
PlaceholderAPI
API Endpoints
Get all players
GET /players

Returns all cached player statistics.

Example:

https://your-server.com/players
Installation
Download the latest RestStatsAPI.jar.
Place it inside your server's plugins folder.
Install PlaceholderAPI.
Configure your placeholders.
Restart your server.
Development

Built with:

Java
Paper API
PlaceholderAPI
Gson

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




YAML

server:
  host: 0.0.0.0
  port: 8080

update-interval: 30

placeholders:
  rank: "%rankedsmp_rank%"
  kills: "%rankedsmp_kills%"
  deaths: "%rankedsmp_deaths%"
