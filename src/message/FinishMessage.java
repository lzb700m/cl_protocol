package message;

public class FinishMessage extends Message {

	private static final long serialVersionUID = -6279684221102050079L;

	// private static final String S = "POISON";

	public FinishMessage(int srcVal, int destVal) {
		super(srcVal, destVal);
	}
}
