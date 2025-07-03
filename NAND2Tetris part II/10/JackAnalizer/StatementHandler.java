package JackAnalizer;

class StatementHandler extends Parser{
	Expressions expHandler;
	public StatementHandler(Tokenizer tokenizer){
		super(tokenizer);

		expHandler = new Expressions(tokenizer);
	}

	public String handleStatements(){
		String statements = "";
		while(!this.tokenizer.getToken().equals("}")){
			statements += handleStatement(); 
			//after calling the handleStatement, should expect either to be on another statement, or }
		}
		return "<statements>\n" + indent(statements) + "</statements>\n";
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
		String letStatement = "<keyword> let </keyword>\n";

		tokenizer.advance();

		letStatement += handleIdentifier();
		//NEEDS IMPLEMENTING: after Identifer,could have: "[" + expression + "]". 

		tokenizer.advance();
		if(tokenizer.getToken().equals("[")){
			letStatement += "<symbol> [ </symbol>\n";
			tokenizer.advance();
			letStatement += expHandler.handleExpression();
			expect("]");
			letStatement += "<symbol> ] </symbol>\n";
			tokenizer.advance();
		}

		expect("=");
		letStatement += "<symbol> = </symbol>\n";
		tokenizer.advance();

		letStatement += expHandler.handleExpression();
		
		expect(";");

		letStatement += "<symbol> ; </symbol>\n";
		tokenizer.advance();
		

		return"<letStatement>\n" + indent(letStatement) + "</letStatement>\n";
	}
	private String handleIfStatement(){
		expect("if");

		String ifStatement = "<keyword> if </keyword>\n";
		tokenizer.advance();

		expect("(");
		ifStatement += "<symbol> ( </symbol>\n";
		tokenizer.advance();

		ifStatement += expHandler.handleExpression();

		expect(")");
		ifStatement += "<symbol> ) </symbol>\n";
		tokenizer.advance();

		expect("{");
		ifStatement += "<symbol> { </symbol>\n";
		tokenizer.advance();
		
		ifStatement += handleStatements();

		expect("}");
		ifStatement += "<symbol> } </symbol>\n";
		tokenizer.advance();

		if(tokenizer.getToken().equals("else")){
			ifStatement += "<keyword> else </keyword>\n";
			tokenizer.advance();
			expect("{");
			ifStatement += "<symbol> { </symbol>\n";
			tokenizer.advance();
			ifStatement += handleStatements();
			expect("}");
			ifStatement += "<symbol> } </symbol>\n";
			tokenizer.advance();
		}
		
		return "<ifStatement>\n" + indent(ifStatement) + "</ifStatement>\n";

	}

	private String handleWhileStatement(){
		expect("while");

		String whileStatement = "<keyword> while </keyword>\n";
		tokenizer.advance();

		expect("(");
		whileStatement += "<symbol> ( </symbol>\n";
		tokenizer.advance();
		whileStatement += expHandler.handleExpression();
		expect(")");
		whileStatement += "<symbol> ) </symbol>\n";
		tokenizer.advance();

		expect("{");
		whileStatement += "<symbol> { </symbol>\n";
		tokenizer.advance();
		
		whileStatement += handleStatements();

		expect("}");
		whileStatement += "<symbol> } </symbol>\n";
		tokenizer.advance();

		return "<whileStatement>\n" + indent(whileStatement) + "</whileStatement>\n";
	}

	private String handleDoStatement(){
		expect("do");
		tokenizer.advance();
		String doStatement = "<keyword> do </keyword>\n";
		doStatement += expHandler.handleSubroutineCall(0);
		expect(";");
		doStatement += "<symbol> ; </symbol>\n";
		tokenizer.advance();
		return "<doStatement>\n" + indent(doStatement) + "</doStatement>\n";

	}

	private String handleReturnStatement(){
		expect("return");
		String returnStatement = "<keyword> return </keyword>\n";
		
		tokenizer.advance();
		if(!tokenizer.getToken().equals(";")){
			returnStatement += expHandler.handleExpression();
		}
		expect(";");
		
		returnStatement += "<symbol> ; </symbol>\n";

		tokenizer.advance();
		return "<returnStatement>\n" + indent(returnStatement) + "</returnStatement>\n";
	}

}
