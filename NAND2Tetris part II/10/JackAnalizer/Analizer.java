package JackAnalizer;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class Analizer{
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("no argument Given");
			return;
		}
		System.out.println(args[0]);
		if(args[0].length() > 5){
			if(args[0].substring(args[0].length() - 5).equals(".jack")){

				Tokenizer tokenizer = new Tokenizer(args[0]);
				CompilationEngine engine = new CompilationEngine(tokenizer);

				String xml = engine.CompileClass();

				createXMLFile(args[0],xml);

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

			Tokenizer tokenizer = new Tokenizer(dirName + "/" + jackFileNames[i]);
			CompilationEngine engine = new CompilationEngine(tokenizer);

			String xml = engine.CompileClass();

			createXMLFile(jackFileNames[i],xml);

		} 
	}

	private static void createXMLFile(String jackFileName, String xml){
		try{

			String fileName = jackFileName.substring(0, jackFileName.length() - 4) + "xml";
			File file = new File(fileName);

			if(!file.createNewFile()){
				System.out.println("file already exists");
				return;
			}
			


			FileWriter writer = new FileWriter(fileName);
			writer.write(xml);
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
