package message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 3137889725252292100L;
	int src;
	int dest;

	public Message(int srcVal, int destVal) {
		this.src = srcVal;
		this.dest = destVal;
	}

	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(this);
		return out.toByteArray();
	}

	public int getSrc() {
		return src;
	}

	public int getDest() {
		return dest;
	}
}
