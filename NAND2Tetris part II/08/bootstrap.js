import getFunctionCommand from "./FunctionCommands.js"

// should be added to the start of the final .asm file
const setSP = (
    `
    @256
    D=A
    @SP
    M=D`
)
const sysInitCall = getFunctionCommand("call","Sys.init",0,0)

export default setSP + sysInitCall

