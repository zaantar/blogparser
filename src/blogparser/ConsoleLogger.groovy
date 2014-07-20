package blogparser

class ConsoleLogger extends Logger {

	@Override
	public log(String msg, int severity) {
		println '   '.multiply(nestingLevel) + msg.toString()
	}

	public log(String msg) { log(msg, 0) }
	
	@Override
	public globalStep() { }
	@Override
	public step() { }
	@Override
	public setGlobalStepCount(int value) {	}
	@Override
	public setStepCount(int value) { }
	@Override
	public int getStepCount() { }
	@Override
	public resetSteps() { }
	@Override
	public resetGlobalSteps() {	}


}
