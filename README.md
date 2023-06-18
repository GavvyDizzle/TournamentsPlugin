# TournamentsPlugin
A flexible tournament solution for your Spigot server!

### Features
- Allows for many different types of tournaments to suit your needs
- Full GUI support and customization
- Display the results of past tournaments
- Supports multiple active tournaments
- An easy-to-understand tournament scheduling system
- Hooks into GangsPlugin and ShopGUI+

### Requirements
- This plugin has one dependency: ServerUtils
- It has soft dependencies of GangsPlugin, PlaceholderAPI, PrisonEnchants (private), and ShopGUI+
- Currently only supports saving with SQLite

### Tournaments
Example tournaments can be found in the `/example_configs` directory

#### Creating Tournaments
- Each tournament gets its own `.yml` file inside the `plugins/TournamentsPlugin/tournaments` folder
- Tournaments can be scored individually or by gang
- Specify the start and end date down to the minute (time zone can be changed in `config.yml`)
- Specify an objective which tells the tournament what to count

#### Objectives
- Here are the different types of things you can make your tournaments score
- `CRAFT` Crafting all items or a certain item
- `EAT` Eating items
- `EARN` Earning money through ShopGUI+
- `FISH` Catching items by fishing
- `KILL` Kill mobs (or players)
- `MINE` Mined blocks and enchant broken blocks (counts blocks through PrisonEnchants)
- `MINE_RAW` Manually mined blocks (you want to use this for normal mining)
- `SELL_ITEM` Sell items through ShopGUI+
- `SMELT` Smelt items (muse be manually removed from the furnace result slot)
- If you specify the `objective.material` field, the tournament will only count when that material is involved in the objective
  - Use `objective.entityType` when defining a `KILL` objective
- Omitting the material or entityType field will cause the tournament to count **ALL** actions involved in the objective

#### Rewards
- Tournaments give out rewards to certain players the moment the tournament ends
- Since players can be offline when this happens, you should ensure all commands have offline player support
- Each reward has the following settings
  - Specify the placement(s) that get the reward (setting min/max to the same number will make only that placement receive it)
  - Specify commands that run for this player. Use the placeholder `{player_name}` for the player's name
- Reward placement ranges are allowed to overlap. It is up to you to ensure the rewards are correct

#### GUI
- Tournaments have 3 states
  - `pending` When the tournament has not started yet
  - `active` When the tournament is ongoing
  - `completed` When the tournament has finished
- These states specify what menus the tournaments will appear in when using the `/tourn` command
- You can customize these items for each tournament. The following placeholders exist for these items ONLY
  - `{tournament_name}` The name of the tournament
  - `{start_date}` The start date of the tournament
  - `{end_date}` The end date of the tournament
  - `{pretty_start_date}` The pretty start date of the tournament (format specified with `prettyDateFormat` in `config.yml`)
  - `{pretty_end_date}` The pretty end date of the tournament
  - `{time_until_start}` Time until the tournament starts
  - `{time_until_end}` Time until the tournament ends

#### Rewards GUI
- Each tournament also gets a rewards menu that players can open
  - There are settings for each tournament to block opening this menu depending on the time states
- This menu is just a way to display the rewards to your players within the plugin

#### Leaderboard
- The tournament leaderboard displays the top x players where 1 <= x <= 54
  - This item will always be the player's skull
- You can also display an item which shows the player their position
  - This will show their position if they are not within the top x players
- Customize these items with the following placeholders:
  - `{name}` The name of the player or gang
  - `{placement}` The number of this player's placement
  - `{total_placements}` The total number of players or gangs in the tournament
  - `{score}` The player's score as a formatted number

### Commands
- Player Command (GUI Command)
    - `/tourn (tournament, tournaments)` Opens the tournaments main menu
    - Permission: `tournamentsplugin.menu`
- Admin Commands
    - The base permission is `tournamentsplugin.admin`
    - All commands require permission to use which follows the format `tournamentsplugin.admin.command` where command is the name of the command
    - `/tournadmin debug` Toggles objective debugging
    - `/tournadmin delete <id>` Completely delete a tournament
    - `/tournadmin help` Lists all commands
    - `/tournadmin regenerateTable <id>` Regenerates a tournament's data table in the database (effectively wipes data)
    - `/tournadmin reload [arg]` Reloads the plugin
    - `/tournadmin reloadTournament <id>` Reloads a single tournament from its config file
    - `/tournadmin setScore <id> <player> <score>` Set the score of a tournament participant
    - `/tournadmin testReward <id> <placement>` Give yourself the rewards for a tournament (for testing purposes)

### Notes
- Changing the tournament `id` in its config file **will cause unintended behavior** if the tournament has been started (specifically if a database has been created for it)
- If you want to delete a tournament, do so using the `/tournadmin delete <id>` command. This will delete the config file and associated database if one exists.
- Technically, a completed tournament can be "started" again. A tournament can be run by changing the end time. This will obviously retain the existing scores and give rewards upon completion.

