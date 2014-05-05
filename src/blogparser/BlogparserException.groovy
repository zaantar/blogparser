package blogparser

class BlogparserException extends Exception {

	final msg
	
	public BlogparserException(msg) {
		this.msg = msg
	}
	
	public BlogparserException() { }

	@Override
	public String toString() {
		"Blogparser exception: $msg"
	}

}
