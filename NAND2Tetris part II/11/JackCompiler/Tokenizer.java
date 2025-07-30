package JackCompiler;

import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Tokenizer{
	
	private String[] keywords = {
		"class", "constructor","function","method","field","static","var","int","char","boolean","void",
		"true","false","null","this","let","do","if","else","while","return"
	};

	private String[] symbols = {
		"{","}","(",")","[","]",".",",",";","+","-","*","/","&","|","<",">","=","~"
	};

	//integerConstant
	//stringConstant
	//identifier
	
	private String type;

	private Scanner scnr = null;
	private String code = "";
	private String currToken;
	private int currCharIndex = 0;  //rename to nextCharIndex
	private int currLine = 1; //only used for debugging purposes
	private boolean finished = false;

	public Tokenizer(String fileName){
		this.scnr = createScanner(fileName);
		setCode();
		removeComments();
	}
	
	public String getCode(){
		return this.code;
	}

	public boolean isFinished(){
		return this.finished;
	}
	
	public void advance(){
		if(this.finished == true){
			return;
		}
		if(currCharIndex  >= code.length()){
			this.finished = true;
			return;
		}

		while(
			code.charAt(currCharIndex) == ' ' || 
			code.charAt(currCharIndex) == '	' || 
			code.charAt(currCharIndex) == '\n'
		){
			this.currCharIndex ++;

			if(currCharIndex  >= code.length()){
				this.finished = true;
				return;
			}
		}
		
		if(isSymbol(code.charAt(currCharIndex))){
			this.type = "symbol";
			this.currToken = String.valueOf(code.charAt(currCharIndex)); 
			this.currCharIndex ++;
			return;
		}
		
		if(code.charAt(currCharIndex) == '"'){
			this.currCharIndex++;
			String strConstant = "";
			
			while(code.charAt(currCharIndex) != '"'){
				strConstant += code.charAt(currCharIndex);

				currCharIndex++;
			}
			this.currToken = strConstant;
			this.type = "stringConstant";
			currCharIndex++;
			return;
		}
		if(Character.isDigit(code.charAt(currCharIndex))){
			String intConstant = "";

			while(Character.isDigit(code.charAt(currCharIndex))){
				intConstant += code.charAt(currCharIndex);
				this.currCharIndex++;
			};
			this.type = "integerConstant";
			this.currToken = intConstant;
			return;
		}
		
		String token = "";

		char currChar = code.charAt(currCharIndex);

		while(!isSymbol(currChar) && currChar != ' ' && currChar != '"' && currChar != '	'){
			token += currChar;
			this.currCharIndex++;
			currChar = code.charAt(currCharIndex);
		}
		
		if(isKeyWord(token)){
			this.type = "keyword";
			this.currToken = token;
		}

		else{
			this.type = "identifier";
			this.currToken = token;
		}
			

	}
	
	public String getToken(){
		return this.currToken;
	}

	public String getTokenType(){
		return this.type;
	}

	private Scanner createScanner(String fileName){
		try{

			FileInputStream stream = new FileInputStream(fileName);
			Scanner scnr = new Scanner(stream);
			return scnr;
		}
		catch(FileNotFoundException fnf){
			throw new Error("file not found for file: " + fileName);
		}
	}

	private void setCode(){
		while(scnr.hasNextLine()){
			this.code = this.code + scnr.nextLine() + '\n';
		}
	}

	private boolean isKeyWord(String keyword){
		for(int i = 0; i < this.keywords.length; i++){
			if(keywords[i].equals(keyword)){
				return true;
			}
		}
		return false;
	}
	private boolean isSymbol(char symbol){
		for(int i = 0; i < this.symbols.length; i++){
			if(symbols[i].equals(String.valueOf(symbol))){

				return true;
			}
		}
		return false;
	}

	private void printArray(char[] arr){
		for(int i = 0; i < arr.length; i++){
			System.out.print(arr[i]);
		}
	}

	private void removeComments(){
		char[] chars = new char[code.length()];
		chars = code.toCharArray();

		//printArray(chars);

		//while looping through chars, if s for skip, then skip char, if d for delete, delete char
		//if d is capital, keep deleting until your find */
		char operation = 's';

		for(int i = 0; i < chars.length; i++){
			if(chars[i] == '/' && chars[i + 1] == '/'){
				operation = 'd';
			}
			else if(chars[i] == '\n' && operation != 'D'){
				operation = 's';
			}
			else if(chars[i] == '/' && chars[i + 1] == '*'){
				operation = 'D';
			}
			else if(chars[i] == '*' && chars[i+1] == '/'){

				if(operation != 'D'){
					System.err.println("expected operation to equal D");
				}
				operation = 's';
				chars[i] = ' ';
				chars[i+1] = ' ';
			}

			if (operation == 'd' || operation == 'D'){
				chars[i] = ' ';
			}
		}
		code = new String(chars);
	}
}
