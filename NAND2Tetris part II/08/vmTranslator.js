import fs from 'fs'
import path from 'path'
import Parser from './Parser.js'
import getArithmeticCommands from './ArithmeticCommands.js'
import getMemoryCommand from './MemoryCommands.js'
import getBranchingCommand from './BranchingCommands.js'
import getFunctionCommand from './FunctionCommands.js'
import bootStrap from './bootstrap.js'

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
            return getMemoryCommand(this.parser.getCommand(),this.parser.getSegment(),this.parser.getSegmentIndex(),this.parser.currentIndex,this.fileName)
        }
        const writeArithmetic = () => {
            return getArithmeticCommands(this.parser.currentIndex)[this.parser.getCommand()]
        }
        const writeBranching = () => {
            return getBranchingCommand(this.parser.getCommand(), this.parser.getLabel())
        }
        const writeFunction = () => {
            const [functionName, argVar] = this.parser.getFunctionDetails()
            return getFunctionCommand(this.parser.getCommand(),functionName,argVar,this.parser.currentIndex,this.fileName)  
        }
        while(true){
            this.asmFile.push(`//${this.parser.getFullCommand()}`)
            if(this.parser.getCommandType() === 'Arithmetic'){  
                this.asmFile = this.asmFile.concat(writeArithmetic())
            }
            else if(this.parser.getCommandType() === 'MemoryAccess'){
                this.asmFile = this.asmFile.concat(writeMemoryAccess())
            }
            else if(this.parser.getCommandType() === 'Branching'){
                this.asmFile = this.asmFile.concat(writeBranching())
            }
            else if(this.parser.getCommandType() === 'Function'){
                this.asmFile = this.asmFile.concat(writeFunction())
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
let vmFilesPaths = [];
let asmFileName
const vmFile = fs.statSync(process.argv[2],"utf-8",(err, data) => {
    if(err){
        throw new Error(err)
    }
})
if(vmFile.isDirectory()){
    asmFileName = path.basename(process.argv[2])
    const files = fs.readdirSync(process.argv[2],"utf-8",(err, data) => {
        if(err){
            throw new Error(err)
        }
    }).filter((file) => path.extname(file) === ".vm")
    console.log(files)
    if(!files.length) throw new Error("no vmFiles within this directory")
    vmFilesPaths = files.map((file) => `${process.argv[2]}${file}`)
    
}
else if(vmFile.isFile()){
    const baseName = path.basename(process.argv[2])
    asmFileName = baseName.slice(0,baseName.length - 3)
    vmFilesPaths.push(process.argv[2])
}

else throw new Error("argument is neither file or directory")
const asmFile = bootStrap + vmFilesPaths.map((filePath) => {

    const parser = new Parser(fs.readFileSync(filePath,"utf-8", (err) => {
        throw new Error(err)
    }))
    const baseName = path.basename(filePath)
    console.log(`baseName:${baseName}`)
    const fileName = baseName.slice(0,baseName.length - 3)
    const asmWriter = new AsmWriter(fileName,parser)
    asmWriter.write()
    console.log(asmWriter.asmFile)
    return asmWriter.asmFile
}).join('')


fs.appendFile(`${asmFileName}.asm`,asmFile,(err) => {
    if(err){
        throw new Error(err)
    }
    console.log(`created ${asmFileName}.asm`)
})
