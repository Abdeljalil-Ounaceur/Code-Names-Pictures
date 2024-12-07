import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class Main {

  public static void main(String[] args) throws Exception {
    new Game();
  }

  public static SpyCard loadSpyCard(int index) throws Exception {
    File file = new File("../res/spyCards.txt");
    if (!file.exists() || file.length() == 0) {
      throw new Exception("File is empty or does not exist");
    }

    try (FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      SpyCard card;
      while (fis.available() > 0) {
        card = (SpyCard) ois.readObject();
        if (card.spyCardNumber == index) {
          return card;
        }
      }
    } catch (Exception e) {
      throw new Exception("Error reading from file", e);
    }

    throw new Exception("SpyCard not found");
  }

}
