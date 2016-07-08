package message;
public class SnapshotMessage extends Message {

	private static final long serialVersionUID = 8160400252661197360L;
	private int seqNumber;
	private boolean isActive;
	private int[] vectorClock;
	private boolean isChannelEmpty;

	public SnapshotMessage(int srcVal, int destVal, int seqNumVal,
			boolean isActive, int[] vcVal, boolean isChannelEmpty) {
		super(srcVal, destVal);
		this.seqNumber = seqNumVal;
		this.isActive = isActive;
		this.vectorClock = vcVal;
		this.isActive = isChannelEmpty;
	}

	public int getSeqNumber() {
		return seqNumber;
	}

	public boolean isActive() {
		return isActive;
	}

	public int[] getVectorClock() {
		return vectorClock;
	}

	public boolean isChannelEmpty() {
		return isChannelEmpty;
	}

	public void setChannelEmpty(boolean empty) {
		isChannelEmpty = empty;
	}
}
