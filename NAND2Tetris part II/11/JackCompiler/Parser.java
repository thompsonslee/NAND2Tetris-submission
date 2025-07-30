package JackCompiler;

public abstract class Parser{
	protected Tokenizer tokenizer;
	protected VMWriter vmWriter;

	protected Parser(Tokenizer tokenizer, VMWriter vmWriter){
		this.tokenizer = tokenizer;
		this.vmWriter = vmWriter;
	}	
	
	protected String indent(String string){
		
		if(string.length() == 0){
			return string;
		}
		//may need to be adjusted
		String[] lines = new String[1000];
		
		lines = string.split("\n");
		for(int i = 0; i < lines.length; i++){
			if(lines[i] == null){
				break;
			}else{
				lines[i] = "	" + lines[i] + "\n";
			}
		}
		String newString = "";
		for(int i = 0; i < lines.length; i++){
			if(lines[i] == null){
				break;
			}
			newString += lines[i];
		}

		if(newString.length() <= 0){
			throw new Error("newString length is 0 or less. something must be wrong");
		}
		return newString;
	}
	
	protected void expect(String token){
		if(!tokenizer.getToken().equals(token)){
			throw new Error("Expected token: " + token + " but got " + tokenizer.getToken() + 
				getNextTokens()
			);
		}
	}
	
	protected String getNextTokens(){
		tokenizer.advance();
		String tokens = "\n next tokens: " + tokenizer.getToken();
		tokenizer.advance();
		tokens += " " + tokenizer.getToken();
		tokenizer.advance();
		tokens += " " + tokenizer.getToken();
		tokenizer.advance();
		tokens += " " + tokenizer.getToken();
		tokenizer.advance();
		tokens += " " + tokenizer.getToken();
		return tokens;
	}

	protected void expectType(String type){
		if(!tokenizer.getTokenType().equals(type)){
			throw new Error(
				"Expected tokentype: " + type + " but got type: " + tokenizer.getTokenType() +
				"token: " + tokenizer.getToken() + getNextTokens()
			);
		}
	}

	protected String handleIdentifier(){
		expectType("identifier");

		String token = tokenizer.getToken();

		String kind;
		int index;

		if(vmWriter.subLevel.exists(tokenizer.getToken())){
			kind = vmWriter.subLevel.getKind(token).toString().toLowerCase();
			index = vmWriter.subLevel.getIndex(token);
		}
		else if(vmWriter.classLevel.exists(token)){
			kind = vmWriter.classLevel.getKind(token).toString().toLowerCase(); index = vmWriter.classLevel.getIndex(token);
		}
		else{
			//must me a name of a method, or name of a class
			return "";
		}

		if(kind.equals("field")){
			kind = "this";
		}

		return "push " + kind + " " + index + "\n";
	}







}
