# RestStatsAPI

A lightweight REST API for Minecraft servers that exposes player statistics as JSON.

Use it to build live leaderboards, server websites, player profile pages, Discord bots and dashboards. You don't need custom integrations for every plugin: list the stats you want in a config file, and RestStatsAPI serves them over HTTP.

Runs on **Paper, Purpur, Spigot, any Bukkit server, Fabric and Forge**, from Minecraft **1.20 to 26.x** (and 1.8+ with the universal Bukkit jar).

**[⬇ Download the latest release](https://github.com/SkillzNoLogic/Rest-api-paper/releases/latest)**

---

## Features

- 🌐 **Simple JSON API**: one HTTP request returns every player and their stats. CORS is enabled, so browsers can call it directly.
- 🧩 **PlaceholderAPI support** (plugin builds): expose any placeholder from any plugin, with no code changes.
- 📊 **Vanilla statistics** (Fabric and Forge builds): kills, deaths, play time, blocks mined, items crafted, and any other vanilla stat.
- 👥 **Offline players included**: everyone who has joined the server is listed, not just who is online right now.
- 🔄 **Automatic refresh**: stats update on a configurable interval.
- 🚀 **No lag**: placeholder work is spread across server ticks, the mod builds read files on a background thread, and the JSON is built once per refresh, so API requests are effectively free.

## Which jar do I need?

| Your server | Download | Minecraft | Java | Stats come from |
|-------------|----------|-----------|------|-----------------|
| Paper (or a Paper fork) | `RestStatsAPI-<ver>-paper-mc1.20-26.x.jar` | 1.20 – 26.x | 17+ | PlaceholderAPI |
| Purpur | `RestStatsAPI-<ver>-purpur-mc1.20-26.x.jar` | 1.20 – 26.x | 17+ | PlaceholderAPI |
| Spigot | `RestStatsAPI-<ver>-spigot-mc1.20-26.x.jar` | 1.20 – 26.x | 17+ | PlaceholderAPI |
| Anything Bukkit-based, including old versions | `RestStatsAPI-<ver>-bukkit-mc1.8-26.x.jar` | 1.8 – 26.x | 8+ | PlaceholderAPI |
| Fabric | `RestStatsAPI-<ver>-fabric-mc1.20-26.x.jar` | 1.20 – 26.x | 17+ | Vanilla statistics |
| Forge | `RestStatsAPI-<ver>-forge-mc1.20-26.x.jar` | 1.20 – 26.x | 17+ | Vanilla statistics |

Not sure? Use the jar named after your server software. If your server is very old or an unusual fork, the **bukkit** jar runs on anything Bukkit-based.

## Installation

### Paper / Purpur / Spigot / Bukkit

1. Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) and the expansions for the stats you want (for example `/papi ecloud download Statistic`).
2. Put the RestStatsAPI jar in your `plugins/` folder.
3. Start the server once, then edit `plugins/RestStatsAPI/config.yml`.
4. Restart the server.

### Fabric / Forge

1. Put the RestStatsAPI jar in your server's `mods/` folder. Fabric API is **not** required, and players do **not** need the mod.
2. Start the server once, then edit `config/reststatsapi.json`.
3. Restart the server.

Then open `http://<your-server-ip>:8080/players` in a browser.

## API

| Method | Path | Returns |
|--------|------|---------|
| `GET` | `/players` | Every known player and their configured stats |
| `GET` | `/` | Status check: `{"status":"online","plugin":"RestStatsAPI","platform":"paper"}` |

### Example response: `GET /players`

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

All stat values are strings. `name` is left out if the server has never recorded the player's name.

### Using it from a website

```js
const players = await fetch("http://your-server.com:8080/players").then(r => r.json());
const top = players.sort((a, b) => b.stats.kills - a.stats.kills).slice(0, 10);
```

## Configuration

### Plugin builds: `plugins/RestStatsAPI/config.yml`

```yaml
server:
  host: 0.0.0.0   # address to listen on (use 127.0.0.1 if behind a reverse proxy)
  port: 8080

# How often player stats are refreshed, in seconds (minimum 1).
refresh-interval: 30

# Print a console message every time stats are refreshed (true/false).
console-messages: true

# Max time (milliseconds) spent resolving placeholders per server tick. A refresh is
# spread across as many ticks as needed, so large player lists never cause a lag spike.
tick-budget-ms: 2.0

# Output name -> PlaceholderAPI placeholder
placeholders:
  rank: "%rankedsmp_rank%"
  kills: "%statistic_player_kills%"
  deaths: "%statistic_deaths%"
```

Any placeholder works. If an expansion isn't installed, the raw placeholder text is returned instead, e.g. `"%rankedsmp_rank%"`.

After editing the file, run `/reststats reload`. You don't need to restart.

> Upgrading from 1.2.x? The old `update-interval` key (`updateInterval` for the mods) still works. The mods rewrite it to the new name automatically, and the plugins switch to the new name the next time you change the interval with a command.

#### Commands

