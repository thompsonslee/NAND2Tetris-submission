10x20 blocks

classes:
	render: 
		handles rendering to the screen by being given state of game
	board:
		provides board data and methods to move currently falling block
		data includes block type and coordinates of block
		can generate new shape at top of screen
		needs coordinates for all blocks that are falling
		needs the ability to rotate these blocks
	shape:
		can be one of five shapes
		contains data about the current falling shape such as coordinates for its blocks.
		provides board with coordinates of its blocks
		has methods for rotating block
	Input:
		provides user input
	gameController:
		handles overall game logic using renderer, board and listener
		handles stuff like game speed, game state (isGameOver?)
	main:
		initializes gameController
