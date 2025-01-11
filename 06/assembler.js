const destTable = {
    "null"  : "000",
    "M"     : "001",
    "D"     : "010",
    "MD"    : "011",
    "A"     : "100",
    "AM"    : "101",
    "AD"    : "110",
    "AMD"   : "111"
}
const jumpTable = {
    "null": "000",
    "JGT" : "001",
    "JEQ" : "010",
    "JGE" : "011",
    "JLT" : "100",
    "JNE" : "101",
    "JLE" : "110",
    "JMP" : "111"
}
const compTable = {
    "0"     : "0101010",
    "1"     : "0111111",
    "-1"    : "0111010",
    "D"     : "0001100",
    "A"     : "0110000",
    "M"     : "1110000",
    "!D"    : "0001111",
    "!A"    : "0110011",
    "!M"    : "1110001",
    "-D"    : "0001111",
    "-A"    : "0110011",
    "-M"    : "1110011",
    "D+1"   : "0011111",
    "A+1"   : "0110111",
    "M+1"   : "1110111",
    "D-1"   : "0001110",
    "A-1"   : "0110010",
    "M-1"   : "1110010",
    "D+A"   : "0000010",
    "D+M"   : "1000010",
    "D-A"   : "0010011",
    "D-M"   : "1010011",
    "A-D"   : "0000111",
    "M-D"   : "1000111",
    "D&A"   : "0000000",
    "D&M"   : "1000000",
    "D|A"   : "0010101",
    "D|M"   : "1010101"
}

class AsmFile{
    constructor(text) {
        this.text = text.split(/\n/)
    }
    removeSpaces(){
        this.text = this.text.map((line) => {
            const charArr = line.split('')

            return charArr.filter((char) => char != ' ').join('')
        })

    }
    removeLineBreaks(){
        this.text = this.text.map((line) => {
            return line.replace(/(\r\n|\n|\r)/gm, "")
            
        })
    }
    removeComments(){
        const textNoComments = this.text.map((line) => {
            const charArray = line.split('')
            const firstSlashIndex = charArray.indexOf('/')
            if(firstSlashIndex != -1 && charArray[firstSlashIndex +1] === '/'){
                return charArray.slice(0,firstSlashIndex).join('')
            }

            return line
        })
        this.text = textNoComments
    }
    removeEmptyLines(){
        this.text = this.text.filter(line => line.length)
    }
    firstPass(){
        const symbolTable = {
            "R0": 0,
            "R1": 1,
            "R2": 2,
            "R3": 3,
            "R4": 4,
            "R5": 5,
            "R6": 6,
            "R7": 7,
            "R8": 8,
            "R9": 9,
            "R10": 10,
            "R11": 11,
            "R12": 12,
            "R13": 13,
            "R14": 14,
            "R15": 15,
            "SCREEN": 16384,
            "KBD": 24576,
            "SP": 0,
            "LCL": 1,
            "ARG": 2,
            "THIS": 3,
            "THAT": 4,
        }

        const getLabel = (line) => {
            const charArr = line.split('')
            if(charArr[0] === '(' && charArr[charArr.length - 1] === ')'){
                return charArr.slice(1,-1).join('')
            }
            return
        }

        let variableAddr = 16
        for(let i = 0; i < this.text.length ;){
            const line = this.text[i]
            const label = getLabel(line)
            if(label){
                symbolTable[label] = i
                this.text.splice(i,1)
                if(getLabel(this.text[i])) continue;
                i++
            }
            i++
        };

        this.text = this.text.map((line) => {
            const charArr = line.split('')
            const symbol = charArr.slice(1).join('')
            if(charArr[0] === '@' && isNaN(symbol)){
                if(symbolTable.hasOwnProperty(symbol)){
                    return(`@${symbolTable[symbol]}`)
                }
                symbolTable[symbol] = variableAddr
                variableAddr++;
                
                return(`@${variableAddr - 1}`)

            }
            return line
        })

    }
    main(){
        const aParser = (line,lineNum) => {
            const num = parseInt(line.split('').slice(1).join(''))
            const binaryArray = num.toString(2).split('')
            while(binaryArray.length != 16){
                binaryArray.unshift('0')
            }
            return binaryArray.join('')
        }
        const cParser = (line,lineNum) => {
            const charArr = line.split('')
            const equalsIndex = charArr.indexOf('=')
            const scIndex = charArr.indexOf(';')

            const compInstruction = charArr.slice(equalsIndex != -1 ? equalsIndex + 1 : 0, scIndex != -1 ? scIndex : charArr.length).join('')
            const compBin = compTable[compInstruction]

            if(!compBin) throw new Error(`failed to get compBin for line ${lineNum}: ${line}`)
            
            let destBin;
            if(equalsIndex === -1){
                destBin = '000'
            }
            else{

                const descCommand = charArr.slice(0,equalsIndex).join('');
                if(!destTable[descCommand]){
                    throw new Error(`invalid destination param at line ${lineNum}: ${line}`)
                }
                destBin = destTable[descCommand]
            }

            let jumpBin;
            if(scIndex === -1){
                jumpBin = '000'
            }else{
                const jmpCommand = charArr.slice(scIndex + 1).join('')
                if(!jumpTable[jmpCommand]) throw new Error(`invalid jump command at line ${lineNum}`)
                jumpBin = jumpTable[jmpCommand]
            }

            return '111' + compBin + destBin + jumpBin
        }

        this.text = this.text.map((line,lineNum) => {
            let newText = ''
            if(line.split('')[0] === '@'){
                newText = aParser(line,lineNum)
            }
            else(
                newText = cParser(line,lineNum)
            )
            newText = newText.split('')
            newText.push('\n')
            return newText.join('')
        }).join('')

    }
    createBin = () => {
        this.removeSpaces()
        this.removeComments()
        this.removeLineBreaks()
        this.removeEmptyLines()
        this.firstPass()
        this.main()
    }


}

const fs = require('fs')
const path = require('path')

if(process.argv.length != 3){
    throw new Error(`expected process.arvg.length to be 3 but was ${process.argv.length}`)
}
const asmFile = new AsmFile(fs.readFileSync(process.argv[2],"utf-8",(err, data) => {
    if(err){
        throw new Error(err)
    }
}))


asmFile.createBin()

const file = path.basename(process.argv[2])
const extension = path.extname(process.argv[2])
const fileName = file.slice(0,file.length - extension.length)
fs.appendFile(`${fileName}.hack`,asmFile.text,(err) => {
    if(err){
        console.log(err.message)
        return
    }
    console.log(`created ${fileName}.hack`)
})



