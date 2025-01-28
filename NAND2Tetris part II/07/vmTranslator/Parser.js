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

const deLint = (vmFile) => {
    let text = vmFile.split(/\n/)
    const removeSpaces = () => {
        text = text.map((line) => {
            let charArr = line.split('')
            const firstChar = charArr.findIndex((char) => char != ' ')
            const lastChar = charArr.toReversed().findIndex((char) => char != ' ')
            
            charArr = charArr.slice(firstChar,charArr.length - lastChar)
            while(true){
                const index = charArr.indexOf(' ')
                if (index != -1){
                    charArr[index] = ',' 
                }
                else break
            }
            return charArr.join('')
        })
    }
    const removeLineBreaks = () => {
        text = text.map((line) => {
            return line.replace(/(\r\n|\n|\r)/gm, "")
            
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
        this.hasMoreLines = true
    }

    next(){
        if(!this.hasMoreLines) return;

        this.currentIndex ++
        
        if(this.currentIndex === this.text.length - 1){
            this.hasMoreLines = false
        }
    }
    getCommandType(){
        if(arithmeticCommands.indexOf(this.text[this.currentIndex][0]) != -1){
            if(this.text[this.currentIndex].length != 1){
                throw new Error(`arguments given after memory Access Argument '${this.text[this.currentIndex[0]]}' at line: ${this.currentIndex}`)
            }
            return 'Arithmetic'
        }
        else if(memAccessCommands.indexOf(this.text[this.currentIndex][0] != -1)){
            return 'MemoryAccess'
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
}