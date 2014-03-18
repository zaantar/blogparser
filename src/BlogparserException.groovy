package blogparser

class BlogparserException extends Exception {

	final msg

	@Override
	public String toString() {
		"Blogparser exception: $msg"
	}

}
