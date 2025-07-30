package JackCompiler;

class Var{


	public enum Kind {
		FIELD,STATIC,LOCAL,ARGUMENT
	};

	public enum Scope{
		CLASSLEVEL,SUBROUTINELEVEL
	};

	private String name;
	private String type;
	private Kind kind;
	private Scope scope;

	public Var(String name, String type, Kind kind, Scope scope){
		this.name = name;
		this.type = setType(type);
		this.kind = kind;
		this.scope = scope;
	}

	private String setType(String type){
		if(type.equals("int") || type.equals("char") || type.equals("boolean")){
			return type;
		}
		if(!Character.isUpperCase(type.charAt(0))){
			throw new Error("type is not int, char or boolean so should be upper case for token: " + type);
		}
		return type;
	}


	public String getName(){
		return this.name;
	}

	public String getType(){
		return this.type;
	}

	public Kind getKind(){
		return this.kind;
	}
	
	public Scope getScope(){
		return this.scope;
	}
}
