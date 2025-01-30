# Minecraft Role Chaos Survival - 1.21-1.0

This plugin implements a game mode where each player receives a unique role and must eliminate all other roles at least once to win. The game takes place in a defined area, with random respawn mechanics and random inventories.

## How it Works

The goal of the game for each player is to eliminate all available roles, including the one assigned at the start of the game. Each role is unique and cannot be occupied by two players simultaneously. Every time a player dies, their role is randomly redistributed among the remaining roles. Upon respawning, the player randomly receives a role from those available. At certain intervals, a chest containing a random item appears for all players, offering different items for each player. Additionally, at another interval, the minimum map height increases, and players who are below this threshold or respawn below it take damage.

To play this game, you need at least two players, and the number of roles must be one more than the number of players.

I haven't tested the plugin with cracked Minecraft accounts, so it may not work as intended.

## Features

- **Random Roles:** At the beginning of each game, each player receives a unique role from a customizable list of roles.
- **Random Respawn:** When a player dies, they respawn with a random role from those available at the time of their death.
- **Defined Game Area:** Players are teleported into a defined area of the map, surrounded by an invisible barrier.
- **Vertical Limit:** The minimum vertical game limit increases during the game, forcing players to climb higher.
- **Random Item Inventories:** Periodically, an inventory with a random item opens for everyone. Each player can pick a random item.

- **Interface Display:**
  - **Tablist:** Displays the player Y-level, the other players' roles and the lowest Y-level before taking damage.
  - **Bossbar:** Displays the remaining time before the next inventory opens.
  - **Experience:** Displays the player's progress.

- **Disconnect Protection:** Players who disconnect are reset to avoid abuse.

## Installation

1. Download the `.jar` file from GitHub in the `build` folder.
2. Place it in your Minecraft server's `plugins` folder.
3. Restart or reload the server.
4. Configure the `config.yml`.
5. Restart or reload the server.
6. Set the map boundaries using the `/setmap <x, z, length>` command.
7. Use `/startgame` to start a game and `/stopgame` to stop it.

## Commands

- `/startgame`: Starts a new game.
- `/stopgame`: Stops the current game.
- `/info <role|list>`: Displays role descriptions or the list of roles. Example: `/info Warrior`.
- `/setmap <x, z, length> | <reset>`: Defines the game area by specifying the center coordinates `<x> <z>` and the length of one side `<length>` (must be between 5 and 320 blocks). You can also reset the game area with `/setmap reset`.

## Configuration

You can customize the description, name, items, and effects of each role in the plugin's `config.yml`.  
You can specify the quantity of items using the format `<Item>:<Quantity>`. You can also specify the effect level using the format `<Effect>:<Amplifier>`.
Here is an example of the role structure :
```
roles:
  example:
    description: Role example
    armor:
      helmet: IRON_HELMET
      chestplate: IRON_CHESTPLATE
      leggings: ''
      boots: ''
    items:
    - IRON_SWORD:1
    - GOLDEN_APPLE:5
    effects:
    - SPEED:1
```

You can customize the content and interval for the inventory appearance in the plugin's `config.yml`.  
The interval is in seconds. You can specify the quantity of items using the format `<Item>:<Quantity>`
Here is an example of the inventory structure :
```
inventory:
  interval: 300
  items:
  - DIRT:64
```

You can customize the interval and maximum height for the minimum level system in the plugin's `config.yml`.  
The interval is in seconds (minimum 15s), and the maximum height is `y=320`.
Here is an example of the minimum level system structure :
```
void:
  interval: 30
  max: 150
```

You can customize the death messages in the plugin's `config.yml`.  
Here is an example of the death message structure :
```
death_message:
  suicide: died by himself
  messages:
  - was killed by
```

## About Me & Contributions

This project is a learning experience for me as I am self-taught and do not have a formal degree in computer science. I’m open to feedback, suggestions, and (please) constructive criticism to help me improve my skills. Since I am a native French speaker, please be understanding if there are any issues with my English. Feel free to contribute or offer advice!

## Authors

- **Epsilonae** - Creator of the plugin
