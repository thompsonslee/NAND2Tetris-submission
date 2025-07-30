package JackCompiler;

class StatementHandler extends Parser{
	Expressions expHandler;

	public StatementHandler(Tokenizer tokenizer, VMWriter vmWriter){
		super(tokenizer,vmWriter);

		expHandler = new Expressions(tokenizer,vmWriter);
	}

	public String handleStatements(){
		String statements = "";
		while(!this.tokenizer.getToken().equals("}")){
			statements += handleStatement(); 
			//after calling the handleStatement, should expect either to be on another statement, or }
		}
		return statements;
	}

	private String handleStatement(){
		switch(this.tokenizer.getToken()){
			case("let"):
				return handleLetStatement();
		    case("if"):
		    	return handleIfStatement();
		    case("while"):
		    	return handleWhileStatement();
		    case("do"):
		    	return handleDoStatement();
		    case("return"):
		    	return handleReturnStatement();
			default:
				throw new Error(
					"expected let|if|while|do|return, but got " + tokenizer.getToken() + "\n" + 
					"the next tokens would be: " +
					getNextToken() + " " + getNextToken() + " " + getNextToken() + " " + getNextToken()
				);
		}
	}

	private String getNextToken(){
		tokenizer.advance();
		return tokenizer.getToken();
	}

	private String handleLetStatement(){
		expect("let");

		tokenizer.advance();

		expectType("identifier");
		String varName = tokenizer.getToken();
		
		String varKind;
		int varIndex;

		if(vmWriter.subLevel.exists(varName)){
			varKind = vmWriter.subLevel.getKind(varName).toString();			
			varIndex = vmWriter.subLevel.getIndex(varName);
		}
		else if	(vmWriter.classLevel.exists(varName)){
			
			varKind = vmWriter.classLevel.getKind(varName).toString();
			varIndex = vmWriter.classLevel.getIndex(varName);
		}
		else {
			throw new Error("cant find var: " + varName + " on symbolTables");
		}

		if(varKind.equals("FIELD")){
			varKind = "this";	
		}

		String popCommand = "pop " + varKind.toLowerCase() + " " + varIndex + "\n";


		tokenizer.advance();
		if(tokenizer.getToken().equals("[")){
			return handleArrayAccessor(varKind, varIndex);
		}

		else{

			expect("=");
			tokenizer.advance();

			String expression = expHandler.handleExpression();
			
			expect(";");

			tokenizer.advance();

			return expression + popCommand;
		}
	}

	private String handleArrayAccessor(String varKind, int varIndex){
		tokenizer.advance();

		String arrAccess= 
			expHandler.handleExpression() +
			"push " + varKind.toLowerCase() + " " + varIndex + "\n" +
			"add\n";

		expect("]");
		tokenizer.advance();

		expect("=");
		tokenizer.advance();
		
		arrAccess += 
			expHandler.handleExpression() +
			"pop temp 0\n" +
			"pop pointer 1\n" +
			"push temp 0\n" +
			"pop that 0\n";

		expect(";");

		tokenizer.advance();

		return arrAccess;
	}

	private String handleIfStatement(){
		expect("if");

		tokenizer.advance();

		expect("(");
		tokenizer.advance();

		String ifExpression = expHandler.handleExpression() + "not\n";

		expect(")");
		tokenizer.advance();

		expect("{");
		tokenizer.advance();
		
		String exitLabel = vmWriter.className + "_" + vmWriter.getLabelCount()+ "\n";
		vmWriter.incrementLabelCount();;

		String elseLabel = vmWriter.className + "_" + vmWriter.getLabelCount()+ "\n";
		vmWriter.incrementLabelCount();

		String ifStatements = handleStatements();


		expect("}");
		tokenizer.advance();
		
		String elseStatements = "";

		if(tokenizer.getToken().equals("else")){
			tokenizer.advance();
			expect("{");
			tokenizer.advance();
			elseStatements = handleStatements();
			expect("}");
			tokenizer.advance();
		}

		return 
			ifExpression + 
			"if-goto " + elseLabel +
			ifStatements +
			"goto " + exitLabel +
			"label " + elseLabel + 
			elseStatements + //elseStatements will be "" when just an if statement, instead of an if else statement
			"label " + exitLabel;

	}

	private String handleWhileStatement(){
		expect("while");
		tokenizer.advance();

		expect("(");
		tokenizer.advance();
		String expression = expHandler.handleExpression() + "not\n";
		expect(")");
		tokenizer.advance();

		expect("{");
		tokenizer.advance();
		

		String enterLabel = vmWriter.className + "_" + vmWriter.getLabelCount() + "\n";
		vmWriter.incrementLabelCount();;

		String exitLabel = vmWriter.className + "_" + vmWriter.getLabelCount() + "\n";
		vmWriter.incrementLabelCount();

		String statements = handleStatements();

		expect("}");
		tokenizer.advance();

		return
			"label " + enterLabel +
			expression +
			"if-goto " + exitLabel +
			statements +
			"goto " + enterLabel +
			"label " + exitLabel;
	}

	private String handleDoStatement(){
		expect("do");
		tokenizer.advance();

		String doStatement = expHandler.handleSubroutineCall(true,"");
		expect(";");

		tokenizer.advance();

		return doStatement;

	}

	private String handleReturnStatement(){

		expect("return");
		String returnStatement = "return";
		
		tokenizer.advance();
		if(!tokenizer.getToken().equals(";")){
			returnStatement =  expHandler.handleExpression() + returnStatement;;
		}else{
			returnStatement = "push constant 0\n" + returnStatement;
		}

		returnStatement += "\n";

		expect(";");
		
		tokenizer.advance();
		return returnStatement;
	}

}
