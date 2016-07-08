package message;
public class ApplicationMessage extends Message {

	private static final long serialVersionUID = -8520308843874823566L;
	String content;
	int[] vectorClcok;

	public ApplicationMessage(int srcVal, int destVal, String contentVal,
			int[] vcVal) {
		super(srcVal, destVal);
		this.content = contentVal;
		this.vectorClcok = vcVal;
	}
}
