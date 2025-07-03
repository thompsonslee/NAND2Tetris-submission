package JackAnalizer;

public class CompilationEngine extends Parser{
	
	public CompilationEngine(Tokenizer tokenizer){
		super(tokenizer);
	}
	
	public String CompileClass(){

		String jackClass = "<class>\n";

		tokenizer.advance();
		
		expectType("keyword");
		expect("class");
		
		String innerClass = "<keyword> class </keyword>\n";
		tokenizer.advance();

		innerClass += handleIdentifier(); //class name

		tokenizer.advance();

		expectType("symbol");
		expect("{");
		innerClass += "<symbol> { </symbol>\n";
		
		tokenizer.advance();

		innerClass += handleClassVarDecs();

		innerClass += handleSubroutineDecs();
		
		expect("}");
		innerClass += "<symbol> } </symbol>\n";

		jackClass += indent(innerClass);
		jackClass += "</class>";
		return jackClass;
	}


	private String handleSubroutineDecs(){
		String subRoutineDecs = "";
		while(
			tokenizer.getToken().equals("constructor") ||
			tokenizer.getToken().equals("function") ||
			tokenizer.getToken().equals("method")
		){
			subRoutineDecs += handleSubrountieDec();
		}

		return subRoutineDecs;
	}

	private String handleSubrountieDec(){
		expectType("keyword");
		if(
			!tokenizer.getToken().equals("constructor") &&
			!tokenizer.getToken().equals("function") &&
			!tokenizer.getToken().equals("method") 
		){
			throw new Error("expected constructor,function or method token but got: " + tokenizer.getToken());
		}

		String subroutineDec = "<keyword> " + tokenizer.getToken() + " </keyword>\n";

		tokenizer.advance();

		if(tokenizer.getToken().equals("void")){
			subroutineDec += "<keyword> void </keyword>\n";
		}
		else{
			subroutineDec += handleType();
		}
		
		tokenizer.advance();

		subroutineDec += handleIdentifier(); //function name

		tokenizer.advance();
		expectType("symbol");
		expect("(");
		subroutineDec += "<symbol> ( </symbol>\n";
		
		tokenizer.advance();

		subroutineDec += handleParameterList();
		expect(")");

		subroutineDec += "<symbol> ) </symbol>\n";
		

		tokenizer.advance();
	
		subroutineDec +=  handleSubroutineBody();

		subroutineDec = "<subroutineDec>\n" + indent(subroutineDec) + "</subroutineDec>\n";
		return subroutineDec;
	}

	private String handleSubroutineBody(){
		expect("{");
		
		String body = "<symbol> { </symbol>\n";

		tokenizer.advance();
		if(tokenizer.getToken().equals("var")){
			body += handleVarDecs();
		}
		
		StatementHandler statements = new StatementHandler(tokenizer);

		body += statements.handleStatements();
		//handle statements

		tokenizer.advance();

		body += "<symbol> } </symbol>\n";
		
		return "<subroutineBody>\n" + indent(body) + "</subroutineBody>\n";
	}


	private String handleVarDecs(){
		expect("var");
		
		String varDecs = "";

		while(tokenizer.getToken().equals("var")){
			varDecs += handleVarDec();
			tokenizer.advance();
		}

		return varDecs;
	}

	private String handleVarDec(){
		expect("var");
		
		String varDec = "<keyword> var </keyword>\n";

		tokenizer.advance();
		varDec += handleType();
		tokenizer.advance();
		varDec += handleIdentifier();
		tokenizer.advance();

		while(!tokenizer.getToken().equals(";")){
			expect(",");
			varDec += "<symbol> , </symbol>\n";
			tokenizer.advance();
			varDec += handleIdentifier();
			tokenizer.advance();
		}

		expect(";");
		varDec += "<symbol> ; </symbol>\n";
		
		return "<varDec>\n" + indent(varDec) + "</varDec>\n";
		
	}

	private String handleParameterList(){
		String params = "";
		if(!tokenizer.getToken().equals(")")){
			params += handleType();
			tokenizer.advance();
			params += handleIdentifier();
			tokenizer.advance();
		}
		while(!tokenizer.getToken().equals(")")){
			expect(",");
			params += "<symbol> , </symbol>\n";
			tokenizer.advance();
			params += handleType();
			tokenizer.advance();
			params += handleIdentifier();
			tokenizer.advance();
		}

		expect(")");

		return "<parameterList>\n" + indent(params) + "</parameterList>\n";
	}

	private String handleClassVarDecs(){
		String varDecs = "";

		while(tokenizer.getToken().equals("static") || tokenizer.getToken().equals("field")){
			varDecs += handleClassVarDec();
			tokenizer.advance();
		}

		return varDecs;
	}

	private String handleClassVarDec(){
		expectType("keyword");
		if(!tokenizer.getToken().equals("static") && !tokenizer.getToken().equals("field")){
			throw new Error("expected token to be static of field but got: " + tokenizer.getToken());
		}

		String varDec = "<keyword> " + tokenizer.getToken() + " </keyword>\n";

		tokenizer.advance(); 
		varDec += handleType();
		tokenizer.advance();
		varDec +=handleIdentifier();
		tokenizer.advance();
		
		while(!tokenizer.getToken().equals(";")){
			expect(",");
			varDec += "<symbol> , </symbol>\n";
			tokenizer.advance();
			varDec += handleIdentifier();
			tokenizer.advance();
		}

		expect(";");
		varDec += "<symbol> ; </symbol>\n";

		return "<classVarDec>\n" + indent(varDec) + "</classVarDec>\n";

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

			return "<keyword> " + tokenizer.getToken() + " </keyword>\n";
		}

		if(tokenizer.getTokenType() == "identifier"){
			if(!Character.isUpperCase(tokenizer.getToken().charAt(0))){
				throw new Error(
					"expected identifer for defining type, but first character is not uppercase.\n" +
					"token: " + tokenizer.getToken()
				);
			}
			return "<identifier> " + tokenizer.getToken() + " </identifier>\n";
		}

		throw new Error(
			"expected tokentype keyword or identifier but got: " + 
			tokenizer.getTokenType() + " token: " + tokenizer.getToken()
		);

	}
}
