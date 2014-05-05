package blogparser

import javax.swing.JProgressBar
import javax.swing.JTextArea

class TextAreaLogger extends Logger {

	JTextArea outputArea
	JProgressBar globalProgressBar
	JProgressBar currentStepProgressBar
	
	public TextAreaLogger(JTextArea outputArea, JProgressBar globalProgressBar, JProgressBar currentStepProgressBar) {
		this.outputArea = outputArea;
		this.globalProgressBar = globalProgressBar
		this.currentStepProgressBar = currentStepProgressBar
	}
	
	@Override
	public log(String msg, int severity) {
		if(severity == -1 || severity >= verbosity) {
			outputArea.append("\n" + '   '.multiply(nestingLevel) + msg.toString());
		}
	}
	
	
	@Override
	public log(String msg) { log(msg, -1) }

	@Override
	public setGlobalStepCount(int value) {
		globalProgressBar.setMaximum(value)
	}

	@Override
	public globalStep() {
		globalProgressBar.setValue(globalProgressBar.getValue() + 1)
		resetSteps();
	}
	
	@Override
	public resetGlobalSteps() {
		globalProgressBar.setValue(0);
	}

	@Override
	public setStepCount(int value) {
		currentStepProgressBar.setMaximum(value)
	}

	@Override
	public step() {
		currentStepProgressBar.setValue(currentStepProgressBar.getValue() + 1)
	}

	@Override
	public int getStepCount() {
		return currentStepProgressBar.getMaximum();
	}

	@Override
	public resetSteps() {
		currentStepProgressBar.setValue(0);
	}

	


}
