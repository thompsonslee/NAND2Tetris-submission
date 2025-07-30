package JackCompiler;


import java.util.concurrent.atomic.AtomicInteger;

class Expressions extends Parser{

	Expressions(Tokenizer tokenizer, VMWriter vmWriter){
		super(tokenizer,vmWriter);
	}
	
	public String handleExpression(){
		
		String firstTerm = handleTerm();
		String op = null;
		String secondTerm = null;


		if(!isEndOfExpression()){
			op = handleOp();
			tokenizer.advance();
			secondTerm = handleTerm();
		}


		if(!isEndOfExpression()){
			throw new Error("expected ; or ) or ] or , but got: " + tokenizer.getToken() + getNextTokens());

		}
		
		//at this point, the expression should be done
		if(op == null){
			return firstTerm;
		}
		else{
			return firstTerm + secondTerm + op;
		}
	}
	
	private boolean isEndOfExpression(){
		
		String token = tokenizer.getToken();

		return token.equals(";")
			|| token.equals(")")
			|| token.equals("]")
			|| token.equals(",");
	}


	private String handleOp(){

		expectType("symbol");
		
		char[] ops = {'+','-','*','/','&','|','<','>','='};
		String[] vmOps = {
			"add",
			"sub",
			"call Math.multiply 2",
			"call Math.divide 2",
			"and",
			"or",
			"lt",
			"gt",
			"eq"
		};
		
		for(int i = 0; i < ops.length + 1; i++){
			if(tokenizer.getToken().charAt(0) == ops[i]){
				return vmOps[i] + "\n";
			}
		}
		throw new Error(
			"expected an OP symbol, but got: " + tokenizer.getToken()
			+ getNextTokens()
		);
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
		//	- or ~
		
		String term = "";
		
		if(tokenType.equals("integerConstant")){
			term = "push constant " + tokenizer.getToken() + "\n";
			tokenizer.advance();
		}

		else if(tokenType.equals("stringConstant")){
			term = handleString();
			tokenizer.advance();
		}

		else if(tokenType.equals("keyword")){
			term = handleKeyWord();
			tokenizer.advance();
		}
		
		else if(tokenizer.getToken().equals("-") || tokenizer.getToken().equals("~")){
			String op;
			if(tokenizer.getToken().equals("-")){
				op = "neg\n";
			}
			else{
				op = "not\n";
			}
			
			tokenizer.advance();
			String opTerm = handleTerm();

			term = opTerm + op;
		}

		else if(tokenizer.getToken().equals("(")){
			tokenizer.advance();
			term = handleExpression();
			expect(")");
			tokenizer.advance();
		}

		else if(tokenType.equals("identifier")){
			String identifier = tokenizer.getToken();
			
			term = handleIdentifier();
			
			tokenizer.advance();
			
			String token = tokenizer.getToken();
			if(token.equals("[")){
				if(term.equals("")){
					throw new Error("trying to do array accessing on identifier: " + identifier + 
						", but handleIdentifier() returned nothing for that identifier");
				}

				tokenizer.advance();
				String arrAccessor = handleExpression() +
				term + 
				"add\n" +
				"pop pointer 1\n" +
				"push that 0\n";

				expect("]");
				tokenizer.advance();
				term = arrAccessor;
			}

			//could this be allowed after handling array access??	
			else if(token.equals("(") || token.equals(".")){
				term = handleSubroutineCall(false,identifier);
			}
		}
		return term;
	}

	private String handleKeyWord(){
		String token = tokenizer.getToken();
		
		if(token.equals("true")){

			return 
				"push constant 1\n" +
				"neg\n";

		}
		else if(token.equals("false")){
			return "push constant 0\n";
		}

		else if(token.equals("null")){
			return "push constant 0\n";
		}
		else if(token.equals("this")){
			return "push pointer 0\n";
		}

		else {
			throw new Error("expected keyword but got: " + token);
		}
	}
	
