package JackAnalizer;

class Expressions extends Parser{

	Expressions(Tokenizer tokenizer){
		super(tokenizer);
	}
	
	public String handleExpression(){
		String expression = handleTerm();
		String token = tokenizer.getToken();

		if(
			   !token.equals(";") 
			&& !token.equals(")") 
			&& !token.equals("]")
			&& !token.equals(",")
		){
			expression += handleOp();
			tokenizer.advance();
			expression += handleTerm();
		}
			
		token = tokenizer.getToken();
		if(
			   !token.equals(";")
			&& !token.equals(")")
			&& !token.equals("]")
			&& !token.equals(",")
		){
			throw new Error("expected ; or ) or ] or , but got: " + tokenizer.getToken() + getNextTokens());

		}

		return "<expression>\n" + indent(expression) + "</expression>\n";
	}

	private String handleOp(){

		expectType("symbol");
		
		char[] ops = {'+','-','*','/','&','|','<','>','='};
		
		for(int i = 0; i < ops.length + 1; i++){
			if(i == 9){
				throw new Error(
					"expected an OP symbol, but got: " + tokenizer.getToken()
					+ getNextTokens()
				);
			}
			else if(tokenizer.getToken().charAt(0) == ops[i]){
				break;
			}
		}

		if(tokenizer.getToken().equals("<")){
			
			return "<symbol> &lt; </symbol>\n";
		}

		if(tokenizer.getToken().equals(">")){
			return "<symbol> &gt; </symbol>\n";
		}
		
		if(tokenizer.getToken().equals("&")){

			return "<symbol> &amp; </symbol>\n";
		}

		return "<symbol> " + tokenizer.getToken() + " </symbol>\n";
	}

	private String handleTerm(){
		String tokenType = tokenizer.getTokenType();

		//term can either be 
		//	integerconstant
		//	stringConstant
		//	keywordConstant (true,false,null,this)
		//	varName
		//	varname[expression]
		//	subroutineCall:  function(list) or identifier.function(list)
		//
		
		String term = "";
		
		if(tokenType.equals("integerConstant")){
			term = "<integerConstant> " + tokenizer.getToken() + " </integerConstant>\n";
			tokenizer.advance();
		}

		else if(tokenType.equals("stringConstant")){
			term = "<stringConstant> " + tokenizer.getToken() + " </stringConstant>\n";
			tokenizer.advance();
		}

		else if(tokenType.equals("keyword")){
			term += "<keyword> " + tokenizer.getToken() + " </keyword>\n";
			tokenizer.advance();
		}
		
		else if(tokenizer.getToken().equals("-") || tokenizer.getToken().equals("~")){
			term += "<symbol> " + tokenizer.getToken() + "</symbol>\n";
			tokenizer.advance();
			term += handleTerm();
		}

		else if(tokenizer.getToken().equals("(")){
			term += "<symbol> ( </symbol>\n";
			tokenizer.advance();
			term += handleExpression();
			expect(")");
			term += "<symbol> ) </symbol>\n";
			tokenizer.advance();
		}

		else if(tokenType.equals("identifier")){
			term += handleIdentifier();

			tokenizer.advance();
			
			String token = tokenizer.getToken();
			if(token.equals("[")){
				term += "<symbol> [ </symbol>\n";
				tokenizer.advance();
				term += handleExpression();
				expect("]");
				term += "<symbol> ] </symbol>\n";
				tokenizer.advance();
			}
			
			if(token.equals("(") || token.equals(".")){
				term += handleSubroutineCall(1);
			}
		}
		return "<term>\n" + indent(term) + "</term>\n";
	}

	
	//options parameter: if set to 0, assumes were starting with identifier.
	//for example variable.doThing();
	// or doThing2();
	//if set to 1, assume that were starting with the . or ( token.
	//for example .doThing();
	//or ()
	public String handleSubroutineCall(int opt){
		String subroutineCall = "";
		if(opt == 0){
			expectType("identifier");
			subroutineCall = "<identifier> " + tokenizer.getToken() + " </identifier>\n";
			tokenizer.advance();

		}

		if(tokenizer.getToken().equals(".")){
			subroutineCall += "<symbol> . </symbol>\n";
			tokenizer.advance();
			expectType("identifier");
			subroutineCall += "<identifier> " + tokenizer.getToken() + " </identifier>\n";
			tokenizer.advance();
		}

		expect("(");

		subroutineCall += "<symbol> ( </symbol>\n";
		tokenizer.advance();
		subroutineCall += handleExpressionList();

		expect(")");

		subroutineCall += "<symbol> ) </symbol>\n";
		tokenizer.advance();

		return subroutineCall;
	}

	private String handleExpressionList(){
		String list = "";
		if(!tokenizer.getToken().equals(")")){
			
			list += handleExpression();
		}
		while(tokenizer.getToken().equals(",")){
			list += "<symbol> , </symbol>\n";
			tokenizer.advance();
			list += handleExpression();
		}
		expect(")");

		return "<expressionList>\n" + indent(list) + "</expressionList>\n";
	}










}
