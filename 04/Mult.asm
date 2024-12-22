@R0
D=M
@i
M=D //i = R0

@R1
D=M
@num
M=D //num=R1



(LOOP)
	@i
	D=M
	@FINISH
	D;JEQ //if i = 0, finish loop

	@num
	D=M
	@accum
	M=D+M

	@i
	M=M-1

	D=M

@LOOP
0;JMP

(FINISH)
@accum
D=M
@R2
M=D
@END
(END)
0;JMP



