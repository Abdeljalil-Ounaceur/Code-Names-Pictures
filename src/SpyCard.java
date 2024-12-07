import java.io.Serializable;
import java.util.Arrays;

public class SpyCard implements Serializable {

	private static final long serialVersionUID = 1L;
	public int spyCardNumber;
	public  int[] pictureTypeTab = new int[20];
	public int firstPlayer;
	
	@Override
	public String toString() {
		return "SpyCard [spyCardNumber=" + spyCardNumber + ", pictureTypeTab=" + Arrays.toString(pictureTypeTab)
				+ ", firstPlayer=" + firstPlayer + "]";
	}
	
	
}
