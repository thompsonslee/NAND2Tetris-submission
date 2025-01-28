import fs from 'fs'
import path from 'path'
import Parser from './Parser.js'
import getArithmeticCommands from './ArithmeticCommands.js'
import getMemoryCommand from './MemoryCommands.js'

if(process.argv.length != 3){
    throw new Error(`expected process.arvg.length to be 3 but was ${process.argv.length}`)
}

class AsmWriter {
    constructor(fileName,parser){
        this.fileName = fileName
        this.parser = parser
        this.asmFile = []
    }
    write(){
        const writeMemoryAccess = () => {
            console.log(`command:${this.parser.getCommand()}`)
            return getMemoryCommand(this.parser.getCommand(),this.parser.getSegment(),this.parser.getSegmentIndex(),this.parser.currentIndex,this.fileName)
        }
        const writeArithmetic = () => {
            return getArithmeticCommands(this.parser.currentIndex)[this.parser.getCommand()]
        }
        while(true){
            console.log(`currentIndex: ${this.parser.currentIndex}`)
            console.log(`command: ${this.parser.getFullCommand()}`)
            this.asmFile.push(`//${this.parser.getFullCommand()}`)
            if(this.parser.getCommandType() === 'Arithmetic'){  
                console.log(`arithmeticCommand: ${writeArithmetic()}`)
                this.asmFile = this.asmFile.concat(writeArithmetic())
            }
            else if(this.parser.getCommandType() === 'MemoryAccess'){
                console.log(`memoryCommand:${writeMemoryAccess()}`)
                this.asmFile = this.asmFile.concat(writeMemoryAccess())
            }
            if(!this.parser.hasMoreLines) break;
            this.parser.next()
        }
        this.asmFile = this.asmFile.map((line) => {
            const newLine = line.split('')
            newLine.push('\n')
            return newLine.join('')
        }).join('')
    }
}
const vmFile = fs.readFileSync(process.argv[2],"utf-8",(err, data) => {
    if(err){
        throw new Error(err)
    }
})
const parser = new Parser(vmFile)
const file = path.basename(process.argv[2])
const extension = path.extname(process.argv[2])
const fileName = file.slice(0,file.length - extension.length)

const asmWriter = new AsmWriter(fileName,parser);
asmWriter.write();

console.log(asmWriter.asmFile)



fs.appendFile(`${fileName}.asm`,asmWriter.asmFile,(err) => {
    if(err){
        console.log(err.message)
        return
    }
    console.log(`created ${fileName}.asm`)
})
