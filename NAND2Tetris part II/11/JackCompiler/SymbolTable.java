package JackCompiler;

import JackCompiler.Var.Kind;

class SymbolTable{

	private Var[] vars;

	private boolean isMethod; //only need this to offset args by +1 when getting argumentCount

	public SymbolTable(){
		this.vars = new Var[30]; //if this isnt enough then christ
	}

	public void createVar(String name, String type, String kind, String scope){
		try{
			Var.Kind enumKind = Var.Kind.valueOf(kind.toUpperCase());
			Var.Scope enumScope = Var.Scope.valueOf(scope.toUpperCase());

			Var var = new Var(name,type,enumKind,enumScope);
			push(var);
		} catch(IllegalArgumentException e){
			
			throw new Error("invalid var enum given: " + name + " " + type + " " + kind + " " + scope);
		}
	}

	public boolean isMethod(){
		return isMethod;
	}

	public void setIsMethod(){
		this.isMethod = true;
	}

	public void clear(){
		this.isMethod = false;
		for(int i = 0; i < vars.length; i++){
			if(vars[i] == null){
				return;
			}
			vars[i] = null;
		}
	}

	private void push(Var var){
		for(int i = 0; i < vars.length; i++){
			if(vars[i] == null){
				vars[i] = var;
				return;
			}
		}
		throw new Error("reached end of this.vars");
	}

	public int getCount(String kind){
		try{
			Var.Kind enumKind = Var.Kind.valueOf(kind.toUpperCase());

			int count = 0;
			for(int i = 0; i < vars.length; i++){
				if(vars[i] == null){
					break;
				}

				if(vars[i].getKind() == enumKind){
					count++;
				}
			}
			return count;

		} catch(IllegalArgumentException e){
			
			throw new Error("invalid kind given: " + kind);
		}
	}
	
	

	public boolean exists(String name){
		for(int i = 0; i < vars.length; i++){
			if(vars[i] == null){
				return false;
			}
			if(vars[i].getName().equals(name)){
				return true;
			}
		}
		return false;
	}

	public String getType(String name){
		for(int i = 0; i < vars.length; i++){
			if(vars[i].getName().equals(name)){
				return vars[i].getType();
			}
		}
		throw new Error("trying to get type for varName: " + name + " but it does not exist");
	}

	public Var.Kind getKind(String name){
		for(int i = 0; i < vars.length; i++){
			if(vars[i].getName().equals(name)){
				return vars[i].getKind();
			}
		}
		throw new Error("trying to get Kind for varName: " + name + " but it does not exist");
	}

	public Var.Scope getScope(String name){
		for(int i = 0; i < vars.length; i++){
			if(vars[i].getName().equals(name)){
				return vars[i].getScope();
			}
		}
		throw new Error("trying to get Scope for varName: " + name + " but it does not exist");
	}

	public int getIndex(String name){
		Var.Kind kind = getKind(name);
		int index = 0;
		for(int i = 0; i < vars.length; i++){

			if(vars[i].getName().equals(name)){
				if(kind == Kind.ARGUMENT && this.isMethod){
					index++;
				}
				return index;
			}

			if(vars[i].getKind() == kind){
				index++;
			}
		}
		throw new Error("Trying to get index of varName: " + name + " but it does not exist");
	}

	public void printTable(){
		for(int i = 0; i < vars.length; i++){
			if(vars[i] == null){
				return;
			}
			System.out.println(
				"	name: " + vars[i].getName() + "\n" +
				"	type: " + vars[i].getType() + "\n" +
				"	kind: " + vars[i].getKind() + "\n" +
				"	scope: " + vars[i].getScope() + "\n"
			);
		}
	}
}
