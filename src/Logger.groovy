package blogparser


abstract class Logger {
	
	protected nestingLevel = 0
	
	/** Logs a message (may be any object with toString method) */
	abstract public log(msg)
	
	/* Methods to regulate nesting. */
	public down() {
		nestingLevel++
	}
	
	public up() {
		nestingLevel--
	}

}
