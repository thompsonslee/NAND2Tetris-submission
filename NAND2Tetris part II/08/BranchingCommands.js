//goto

//if-goto

//label

export default(branchingCommand, labelName) => {
    if(!branchingCommand) throw new Error ('no branchingCommand given')
    if(!labelName) throw new Error('no labelName given')
    if(branchingCommand === 'goto'){
        return(
            `@${labelName}
            0,JMP`
        )
    }
    else if(branchingCommand === 'if-goto'){
        return(
            `@SP
            M=M-1
            A=M
            D=M
            M=0
            @${labelName}
            D;JNE`
        ) 
    }
    else if(branchingCommand === 'label'){
        return(
            `(${labelName})`
        )
    }
    else throw new Error('invalid branching command')
}