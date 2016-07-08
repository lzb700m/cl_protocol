package message;
public class MarkerMessage extends Message {
	private static final long serialVersionUID = -4095719536115291717L;
	private int seqNumber;

	public MarkerMessage(int srcVal, int destVal, int seqNumVal) {
		super(srcVal, destVal);
		this.seqNumber = seqNumVal;
	}

	public int getSeqNumber() {
		return seqNumber;
	}
}
