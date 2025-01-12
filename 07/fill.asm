(MAIN)
	@px
	M=0
	@KBD
	D=M
	@SKIP
		D;JEQ //skip changing to black if 0
		@px
		M=-1
	(SKIP)
	
	@24576 //8192 + 16384
	D=A
	@i
	M=D
	(LOOP)
		@i
		D=M
		@SCREEN
		D=D-A
		@MAIN
		D;JLT //exit loop if i=0

		@px
		D=M
		@i
		A=M
		M=D //RAM[i] = px

		@i
		M=M-1
		@LOOP
		0;JMP

	
	
	
	