package JackCompiler;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class Compiler{
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("no argument Given");
			return;
		}
		if(args[0].length() > 5){
			if(args[0].substring(args[0].length() - 5).equals(".jack")){

				Tokenizer tokenizer = new Tokenizer(args[0]);
				VMWriter vmWriter = new VMWriter();
				CompilationEngine engine = new CompilationEngine(tokenizer,vmWriter);

				String vm = engine.CompileClass();

				createVMFile(args[0],vm);

				return;
			}
		}

		handleDirectory(args[0]);

	}

	private static void handleDirectory(String dirName){
		File dir = new File(dirName);
		
		if(!dir.exists() && !dir.isDirectory()){
			throw new Error("invaid argument given");
		}

		String[] jackFileNames = dir.list();

		for(int i = 0; i < jackFileNames.length; i++){

			if(!jackFileNames[i].substring(jackFileNames[i].length() - 5).equals(".jack")){
				continue;
			}
			Tokenizer tokenizer = new Tokenizer(dirName + "/" + jackFileNames[i]);
			
			VMWriter vmWriter = new VMWriter();
			CompilationEngine engine = new CompilationEngine(tokenizer,vmWriter);
			String vm = engine.CompileClass();

			createVMFile(jackFileNames[i],vm);

		} 
	}

	private static void createVMFile(String jackFileName, String vm){
		try{

			String fileName = jackFileName.substring(0, jackFileName.length() - 4) + "vm";
			File file = new File(fileName);

			if(!file.createNewFile()){
				System.out.println("overriding file: " + fileName);
				file.delete();
				if(!file.createNewFile()){
					throw new Error("an error occured while trying to delete already existing file");
				}
			}
			


			FileWriter writer = new FileWriter(fileName);
			writer.write(vm);
			writer.close();
			System.out.println("Created " + fileName);
		} catch(IOException e){
			System.out.println(e.getMessage());
		}
	}

	private static String createTokenizerXML(Tokenizer tokenizer){
		
		tokenizer.advance();		
		String xml = "<tokens>\n";
		while(!tokenizer.isFinished()){
			String token = tokenizer.getToken();

			if(token.equals(">")){
				token = "&gt;";
			}
			else if(token.equals("<")){
				token = "&lt;";
			}
			else if(token.equals("&")){
				token = "&amp;";
			}

			xml += "    <" + tokenizer.getTokenType() + ">" + token + 
			"</" + tokenizer.getTokenType() + ">\n";
			tokenizer.advance();
		}
		xml += "</tokens>";

		return xml;
	}

}
