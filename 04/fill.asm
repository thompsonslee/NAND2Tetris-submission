@SCREENCOLOR
M=0

(LOOP)
	
	@KBD
	D=M
	@SETWHITE
	D;JEQ
	(RETURN0)

	@SETBLACK
	D;JNE
	(RETURN1)

	@SCREEN					//8
	D=A					//9
	@8192					//10
	D=D+A					//11
	@i					//12
	M=D //set i to SCREEN+8192		//13
	
	(SCREENLOOP)
		@i				//14
		D=M				//15
		@SCREEN				//16
		D=A-D				//17
		@LOOP
		D;JGT //jump to loop if i-screen > 0
		
		@SCREENCOLOR
		D=M
		@i
		A=M
		M=D //set SCREEN[i] to @screencolor

		@i
		M=M-1
		@SCREENLOOP
		0;JMP


(SETWHITE)
@SCREENCOLOR
M=0
@RETURN0
0;JMP

(SETBLACK)
@SCREENCOLOR
M=-1
@RETURN1
0;JMP