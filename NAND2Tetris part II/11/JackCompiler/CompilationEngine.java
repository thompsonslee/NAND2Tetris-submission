package JackCompiler;

public class CompilationEngine extends Parser{
	
	public CompilationEngine(Tokenizer tokenizer, VMWriter vmWriter){
		super(tokenizer,vmWriter);
	}
	
	public String CompileClass(){

		String jackClass = "";
		
		tokenizer.advance();
		
		expectType("keyword");
		expect("class");
		
		tokenizer.advance();

		expectType("identifier");
		this.vmWriter.className = tokenizer.getToken(); //class name
	
		tokenizer.advance();

		expectType("symbol");
		expect("{");
		
		tokenizer.advance();

		handleClassVarDecs();

		String comment = "// Compiled " + this.vmWriter.className + ".jack:\n";
		jackClass += comment;
		jackClass += unindentLabels(handleSubroutineDecs());;
		
		expect("}");

		return jackClass;
	}


	private String handleSubroutineDecs(){
		String subRoutineDecs = "";
		while(isFuncDecType(tokenizer.getToken())){
			subRoutineDecs += handleSubrountieDec();
		}

		return subRoutineDecs;
	}

	private String handleSubrountieDec(){
		expectType("keyword");
		if(!isFuncDecType(tokenizer.getToken())){
			throw new Error("expected constructor,function or method token but got: " + tokenizer.getToken());
		}

		vmWriter.subLevel.clear();
		
		String subroutineDecType =  tokenizer.getToken();

		String returnType = "";

		tokenizer.advance();

		if(tokenizer.getToken().equals("void")){
			returnType = "void";
		}
		else{
			returnType += handleType();
		}
		
		tokenizer.advance();
		
		expectType("identifier");
		String functionName = vmWriter.className + "." + tokenizer.getToken();

		vmWriter.functionName = functionName;

		tokenizer.advance();

		expectType("symbol");
		expect("(");
		
		tokenizer.advance();

		writeParamsToTable();
		expect(")");

		tokenizer.advance();
		
		String functionTypeHandling = handleFuncTypeHandling(subroutineDecType); 
		String subroutineBody = handleSubroutineBody();


		String subroutineDec = "function " + functionName + " " + vmWriter.subLevel.getCount("local") + "\n";
		subroutineDec += functionTypeHandling;
		subroutineDec += subroutineBody;
		return subroutineDec;
	}


	/**
	* returns the vm code needed to handle either constructors, methods, or functions. this vm code needs to be the first vm
	* code at the top of the vm function.
	*/
	private String handleFuncTypeHandling(String token){
		String funcHandle;
		if(token.equals("constructor")){
			int fieldCount = vmWriter.classLevel.getCount("field");
			funcHandle =	
				"push constant " + fieldCount + "\n" +
				"call Memory.alloc 1\n" +
				"pop pointer 0\n";
			
			
		}
		else if(token.equals("method")){
			vmWriter.subLevel.setIsMethod();
			funcHandle =	
				"push argument 0\n" +
				"pop pointer 0\n";
		}
		else if(token.equals("function")){
			return "";
		}
		else{
			throw new Error("expected function declaration type, but got: " + token);
		}
		return indent(funcHandle);
	};

	private boolean isFuncDecType(String token){
		return(
			token.equals("constructor") ||
			token.equals("function") ||
			token.equals("method")
		);
	}

	private String handleSubroutineBody(){
		expect("{");

		tokenizer.advance();
		if(tokenizer.getToken().equals("var")){
			writeVarsToTable();
		}
		
		StatementHandler statements = new StatementHandler(tokenizer,vmWriter);
		
		
		String body = statements.handleStatements();

		tokenizer.advance();
		
		return indent(body);
	}


	private void writeVarsToTable(){
		expect("var");
		
		while(tokenizer.getToken().equals("var")){
			writeVarToTable();
			tokenizer.advance();
		}
	}

	private void writeVarToTable(){
		expect("var");

		tokenizer.advance();
		String type = handleType();
		tokenizer.advance();

		expectType("identifier");
		String name = tokenizer.getToken();
		tokenizer.advance();

		vmWriter.subLevel.createVar(name, type, "local", "subroutinelevel");

		while(!tokenizer.getToken().equals(";")){
			expect(",");
			tokenizer.advance();
			expectType("identifier");
			name = tokenizer.getToken();
			tokenizer.advance();
			vmWriter.subLevel.createVar(name, type, "local", "subroutinelevel");
		}
		expect(";");
	}

	private void writeParamsToTable(){
		if(!tokenizer.getToken().equals(")")){
			String type = handleType();
			tokenizer.advance();
			expectType("identifier");
			String name = tokenizer.getToken();
			tokenizer.advance();
			vmWriter.subLevel.createVar(name,type,"argument","subroutinelevel");

		}
		while(!tokenizer.getToken().equals(")")){
			expect(",");
			tokenizer.advance();
			String type = handleType();
			tokenizer.advance();
			expectType("identifier");
			String name = tokenizer.getToken();
			tokenizer.advance();
			vmWriter.subLevel.createVar(name,type,"argument","subroutinelevel");
		}
		expect(")");
	}

	private void handleClassVarDecs(){
		while(tokenizer.getToken().equals("static") || tokenizer.getToken().equals("field")){
			handleClassVarDec();
			tokenizer.advance();
		}

	}

	private void handleClassVarDec(){
		expectType("keyword");
		if(!tokenizer.getToken().equals("static") && !tokenizer.getToken().equals("field")){
			throw new Error("expected token to be static of field but got: " + tokenizer.getToken());
		}

		String kind = tokenizer.getToken();

		tokenizer.advance(); 
		String type = handleType();
		tokenizer.advance();
		expectType("identifier");
		String name = tokenizer.getToken();
		tokenizer.advance();

		vmWriter.classLevel.createVar(name, type, kind, "CLASSLEVEL");
		
		while(!tokenizer.getToken().equals(";")){
			expect(",");
			tokenizer.advance();
			expectType("identifier");
			name = tokenizer.getToken();

			vmWriter.classLevel.createVar(name, type, kind, "CLASSLEVEL");
			tokenizer.advance();
		}

		expect(";");
	}


	private String handleType(){

		if(tokenizer.getTokenType() == "keyword"){
			if(
				!tokenizer.getToken().equals("int")  &&
				!tokenizer.getToken().equals("char") &&
				!tokenizer.getToken().equals("boolean") 
			){
				throw new Error("expected type, but got: " + tokenizer.getToken());
			}

			return tokenizer.getToken();
		}

		if(tokenizer.getTokenType() == "identifier"){
			if(!Character.isUpperCase(tokenizer.getToken().charAt(0))){
				throw new Error(
					"expected identifer for defining type, but first character is not uppercase.\n" +
					"token: " + tokenizer.getToken()
				);
			}
			return tokenizer.getToken();
		}

		throw new Error(
			"expected tokentype keyword or identifier but got: " + 
			tokenizer.getTokenType() + " token: " + tokenizer.getToken()
		);

	}

	//for some bizarre reason, the vmEmulator doesnt compile when labels are indented
	private String unindentLabels(String vmCode){
		String newCode = "";
		String[] lines = vmCode.split("\n");
		for(int i = 0; i < lines.length; i++){
			if(lines[i].startsWith("	label")){
				newCode += lines[i].substring(1, lines[i].length()) + "\n";
			}
			else{
				newCode += lines[i] + "\n";
			}
		}
		return newCode;
		

	}
}
