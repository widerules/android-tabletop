## Summary ##

Interface `com.baturinsky.tabletop.service.PartyListener` is interface, used to describe
game moves, or a whole game.

http://code.google.com/p/android-tabletop/source/browse/trunk/src/com/baturinsky/tabletop/service/PartyListener.java

## Declaration ##

```
public interface PartyListener {
	void user(long id);
	void begin(long time);	
	void write(long id, String val);
	void delete(long id);
	void link(long sup, long sub);
	void unlink(long sup, long sub);
	void end();

```

It is a main interface in engine. Currently it is used for logging, storing into db, serialization in and from string.

## Commands ##

It's commands do following:

_void user(long id);_ - declares id of player that do following changes. Not used currently.

_void begin(long time);_ - starts a transaction (one move, for example, or a real transation when writing into db) and marks time of it. Time is used for conflicts of concurrent moves.

_void write(long id, String val);_ - changes value of notes and makes new note if it does not exist.

_void delete(long id);_ - removes note from the game.

_void link(long sup, long sub);_ - links two notes.

_void unlink(long sup, long sub);_ - removes link.

_void end();_ - finishes move or transaction.

## Implementations ##

`com.baturinsky.tabletop.service.PartyListenerLog`

http://code.google.com/p/android-tabletop/source/browse/trunk/src/com/baturinsky/tabletop/service/PartyListenerLog.java

Just logs commands.

`com.baturinsky.tabletop.service.Update`

http://code.google.com/p/android-tabletop/source/browse/trunk/src/com/baturinsky/tabletop/service/Update.java

This class can store commands into a string, and then deserialize them back to commands.
It's main use is interface between applications and sending moves to the remote client.

`com.baturinsky.tabletop.service.DbListener`

http://code.google.com/p/android-tabletop/source/browse/trunk/src/com/baturinsky/tabletop/service/DbListener.java

This class alters game state, stored in a database, according to commands.

If you store your move into an Update class, send resulting string to remote client, create Update instance from this string there, and then call method "apply" of this class with a `DbListener` as argument, then it will alter db-stored game state according to your commands.

## Current Uses ##
In demo `DbListener` class is used to store game and game updates to database in real time.