package p;
record A(int f, int g){	
	A{
		this.f= f;
	}
	
	public void val(int f) {
		
	}

	public int getVal() {
		return f();
	}
	
	public int f() {
		return f;
	}
}