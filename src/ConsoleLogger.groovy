package blogparser

class ConsoleLogger extends Logger {

	@Override
	public log(Object msg) {
		println '   '.multiply(nestingLevel) + msg.toString()
	}

}
