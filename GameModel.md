# Introduction #

Tabletop Android game model is designed in a spirit of microkernel.

It has very simple core, and everything else is built on top of it.

# Kernel #

There are only two entities at a heart of model - Note and Link;

**Note** is a string and it's unique integer Id. String and an integer;

**Link** is an oriented connection between two Notes. Two integer - ids of first and second notes; First note in a link is called "sup", second - "sub". Usually link denotes relations like "Sub is part of Sup" or "Sub is an instance of set Sup". We say that "Sub" note is linked **to** by this link, and "Sup" note is linked **from**. _Hmm, probably I should just replace sub and sup with from and to in db..._

Each game is a set of notes and links between them. Each move is  changing of some notes, and adding or removing some links.

Such simple model leads to very simple interfaces.

This is a main interface that is used in engine:

```
public interface PartyListener {
	void user(long id);
	void begin(long time);	
	void write(long id, String val);
	void delete(long id);
	void link(long sup, long sub);
	void unlink(long sup, long sub);
	void end();
}
```

See PartyListener description for details.

Format of sqlite database is also very simple - onle two tables

```
CREATE TABLE links (_id INTEGER PRIMARY KEY, sup INTEGER, sub INTEGER);

CREATE TABLE notes (_id INTEGER PRIMARY KEY, value STRING);
```

Everything else are indexes, views and triggers.

See DbPresentation for details

# Extended Game Model #
Game model that is used in XmlFormat and in gui presentation is different. It is  similar to usual xml - it has a elements (called Attributes) and attributes of elements (called Attributes). Each element has a type, each attribute has a name and value. Element can has a sequence of sub-elements.

Extended game model is built upon the kernel.

Also there are entities in Extended Game Model that is not yet implemented - User and Visibility.

# Details #

So, how is game vuilt on top of that? It is done in a following way.

There are a set of negative note ids with special meaning. They are declared in Partyconstants.java interface.

```
final static int SYSTEM = -1, NAME = -2, OPTION = -3, USER = -4,
VISIBILITY = -5, ATTRIBUTE = -6, ACCESSORY = -7, ROOT = -8;
```

They are used to mark a "type" of note. If some note is linked from one of this negative pseudo-notes, then it means that it has type that is marked with this note.

  * NAME - notes, that are names of other notes. It is used mainly for names of attributes - even if there are hundreds of attributes with name "width" in game, they all are linked from one note, linked from NAME.

  * ATTRIBUTE - notes, that are values of attributes.
  * ACCESSORY - notes, that represent accessories. They have a value, equal to it's type (picture, grid, deck, etc.). More important, all of it's attributes are linked from it.

Accessory note is linked from it's parent note and ACCESSORY pseudo-note. It is linked to all it's attributes and child accessories.

Attribute note is linked from it's parent accessory, name note, and ATTRIBUTE note.


  * ROOT - root accessory of game
  * OPTION, SYSTEM - notes, that are not directly relevant to game state, but hold some utility information

  * USER - note, representing some player. It's value should contain information about sending and receiving information from him - net address, encryption key, etc. It can be linked to some notes with extra information about him - name, photo, etc.

If some note or link is changed by some user on his instance of game state, then it should broadcast Update (see PartyListener) to all other users, containing this change.

  * VISIBILITY - visibility class. If some note is linked from visibility note, than it should be seen only by users, that is linked to same visibility note. All other users doesn't receive information about changes of this note, and link from or to this note.

DbPresentation reflects some ideas of this model, so you can see it as well.