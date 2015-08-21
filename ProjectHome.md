# News #
After completing prototype for ADC, I took some time to review architecture, study similar projects, etc.
As a result, I changed a lot of things. In fact, only most basic code and architecture remained, everything else has changed. So, It will take some time before project will be back in a "playable" state. I'll update source, code, description etc. then.

# Concept #

This is a graphical interface for playing tabletop and tabletop-alike games.
It is a lot like [XChess](https://gna.org/projects/xchess/) or [XBoard](http://www.tim-mann.org/xboard.html). But it is not limited to only chess. It is a set of tools, that allow to easily teach it to work with virtually any  game that can be played on a table with boards, pieces, cards, etc.

# Architecture basics #

The core of this application consist of two parts - game model and it's visual presentation.

GameModel is designed in a way, that will allow easily adjust it to different kinds of games and different kind of play styles (one player, several remote players, ai, game server, etc).

VisualEngine is designed to present current game state engine on a screen of Android phone and to interact with it. It represent game as a set of interactive game accessories - pieces, boards, card decks, dices. Simple XmlFormat is used to make creating new game environments easy.

# Demonstration #

[Tabletop.apk](http://android-tabletop.googlecode.com/files/Tabletop.apk) is a demo application, that demonstrates some of engine capabilities. You can choose chess of draughts game boards (see menu). Moves you do are stored to a file in real time, so you can exit application at any time and when you relaunch it you will have same position as when exited.

To move a piece, click on it, and then where you want to place it. You can take new pieces from palettes  atthe bottom of the screen and frop there unneeded pieces to remove them from game.

It also can be used for solving chess puzzless, like 8 Queens.

| ![http://android-tabletop.googlecode.com/files/position.png](http://android-tabletop.googlecode.com/files/position.png) | ![http://android-tabletop.googlecode.com/files/queens.png](http://android-tabletop.googlecode.com/files/queens.png) |
|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------|

# Further development #
Besides some usability and performance tweaks, following features are currently in work and are planned to be finished in immediate future:

  1. Interface for choosing file to store games, copying games, etc.
  1. Networking and multiplayer - it's a shame that it's not present already, but I spend too much time polishing other components. With that it will be finally a complete application, in addition to just a framework. It will be a P2P oriented model - no need for trusted server, just direct data interchange.
  1. Support for games with a chance and hidden information. Card decks, dices, that sort of thing. That will require implementation of fair play enforcing algorithms - Mental Poker, etc.
  1. Trans-application interface - it will allow controlling Tabletop fully from other applications. It can be used for making AI's, writing specialised network interfaces, etc.

# Licensing #
Demo and engine are licensed under GPL v3 license. Code is licensed under New BSD license as well.

**Knight piece in logo, chess pieces**

Created by Elmar Bartel, Tim Mann

**Draughts pieces**

Created by Arvindn and modified by Klin.

http://savannah.gnu.org/projects/xboard/

**"Tux" card set**

Copyright (C) 1998 Oliver Xymoron oxymoron@waste.org

http://www.waste.org/~oxymoron/cards/