package JackCompiler;


class VMWriter{

	public String className;
	public String functionName;
	private int labelCount;

	SymbolTable classLevel;
	SymbolTable subLevel;

	public VMWriter(){
		this.classLevel = new SymbolTable();
		this.subLevel = new SymbolTable();
		this.labelCount = 0;
	}

	public void incrementLabelCount(){
		this.labelCount++;
	}

	public int getLabelCount(){
		return this.labelCount;
	}

}
