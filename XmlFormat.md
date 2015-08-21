## Example ##
Let's start with example of chess board:

```

<?xml version="1.0" encoding="utf-8"?>
<props id="chess">
	<zip zip_file="raw:chess_pieces" width="40" height="40" anchor_x="20" anchor_y="20"/>
	<picture name="chess_board" file="raw:chessboard" width="320" height="320" aa="false"/>	
	<position name="initial">
		<deck name="whites" x="36" y="330" presentation="line" 
			cards="white_queen;white_king;white_bishop;white_knight;white_rook;white_pawn" 
			static="true" child_copied="true" child_hole="true"
		/>
		<deck name="blacks" x="36" y="380" presentation="line" 
			cards="black_queen;black_king;black_bishop;black_knight;black_rook;black_pawn"
			static="true" child_copied="true" child_hole="true" hole="true"
		/>
		<copy of="chess_board" static="true" z="-1"/>
		<grid name="chessboard_grid" 
			x="-20" y="300" 
			d_x="40,0,0" d_y="0,-40,0" 
			width="320" height="320"
			anchor_x="-20" anchor_y="-20"
			child_replaced="true"
			static="true"
		>
			<line range_x="1~8" range_y="1"
				line="white_rook;white_knight;white_bishop;white_queen;white_king;white_bishop;white_knight;white_rook"/>
			<line range_x="1~8" range_y="2"
				line="white_pawn*8"/>
			<line range_x="1~8" range_y="7"
				line="black_rook;black_knight;black_bishop;black_queen;black_king;black_bishop;black_knight;black_rook"/>
			<line range_x="1~8" range_y="8"
				line="black_pawn*8"/>
		</grid>
	</position>
</props>

```

## Syntax ##