`/reststats` (alias `/rsa`) needs the `reststatsapi.admin` permission (ops have it by default). It also works from the server console.

| Command | What it does |
|---------|--------------|
| `/reststats` or `/reststats status` | Shows the API address, cached player count, refresh interval and whether console messages are on |
| `/reststats interval <seconds>` | Changes how often stats refresh. Takes effect immediately and is saved to `config.yml` |
| `/reststats messages <on\|off\|toggle>` | Turns the per-refresh console messages on or off. Saved to `config.yml` |
| `/reststats refresh` | Refreshes stats right now |
| `/reststats reload` | Reloads `config.yml`. The web server only restarts if `host` or `port` changed |

With console messages on, each refresh logs a line like:

```
[RestStatsAPI] Refreshed stats for 42 players (3.1ms server time over 2 ticks). Next refresh in 30s.
```

### Mod builds: `config/reststatsapi.json`

```json
{
  "host": "0.0.0.0",
  "port": 8080,
  "refreshInterval": 30,
  "consoleMessages": true,
  "stats": {
    "kills": "custom/player_kills",
    "mob_kills": "custom/mob_kills",
    "deaths": "custom/deaths",
    "play_time_ticks": "custom/play_time",
    "jumps": "custom/jump",
    "diamonds_mined": "mined/diamond_ore"
  }
}
```

- `refreshInterval`: how often stats are refreshed, in seconds (minimum 1).
- `consoleMessages`: whether to print a console line on every refresh (`true`/`false`).

**Changes apply automatically.** The mod checks the file every second, so you can save your edits and they take effect within a second or two. No restart or command is needed, and the console confirms with `Reloaded config/reststatsapi.json`. If you make a typo and the JSON is invalid, the previous settings stay active and a warning is logged.

The mod builds have no in-game command. Adding one would need version-specific Minecraft code, and the single jar that covers 1.20 to 26.x would no longer be possible.

Stats are written as `category/stat`, using the names from the in-game Statistics screen and the [Minecraft Wiki](https://minecraft.wiki/w/Statistics). The `minecraft:` prefix is optional, so `custom/jump` is the same as `minecraft:custom/minecraft:jump`. Modded stats work too, e.g. `mined/create:zinc_ore`.

| Category | Meaning | Example |
|----------|---------|---------|
| `custom` | General stats | `custom/player_kills`, `custom/deaths`, `custom/play_time` (in ticks, 20 per second) |
| `mined` | Blocks mined | `mined/diamond_ore` |
| `crafted` | Items crafted | `crafted/diamond_sword` |
| `used` | Items used | `used/ender_pearl` |
| `broken` | Tools broken | `broken/diamond_pickaxe` |
| `picked_up` / `dropped` | Items picked up or dropped | `picked_up/diamond` |
| `killed` / `killed_by` | Mobs killed, or killed by | `killed/zombie` |

A stat the player doesn't have yet is returned as `"0"`.

## How each build is optimised

- **All plugin builds** resolve placeholders a few players per tick. Each tick spends at most `tick-budget-ms` on this, instead of handling every player at once. The JSON is built once per refresh and reused for every request, and the web server runs on its own small thread pool.
- **Paper / Purpur** also pause refreshing while the server is struggling, meaning its average tick time is above 45 ms. This uses Paper's tick-time API.
- **Spigot** is built against the modern Spigot API (`api-version: 1.20`).
- **Universal Bukkit** is built for Java 8 against the 1.8.8 API, so it loads on every Bukkit server from 1.8 to 26.x.
- **Fabric / Forge** never run on the server thread. They read the world's stats files on a low-priority background thread, and only re-read files that changed since the last refresh. They use no Minecraft code, which is why one jar covers every version from 1.20 to 26.x. They handle both the old (`world/stats`) and the 26.x (`world/players/stats`) folder layouts.

## Good to know

- **The API has no authentication.** Anyone who can reach the port can read the stats. To restrict access, firewall the port, or set `host` to `127.0.0.1` and put it behind a reverse proxy such as nginx or Caddy.
- **Hosting panels:** many hosts only open specific ports. Set `port` to one your host has given you.
- **Fabric / Forge freshness:** Minecraft writes a player's stats to disk on autosave (every 5 minutes by default) and when they log out. Online players' numbers can therefore lag behind by up to one autosave.
- **Port already in use:** if the log says the API could not start, another program is using that port. Change `port`.

## Building from source

Requires JDK 25.

```bash
./gradlew releaseJars        # every platform jar -> build/release/
./gradlew :paper:runServer   # start a Paper test server with the plugin installed
```

| Module | Contents |
|--------|----------|
| `core` | HTTP server and JSON writer, with no dependencies (Java 8) |
| `bukkit-common` | Shared plugin logic: tick-budgeted refresh, PlaceholderAPI lookups |
| `vanilla` | Stats-file reader and config shared by Fabric and Forge |
| `paper`, `purpur`, `spigot`, `bukkit` | Plugin entrypoints, each built against its own platform API |
| `fabric`, `forge` | Mod entrypoints |
