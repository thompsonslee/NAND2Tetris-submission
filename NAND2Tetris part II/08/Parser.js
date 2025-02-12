const arithmeticCommands = [
    'add',
    'sub',
    'neg',
    'eq',
    'gt',
    'lt',
    'and',
    'or',
    'not'
]
const memAccessCommands = [
    'push',
    'pop'
]
const memorySegments = [
    'local',
    'argument',
    'this',
    'that',
    'constant',
    'static',
    'pointer',
    'temp'
]
const branchingCommands = [
    'goto',
    'if-goto',
    'label'
]
const functionCommands = [
    'call',
    'function',
    'return'
]
const deLint = (vmFile) => {
    let text = vmFile.split(/\n/)
    const removeSpaces = () => {
        text = text.map((line) => {
            const charArray = line.split("")
            return charArray.filter((char,index) => {
                if(char != " ") return true
                if(index === 0) return
                if(charArray[index + 1] === "/") return
                if(charArray[index -1] === " " || charArray[index+1] === " ") return
                return true
            }).map(char => char === " " ? "," : char).join("")
        })
    }
    const removeLineBreaks = () => {
        if(!text) throw new Error('no text')
        text = text.map((line) => {
            if(!line) return line
            return line.replace(/(\r\n|\n|\r|\t)/gm, "")
            
        })
    }
    const removeComments = () => {
        const textNoComments = text.map((line) => {
            const charArray = line.split('')
            const firstSlashIndex = charArray.indexOf('/')
            if(firstSlashIndex != -1 && charArray[firstSlashIndex +1] === '/'){
                return charArray.slice(0,firstSlashIndex).join('')
            }

            return line
        })
        text = textNoComments
    }
    const removeEmptyLines = () => {
        text = text.filter(line => line.length)
    }
    removeSpaces()
    removeLineBreaks()
    removeComments()
    removeEmptyLines()
    return text.map(line => line.split(','))
}

export default class Parser {
    constructor(vmFile){
        this.text = deLint(vmFile)
        this.currentIndex = 0
        this.hasMoreLines = this.text.length === 1 ? false : true 
    }

    next(){
        if(!this.hasMoreLines) return;

        this.currentIndex ++
        
        if(this.currentIndex === this.text.length - 1){
            this.hasMoreLines = false
        }
    }
    setIndex(int){
        this.currentIndex = int
    }
    getCommandType(){
        if(arithmeticCommands.indexOf(this.text[this.currentIndex][0]) != -1){
            if(this.text[this.currentIndex].length != 1){
                throw new Error(`arguments given after Arithmetic commands '${this.text[this.currentIndex][0]}' at line: ${this.currentIndex}. full line: ${this.text[this.currentIndex]}`)
            }
            return 'Arithmetic'
        }
        else if(memAccessCommands.indexOf(this.text[this.currentIndex][0]) != -1){
            return 'MemoryAccess'
        }
        else if(branchingCommands.indexOf(this.text[this.currentIndex][0]) != -1){
            return 'Branching'
        }
        else if(functionCommands.indexOf(this.text[this.currentIndex][0]) != -1){
            return 'Function'
        }
        else{
            throw new Error(`unknow command type: ${this.text[this.currentIndex] [0]} at line: ${this.currentIndex}`)
        }

    }
    getCommand(){
        if(!this.text[this.currentIndex].length){
            throw new Error(`line length is 0 at line: ${this.currentIndex}`)
        }
        return this.text[this.currentIndex][0]
    }
    getSegment(){
        if(this.getCommandType() != 'MemoryAccess'){
            throw new Error(`trying to get segment but command type is not MemoryAccess at line: ${this.currentIndex}`)
        }
        if(memorySegments.indexOf(this.text[this.currentIndex][1]) === -1) {
            throw new Error(`${this.text[this.currentIndex][1]} is not a memory segment at line ${this.currentIndex}`)
        }
        return this.text[this.currentIndex][1]
    }
    getSegmentIndex(){
        if(this.getCommandType() != 'MemoryAccess'){
            throw new Error(`trying to get segment but command type is not MemoryAccess at line: ${this.currentIndex}`)
        }
        if(parseInt(this.text[this.currentIndex][2]) === NaN){
            throw new Error(`expected number but got ${this.text[this.currentIndex][2]} at line: ${this.currentIndex}`)
        }
        return this.text[this.currentIndex][2]
    }
    getFullCommand(){
        return this.text[this.currentIndex]
    }
    getLabel(){
        if(this.getCommandType() != 'Branching'){
            throw new Error(`attempting to get label from non Branching command type at line: ${this.currentIndex}`)
        }
        const label = this.text[this.currentIndex][1]
        if(!label){
            throw new Error(`no label found for branching command at line ${this.currentIndex}`)
        } 
        return label 
    }
    //returns a tuple, where [0] is the function name, and [1] is either the functions var number if function definition, or arg number if function call
    getFunctionDetails(){
        if(this.getCommandType() != 'Function'){
            throw new Error(`attempting to get function details from non Function command type at line: ${this.currentIndex}`)
        }
        return [this.text[this.currentIndex][1],this.text[this.currentIndex][2]]
    }
}