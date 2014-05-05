package blogparser


abstract class Logger {
	
	protected nestingLevel = 0
	protected verbosity = 0
	
	/** Logs a message (may be any object with toString method) */
	abstract public log(String msg, int severity)
	
	/* Methods to regulate nesting. */
	public down() {
		nestingLevel++
	}
	
	public up() {
		nestingLevel--
	}
	
	
	abstract public setGlobalStepCount(int value);
	abstract public globalStep();
	abstract public resetGlobalSteps();
	
	abstract public setStepCount(int value);
	abstract public int getStepCount();
	abstract public step();
	abstract public resetSteps();
	
	public setVerbosity(int verbosity) {
		this.verbosity = verbosity
	}
}