	//options parameter: if optionalString is "", assumes were starting with identifier.
	//for example variable.doThing();
	// or doThing2();
	//if set to something other than "", assume that were starting with the . or ( token,
	//and that the optionalString is the name of the variable
	//for example .doThing();
	//or ()
	public String handleSubroutineCall(boolean voidReturn, String optionalString){

		String firstToken = null;
		String secondToken = null;
		if(optionalString.length() == 0){
			expectType("identifier");
			firstToken = tokenizer.getToken();
			tokenizer.advance();
			
		}else{
			firstToken = optionalString;
		}
		

		//if below is true, firstToken is a class or object
		if(tokenizer.getToken().equals(".")){
			tokenizer.advance();
			expectType("identifier");
			secondToken = tokenizer.getToken();
			tokenizer.advance();
		}
			

		expect("(");

		tokenizer.advance();
		
		AtomicInteger argsCount = new AtomicInteger(0);
		String args = handleExpressionList(argsCount);

		expect(")");
		
		tokenizer.advance();

		String subroutineCall = handleCall(firstToken,secondToken,args , argsCount.intValue());
		if(voidReturn){
			subroutineCall += "pop temp 0\n";
		}

		return subroutineCall;
	}

	private String handleCall(String firstToken, String secondToken, String args, int argsCount){

		if(firstToken == null){
			throw new Error("firstToken is null for handleCall");
		}
		
		String subroutineCall = null;

		boolean isLowerCase = Character.isLowerCase(firstToken.charAt(0));

		//if just firstToken: is a call to method on current obj
		if(secondToken == null){
			
			String call = "call " + vmWriter.className + "." + firstToken;
			subroutineCall = "push pointer 0\n" + args + call + " " + (argsCount + 1) + "\n";
		}

		//if Capital letter on FirstToken and a secondToken: a call to a function on a class
		else if(!isLowerCase){
			String call = "call " + firstToken + "." + secondToken;
			subroutineCall = args + call + " " + argsCount + "\n" ;
		}

		//if firstToken and secondToken: must be call to a method on an object that is a variable
		else if(isLowerCase && (secondToken != null)){
			 
			int index = -1;
			String type = "";
			String kind = "";

			if(vmWriter.subLevel.exists(firstToken)){
				index = vmWriter.subLevel.getIndex(firstToken);
				type = vmWriter.subLevel.getType(firstToken);
				kind = vmWriter.subLevel.getKind(firstToken).toString().toLowerCase();
			} 

			else if(vmWriter.classLevel.exists(firstToken)){
				index = vmWriter.classLevel.getIndex(firstToken);
				type = vmWriter.classLevel.getType(firstToken);
				kind = "this";
			}

			else{
				throw new Error("token: " + firstToken + " does not exist on the as a var or field");
			}

			
			String objPointer = "push " + kind + " " + index + "\n";
			String call = "call " + type + "." + secondToken + " " + (argsCount + 1) + "\n";
			subroutineCall = objPointer + args + call;
		}

		else{
			throw new Error("invalid handleCall. firstToken: " + firstToken + ", secondToken: " + secondToken);
		}

		return subroutineCall;
	}

	private String handleExpressionList(AtomicInteger argsCount){
		String expressions = "";
		if(!tokenizer.getToken().equals(")")){
			
			expressions += handleExpression();

			argsCount.set(argsCount.get() + 1);
		}
		while(tokenizer.getToken().equals(",")){
			tokenizer.advance();
			expressions += handleExpression();
			argsCount.set(argsCount.get() + 1);
		}
		expect(")");
		return expressions;
	}

	private String handleString(){
		expectType("stringConstant");
		
		String string = tokenizer.getToken();

		String vmCode = 
		"push constant " + string.length() + "\n" +
		"call String.new 1\n";

		for(int i = 0; i < string.length(); i++){
			vmCode +=
				"push constant " + (int)string.charAt(i) + "\n" +
				"call String.appendChar 2\n";
		}
		
		return vmCode;
}










}
