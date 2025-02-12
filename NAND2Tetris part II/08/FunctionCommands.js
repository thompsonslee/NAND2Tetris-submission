export default (functionType, functionName, argVar, lineNum, fileName) => {
    //not sure if we need to append the filename or not. doesnt seem line it.

    if(!functionType) throw new Error('no functionType provided')

    if(functionType === 'call'){
        if(!functionName) throw new Error(`no function name given for ${functionType}`)
        const pushReturnAddr = (
            `
            @${functionName}.$ret.${lineNum}
            D=A
            @SP
            A=M
            M=D
            @SP
            M=M+1`
        )
        const pushLCL = (
            `
            @LCL
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1`
        )
        const pushARG = (
            `
            @ARG
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1`
        )
        const pushTHIS = (
            `
            @THIS
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1`
        )
        const pushTHAT = (
            `
            @THAT
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1`           
        )
        const setARG = (
            `
            @SP
            D=M
            @5
            D=D-A
            @${argVar}
            D=D-A
            @ARG
            M=D`
        )
        const setLCL = (
            `
            @SP
            D=M
            @LCL
            M=D`
        )
        const JMPtoFunc = (
            `
            @${functionName}
            0;JMP`
        )
        const returnLabel = `
        (${functionName}.$ret.${lineNum})`

        return pushReturnAddr + pushLCL + pushARG + pushTHIS + pushTHAT + setARG + setLCL + JMPtoFunc + returnLabel
        //push functionName$ret.lineNum
        //push lineNum to stack (this will be the return address) 
        //push LCL to stack
        //push ARG to stack
        //push THIS to stack
        //push THAT to stack

        //set ARG to be SP - 5 - argVar
        //set LCL to SP

        //jump to function


        //(functionName$ret.lineNum)
    }
    else if(functionType === 'function'){
        if(!functionName) throw new Error(`no function name given for ${functionType}`)
        //create label (functionName)
        //for each argVar, set LCL memory segments to 0
        
    return (
        `(${functionName})
        @${argVar}
        D=A
        @R13
        M=D     //i = vars
        (LOOP_${functionName}${lineNum})
        @R13
        D=M
        @END_${functionName}${lineNum}
        D;JEQ  //if i = 0, JEQ
        @SP
        A=M
        M=0
        @SP
        M=M+1 // push 0 to stack
        @R13
        M=M-1
        @LOOP_${functionName}${lineNum}
        0;JMP
        (END_${functionName}${lineNum})`
    )       
    }
    else if(functionType === 'return'){
        const storeEndFrame = (
            `
            @LCL
            D=M
            @R13
            M=D`
        )
        const storeRETADDR = (
            `
            @5
            D=-A
            @R13
            A=D+M
            D=M
            @R14
            M=D`
        )
        const setReturnValue = (
            `
            @SP
            A=M-1
            D=M
            @ARG
            A=M
            M=D`
        )
        const setSP = (
            `
            @ARG
            D=M+1
            @SP
            M=D`
        )
        const setSegmentAddr = (segment, index) => {
            return(
                `
                @${index}
                D=-A
                @R13
                A=D+M
                D=M
                @${segment}
                M=D`
            )
        }
        const gotoRetAddr = (
            `
            @R14
            A=M
            0;JMP`
        )
        //store temp variable ENDFRAME = LCL
        //RETADDR = *(ENDFRAME -5)
        //SET *ARG to what is on top of the stack (the return value)
        //SET SP to ARG + 1

        //SET THAT  = *(ENDFRAME -1)
        //SET THIS  = *(ENDFRAME -2)
        //SET ARG   = *(ENDFRAME -3)
        //SET LOCAL = *(ENDFRAME -4)
        return (
            storeEndFrame   +
            storeRETADDR    + 
            setReturnValue  + 
            setSP           +
            setSegmentAddr('THAT', 1) +
            setSegmentAddr('THIS', 2) +
            setSegmentAddr('ARG',  3) +
            setSegmentAddr('LCL',  4) +
            gotoRetAddr
        )
    }
    else throw new Error(`invalid function type. received: ${functionType} as functiontype`)
}