```
<picture name="chess_board" file="raw:chessboard" width="320" height="320" aa="false"/>
```

  * Picture is a bitmap, that can be used, for example, to draw game pieces.

  * **name** - name of element, that can be used to reference to it. All game elements (engine call them "accesories") can have it.

  * **file** - name of image file. It can be user file, file in "raw" directory in apk (then it should start with "raw:" prefix", or a file inside of zip - then it must be written in form of zip\_name#file\_inside\_zip

  * **width** , **height** - size of picture. If it differs from size of bitmap, then it will be stretched or shrinked accordingly.

  * **aa** - flag of antialiasing.

  * **anchor\_x**, **anchor\_y** - in usual meaning, i.e. picture will be drawn shifted by that values. Default anchor is in top left corner. As you see, chess\_board uses default anchor.

```
<zip zip_file="raw:chess_pieces" width="40" height="40" anchor_x="20" anchor_y="20"/>
```

  * If you don't want to specify each picture in zip by hand, you can use this construction. It will parse all images in zip and register them by their names (without extention).

```
<position name="initial"> ... 
```
  * Description of specific position and screen layout. This one describes starting chess position. If engine is said to load "chess.xml#initial" template, then it will load "chess.xml" file and set "initial" position.

```
<deck name="whites" x="36" y="330" presentation="line" cards="white_queen;white_king;white_bishop;white_knight;white_rook;white_pawn" static="true" child_copied="true" child_hole="true"/> 
```

  * Deck is a sequence of "cards". Card can be a picture, or two pictures - front and back.
  * **cards** is a list of initialcards in deck in Range format.
  * **static** means that deack itself is not active - it can't be selected, moved, etc. That is not extended on cards in it.
  * **child\_copied** **child\_hole** - if you want to set some default attribute for all children of element, you can do it with a `*child_*` prefix. **copied** flag means that card can't be taken from deck, but instead will produce a copy of itself. **hole** means that all pieces that is dropped on it will be removed from the game.
  * **presentation** - deack can be shown in form of line, stack or fan. Currently only line presentation is fully functional.

```
<copy of="chess_board" static="true" z="-1"/>
```

  * This element means that engine should take some element from elsewhere (path is in "of" attribute), create a copy of it and, alter some attributes and place it to where it is. So, this one takes chess board picture, places it in default position and makes it **static** - not active.

```
		<grid name="chessboard_grid" 
			x="-20" y="300" 
			d_x="40,0,0" d_y="0,-40,0" 
			width="320" height="320"
			anchor_x="-20" anchor_y="-20"
			child_replaced="true"
			static="true"
		>
```

  * Very interesting element. Used for game boards. It changes coordinate system inside of it.
  * **d\_x** describes new "x" vector and **d\_y** - new "y" vector. Also, each piece inside of grid should have an integer coordinates (i.e. placed in the center of some cell). So, this particular board has square cells with side of 40, y grows when moving down-up (as it is customary for chess notation).
  * **replaced** means that each piece inside it will be removed, when other piece is placed on it. this grid sets this flag for all it's shildren.

```
 <line range_x="1~8" range_y="2" line="white_pawn*8"/>
```

  * "line" is a macro for placing a line of pieces. "range\_x" and "range\_y" are ranges of coordinates - single number for all, or two -  for first and last piece, with ~ between. As you see, they use grid coordinates, inside of screen coordinates, because they are inside of grid.

  * **line** attribute is a sequence of pieces in a Range form - same as in deck.

## Range ##

And about that "Range" format in lines and decks;

It is a little complicated, but basically it's names of pieces, delimited by ";".
If you have a sequence of equivalent elements, then you can write them as "name\*number"
Also, you can place a sequence of elements in alphabetic order, in format of "first\_element~last\_element". For example, this is a list of 52 playing cards, aces high, followed by two jokers: "02c~13s;01c~01s;j\*2".

## Name lookup ##
Element can reference to it's child, or child of any of it's parents by just name.
It can reference other elements in a form of path, starting from child of it or it's parent, delimited by dots.

For example: "initial.chessboard\_grid" - for elements not inside "initial" element, or just "chessboard\_grid" for elements inside "initial".

In case of several item's parent having same named child, then child of lower parent has priority. If some element has children wih same name, then first can be referenced by name, second as "name#2", third as "name#3", etc.

## Draughts ##
Now you will understand a config file for draughts too:

```

<?xml version="1.0" encoding="utf-8"?>
<props id="chess">
	<picture name="chess_board" file="raw:chessboard" width="320" height="320"/>	
	
	<picture name="white" file="raw:dr_white" anchor_x="22" anchor_y="22"/>
	<picture name="white2" file="raw:dr_white2" anchor_x="22" anchor_y="22"/>
	<picture name="black" file="raw:dr_black" anchor_x="22" anchor_y="22"/>
	<picture name="black2" file="raw:dr_black" anchor_x="22" anchor_y="22"/>
	
	<position name="draughts_initial">
		
		<deck name="all_types" x="72" y="330" presentation="line" 
			cards="white;white2;black;black2" 
			static="true" child_copied="true" child_hole="true" hole="true"
			child_width="44" child_height="44"
		/>
		
		<copy of="chess_board" static="true" z="-1"/>
		<grid name="chessboard_grid" 
			x="-20" y="300" 
			d_x="40,0,0" d_y="0,-40,0" 
			width="320" height="320"
			anchor_x="-20" anchor_y="-20"
			child_replaced="true"
			static="true"
		>
			<line range_x="2~8" range_y="8"
				line="black*4"/>
			<line range_x="1~7" range_y="6"
				line="black*4"/>
			<line range_x="2~8" range_y="7"
				line="black*4"/>

			<line range_x="1~7" range_y="3"
				line="white*4"/>
			<line range_x="2~8" range_y="2"
				line="white*4"/>
			<line range_x="1~7" range_y="1"
				line="white*4"/>

		</grid>
	</position>
</props> 

```

## Cards ##

Config file that describes some standard card decks (card images are in card\_images.zip file, named according to PySol notation)

```
<?xml version="1.0" encoding="utf-8"?>
<props xmlns="http://com.baturinsky.tabletop" id="cards">
	<zip file="raw:card_images" width="73" height="97"/>
	<deck id="32" face="07c~13s;01c~01s" back="back1"/>
	<deck id="36" face="06c~13s;01c~01s" back="back1"/>
	<deck id="52" face="02c~13s;01c~01s" back="back1"/>
	<deck id="all" face="02c~13s;01c~01s;j*2" back="back1"/>
	<position id="game1">
		<deck id="5cards" x="100" y="100">
			<copy of="cards.all.01c"/>
			<copy of="cards.all.02c"/>
			<copy of="cards.all.03c"/>
			<copy of="cards.all.04c"/>
		</deck>
	</position>	
</props>
```