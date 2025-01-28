//local
//argument
//this
//that


export default (memoryCommand,segment,segmentIndex,lineNum,fileName) => {
    
    if([memoryCommand,segment,segmentIndex,lineNum,fileName].includes(undefined)) throw new Error(
        `one or more arguments for memoryCommands function are undefined.
        memoryCommand args: ${memoryCommand},${segment},${segmentIndex},${lineNum},${fileName}`
    )
    const argsMapping = {
        local: 'LCL',
        argument: 'ARG',
        this: 'THIS',
        that: 'THAT'
    }
    if(memoryCommand === 'push'){
        if(['local','argument','this','that'].includes(segment)){
            return [
                `@${segmentIndex}`,
                'D=A',
                `@${argsMapping[segment]}`,
                'A=D+M',
                'D=M',
                '@SP',
                'A=M',
                'M=D',
                '@SP',
                'M=M+1'
            ]
        }
        else if(segment === 'constant'){
            return [
                `@${segmentIndex}`,
                'D=A',
                '@SP',
                'A=M',
                'M=D',
                '@SP',
                'M=M+1'
            ]
        }
        else if(segment === 'static'){
            return [
                `@${fileName}.${segmentIndex}`,
                'D=M',
                '@SP',
                'A=M',
                'M=D',
                '@SP',
                'M=M+1'
            ]
        }
        else if (segment === 'temp'){
            if(segmentIndex > 7) throw new Error(`temp can only have a segmentIndex of <= 7 at lineNum: ${lineNum}` )
            return[
                `@${5 + parseInt(segmentIndex)}`,
                'D=M',
                '@SP',
                'A=M',
                'M=D',
                '@SP',
                'M=M+1'
            ]
        }
        else if (segment === 'pointer'){
            if(segmentIndex  >= 2) throw new Error(`segmentIndex for memoryCommand 'pointer' can only be 0 or 1 at lineNum: ${lineNum}`);
            
            return [
                `@${3 + parseInt(segmentIndex)}`,
                'D=M',
                '@SP',
                'A=M',
                'M=D',
                '@SP',
                'M=M+1'
            ]
        }
        else throw new Error(`unrecognized memoryCommand: ${memoryCommand}`)
    }
    
    else{ //memoryCommand === 'pop'
        if(['local','argument','this','that'].includes(segment)){
            return [
                `@${segmentIndex}`,
                'D=A',
                `@${argsMapping[segment]}`,
                'D=D+M',
                '@R13',
                'M=D',
                '@SP',
                'M=M-1',
                'A=M',
                'D=M',
                'M=0',
                '@R13',
                'A=M',
                'M=D',
            ]
        }
        else if(segment === 'constant'){
            throw new Error(`cannot pull from constant at lineNum: ${lineNum}`)
        }
        else if(segment === 'static'){
            return [
                '@SP',
                'M=M-1',
                'A=M',
                'D=M',
                'M=0',
                `@${fileName}.${segmentIndex}`,
                'M=D',
            ]
        }
        else if(segment === 'temp'){
            return[
                '@SP',
                'M=M-1',
                'A=M',
                'D=M',
                'M=0',
                `@${5 + parseInt(segmentIndex)}`,
                'M=D'
            ]
        }
        else if (segment === 'pointer'){
            if(segmentIndex  >= 2) throw new Error(`segmentIndex for memoryCommand 'pointer' can only be 0 or 1 at lineNum: ${lineNum}`);

            return [
                '@SP',
                'M=M-1',
                'A=M',
                'D=M',
                'M=0',
                `@${3 + parseInt(segmentIndex)}`,
                'M=D',
            ]
        }
    }

    
}