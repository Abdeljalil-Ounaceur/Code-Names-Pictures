
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Game {
  String defaultGateway = "192.168.181.135"; //change this to suite your own smartphone hotspot IP
	
  JButton[] buttonList = new JButton[20];
  ImageIcon[] picturesList = new ImageIcon[20];
  JButton newGameButton, reconnectButton;
  JLabel topPanelLabel, playerLabel, activePlayerLabel, remainingRedCardsLabel,
      remainingBlueCardsLabel, remainingRedCardsNumLabel, remainingBlueCardsNumLabel,
      wordLabel, activeWordLabel, cardNumberLabel, activeCardNumberLabel, timerLabel;
  JFrame frame;
  JPanel centerPanel, rightSidePanel, leftSidePanel, topPanel;
  int player = 0;
  SpyCard selectedSpyCard = null;
  String agentsDirectory = "../res/image_files/agents/";

  int pictureWidth, pictureHeight;
  Dimension size;
  int RemainingRedCards, RemainingBlueCards, activePlayer, nOfRemainingPickedCards;
  int globalSequence = 0, timer, min, sec;
  String pattern = "", responce;
  Socket s;
  DataInputStream din;
  DataOutputStream dout;

  Socket speakerSocket, listenerSocket;
  DataInputStream speakerDin, listenerDin;
  DataOutputStream speakerDout, listenerDout;
  boolean listenerConnected = false, speakerConnected = false;
  int globalCountDownSequence = 0;
  Thread countDownT;
  int topPanelHeight, currentX;
  int frameWidth, frameHeight;
  int globalSoundToPlay = -1;
  AudioControl audioControl;
  boolean receiptConfirmation = true, listeningSuccess = true, reconnect = false;

  Game() throws Exception {

    audioControl = new AudioControl();
    System.out.println("Audio control finished");

    launchSpeakerClient();
    launchListenerClient();

    size = Toolkit.getDefaultToolkit().getScreenSize();
    pictureWidth = (int) (Math.floor(size.getWidth() / 5) * 0.95);
    pictureHeight = (int) (Math.floor(size.getHeight() / 4) * 0.9);

    frame = new JFrame();
    centerPanel = new JPanel();
    rightSidePanel = new JPanel();
    leftSidePanel = new JPanel();
    topPanel = new JPanel();
    topPanelLabel = new JLabel();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize((int) (size.getWidth() * 0.9), (int) (size.getHeight() * 0.9));
    frame.setLayout(new BorderLayout());

    centerPanel.setLayout(new GridLayout(4, 5));
    topPanel.setLayout(null);

    frameWidth = frame.getWidth();
    frameHeight = frame.getHeight();

    centerPanel.setPreferredSize(new Dimension((int) (frameWidth * 0.98), (int) frameHeight));
    rightSidePanel.setPreferredSize(new Dimension((int) (frameWidth * 0.01), (int) frameHeight));
    leftSidePanel.setPreferredSize(new Dimension((int) (frameWidth * 0.01), (int) frameHeight));

    topPanelHeight = (int) (frame.getHeight() * 0.06);
    topPanel.setPreferredSize(new Dimension(frame.getWidth(), topPanelHeight));

    topPanelLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 55));
    topPanelLabel.setForeground(Color.white);
    topPanel.add(topPanelLabel);

    initialiseBoard();
    while (!listenerConnected || !speakerConnected)
      Thread.sleep(1000);
    newGame();

    frame.add(centerPanel, BorderLayout.CENTER);
    frame.add(rightSidePanel, BorderLayout.EAST);
    frame.add(leftSidePanel, BorderLayout.WEST);
    frame.add(topPanel, BorderLayout.NORTH);
    frame.setVisible(true);

  }

  private void launchSpeakerClient() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        while (true) {
          speakerConnected = false;
          while (true) {
            try {
              Thread.sleep(400);
              System.out.println("speaker : creating speakerSocket");
              speakerSocket = new Socket(defaultGateway, 4100);
              System.out.println("speaker : socket created successfully");
              speakerDin = new DataInputStream(speakerSocket.getInputStream());
              speakerDout = new DataOutputStream(speakerSocket.getOutputStream());
              speakerConnected = true;
              reconnect = false;
              break;

            } catch (Exception e) {
              System.out.println("speaker : " + e);
            }
          }

          while (!listenerConnected) {
            try {
              Thread.sleep(400);
            } catch (InterruptedException ignored) {
            }
            System.out.println("speaker : waiting for listenerSocket to connect...");
          }

          int localSequence = globalSequence;

          while (true) {
            try {
              Thread.sleep(400);
              if (reconnect) {
                break;
              }
              if (!receiptConfirmation) {
                globalSequence++;
              }
              System.out.println("-");
              if (localSequence != globalSequence) {
                localSequence++;
                speakerDout.writeUTF(pattern);
                speakerDout.flush();
                System.out.println("speaker: sent " + pattern);
                while (true) {
                  try {
                    responce = speakerDin.readUTF();
                    System.out.println("speaker : listener responded by " + responce);
                    receiptConfirmation = true;
                    break;
                  } catch (Exception ee) {
                    System.out.println("speaker ee : " + ee);
                    receiptConfirmation = false;
                    break;
                  }
                }
              }

            } catch (Exception e) {
              System.out.println("speaker e : " + e);

            }
            if (!receiptConfirmation || !listeningSuccess) {
              break;
            }
          }
        }

      }
    }).start();
  }

  private void launchListenerClient() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        while (true) {
          listenerConnected = false;
          while (true) {
            try {
              Thread.sleep(400);
              System.out.println("listener : creating listenerSocket");
              listenerSocket = new Socket(defaultGateway, 4200);
              System.out.println("listener : socket created successfully");
              listenerDin = new DataInputStream(listenerSocket.getInputStream());
              listenerDout = new DataOutputStream(listenerSocket.getOutputStream());
              listenerConnected = true;
              listeningSuccess = true;
              reconnect = false;
              break;

            } catch (Exception e) {
              System.out.println("listener : " + e);

            }
          }

          while (!speakerConnected) {
            try {
              Thread.sleep(400);
            } catch (InterruptedException ignored) {
            }
            System.out.println("listener : waiting for speakerSocket to connect...");
          }

          String data = null;

          while (true) {
            try {
              while (true) {
                try {
                  Thread.sleep(400);
                  System.out.println(".");
                  data = listenerDin.readUTF();
                  System.out.println("listener : received " + data);
                  listeningSuccess = true;
                  break;
                } catch (Exception ee) {
                  System.out.println("listener ee : " + ee);
                  listeningSuccess = false;
                  break;
                }
              }
              if (!listeningSuccess || !receiptConfirmation || reconnect)
                break;
              listenerDout.writeUTF("ok");
              listenerDout.flush();
              System.out.println("sent reception confirmation");

              nOfRemainingPickedCards = data.charAt(0);
              if (nOfRemainingPickedCards == 'x') {
                switchPlayer();
                continue;
              }
              nOfRemainingPickedCards -= '0';
              String word = data.substring(1);
              activeWordLabel.setText(word);
              activeCardNumberLabel.setText(String.valueOf(nOfRemainingPickedCards));
              setAllButtonsEnabled(true);
              stopCounting();
              countDown();
            } catch (Exception e) {
              System.out.println("listener e : " + e);
            }
          }
        }

      }
    }).start();
  }

  void renderRandomImages() {
    int[] imageNums = new int[20]; // To store 20 distinct random image indices

    // Generate 20 distinct random indices between 1 and 262
    for (int i = 0; i < 20; i++) {
        imageNums[i] = 1 + (int) (262 * Math.random());
        for (int j = 0; j < i; j++) {
            if (imageNums[i] == imageNums[j]) {
                i--; // Regenerate the current index if it's not unique
                break;
            }
        }
    }

    // Rendering the images using the values in the array
    for (int i = 0; i < 20; i++) {
        String b = imageNums[i] < 10 ? "00" : (imageNums[i] < 100 ? "0" : "");
        String path = "../res/image_files/pictures/" + b + imageNums[i] + ".jpg";

        Image dimg = null;
        try {
            dimg = ImageIO.read(new File(path)).getScaledInstance(pictureWidth, pictureHeight, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace(); // Add a trace for debugging if an image fails to load
        }
        picturesList[i] = new ImageIcon(dimg);
        buttonList[i].setIcon(picturesList[i]);
        buttonList[i].setName(String.valueOf(i));
    }
  }


  void initialiseBoard() {

    currentX = (int) (frameWidth * 0.01);

    for (int i = 0; i < 20; i++) {
      buttonList[i] = new JButton();

      buttonList[i].setName(String.valueOf(i));
      buttonList[i].addActionListener(e -> reveal(Integer.parseInt(((JButton) e.getSource()).getName())));
      centerPanel.add(buttonList[i]);
    }

    playerLabel = new JLabel("Team");
    playerLabel.setFont(new Font(null, Font.ITALIC, 25));
    playerLabel.setForeground(Color.white);
    addToTopPanel(playerLabel, 8);

    activePlayerLabel = new JLabel();
    activePlayerLabel.setFont(new Font(null, Font.BOLD, 25));
    activePlayerLabel.setForeground(Color.white);
    activePlayerLabel.setBounds(new Rectangle());
    addToTopPanel(activePlayerLabel, 2);

    wordLabel = new JLabel("| Word :");
    wordLabel.setFont(new Font(null, Font.ITALIC, 25));
    wordLabel.setForeground(Color.white);
    addToTopPanel(wordLabel, 8);

    activeWordLabel = new JLabel("----");
    activeWordLabel.setFont(new Font(null, Font.BOLD, 25));
    activeWordLabel.setForeground(Color.white);
    addToTopPanel(activeWordLabel, 23);

    cardNumberLabel = new JLabel("| Find :");
    cardNumberLabel.setFont(new Font(null, Font.ITALIC, 25));
    cardNumberLabel.setForeground(Color.white);
    addToTopPanel(cardNumberLabel, 10);

    activeCardNumberLabel = new JLabel("-");
    activeCardNumberLabel.setFont(new Font(null, Font.BOLD, 30));
    activeCardNumberLabel.setForeground(Color.white);
    addToTopPanel(activeCardNumberLabel, 5);

    remainingRedCardsLabel = new JLabel("| Red : ");
    remainingRedCardsLabel.setFont(new Font(null, Font.ITALIC, 25));
    remainingRedCardsLabel.setForeground(Color.white);
    addToTopPanel(remainingRedCardsLabel, 10);

    remainingRedCardsNumLabel = new JLabel();
    remainingRedCardsNumLabel.setFont(new Font(null, Font.BOLD, 30));
    remainingRedCardsNumLabel.setForeground(Color.white);
    addToTopPanel(remainingRedCardsNumLabel, 5);

    remainingBlueCardsLabel = new JLabel("| Blue : ");
    remainingBlueCardsLabel.setFont(new Font(null, Font.ITALIC, 25));
    remainingBlueCardsLabel.setForeground(Color.white);
    addToTopPanel(remainingBlueCardsLabel, 10);

    remainingBlueCardsNumLabel = new JLabel();
    remainingBlueCardsNumLabel.setFont(new Font(null, Font.ITALIC, 30));
    remainingBlueCardsNumLabel.setForeground(Color.white);
    addToTopPanel(remainingBlueCardsNumLabel, 5);

    timerLabel = new JLabel("<Time>");
    timerLabel.setFont(new Font(null, Font.ITALIC, 18));
    timerLabel.setForeground(Color.white);
    addToTopPanel(timerLabel, 10);

    newGameButton = new JButton("New Game");
    newGameButton.setFont(new Font(null, Font.BOLD, 15));
    newGameButton.setBackground(Color.white);
    newGameButton.setFocusable(false);
    newGameButton.addActionListener(e -> newGame());
    addToTopPanel(newGameButton, 10);

    reconnectButton = new JButton("<|>");
    reconnectButton.setFont(new Font(null, Font.BOLD, 11));
    reconnectButton.setBackground(Color.white);
    reconnectButton.setFocusable(false);
    reconnectButton.addActionListener(e -> reconnect());
    addToTopPanel(reconnectButton, 4);

    topPanel.add(playerLabel);
    topPanel.add(activePlayerLabel);
    topPanel.add(wordLabel);
    topPanel.add(activeWordLabel);
    topPanel.add(cardNumberLabel);
    topPanel.add(activeCardNumberLabel);
    topPanel.add(remainingRedCardsLabel);
    topPanel.add(remainingRedCardsNumLabel);
    topPanel.add(remainingBlueCardsLabel);
    topPanel.add(remainingBlueCardsNumLabel);
    topPanel.add(timerLabel);
    topPanel.add(newGameButton);
    topPanel.add(reconnectButton);
  }

  void reconnect() {
    try {
      speakerSocket.close();
      listenerSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    reconnect = true;
    new Thread(new Runnable() {

      @Override
      public void run() {
        int i = 0;
        while (!listenerConnected || !speakerConnected) {
          reconnectButton.setText("." + (i >= 1 ? "." : "") + (i == 2 ? "." : ""));
          try {
            Thread.sleep(300);
          } catch (InterruptedException e) {
          }
          i = (++i) % 3;
        }
        reconnectButton.setText("<|>");
      }
    }).start();

  }

  void newGame() {

    // topPanel.setVisible(false);
    setAllButtonsEnabled(false);

    try {
      selectedSpyCard = Main.loadSpyCard(1 + (int) (27 * Math.random()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    RemainingBlueCards = RemainingRedCards = 7;
    if (selectedSpyCard.firstPlayer == 1)
      RemainingRedCards++;
    else
      RemainingBlueCards++;
    activePlayer = selectedSpyCard.firstPlayer;

    sendDataToPhone();

    if (selectedSpyCard.firstPlayer == 1) {
      setPlayerColor(Color.red);
    } else
      setPlayerColor(Color.blue);

    renderRandomImages();

    activePlayerLabel.setText(String.valueOf(selectedSpyCard.firstPlayer));
    remainingRedCardsNumLabel.setText(String.valueOf(RemainingRedCards));
    remainingBlueCardsNumLabel.setText(String.valueOf(RemainingBlueCards));

    setAllButtonsEnabled(false);

  }

  void loadSpyCardColors(int index) throws Exception {
    SpyCard sc = Main.loadSpyCard(index);
    if (sc == null)
      return;
    Color c = null;
    for (int i = 0; i < 20; i++) {
      switch (sc.pictureTypeTab[i]) {
        case 1:
          c = Color.red;
          break;
        case 2:
          c = Color.blue;
          break;
        case 3:
          c = Color.yellow;
          break;
        case 4:
          c = Color.black;
          break;
      }
      buttonList[i].setBackground(c);
    }

  }

  void reveal(int index) {
    int agentNum = 0;
    switch (selectedSpyCard.pictureTypeTab[index]) {
      case 1:
        agentNum = ((int) (Math.random() * 3)) + 5;
        break;
      case 2:
        agentNum = ((int) (Math.random() * 3)) + 2;
        break;
      case 3:
        agentNum = ((int) (Math.random() * 2));
        break;
      case 4:
        agentNum = 8;
        break;
    }
    Image dimg = null;
    try {
      dimg = ImageIO.read(new File(agentsDirectory + agentNum + ".jpg"))
          .getScaledInstance(pictureWidth, pictureHeight, Image.SCALE_SMOOTH);
    } catch (IOException e) {
    }
    ImageIcon img = new ImageIcon(dimg);
    buttonList[index].setIcon(img);
    buttonList[index].setDisabledIcon(img);
    buttonList[index].setEnabled(false);
    buttonList[index].setName("revealed");
    ;
    checkForWinAndSwitch(index);
  }

  void checkForWinAndSwitch(int index) {
    switch (selectedSpyCard.pictureTypeTab[index]) {
      case 1:
        remainingRedCardsNumLabel.setText(String.valueOf(--RemainingRedCards));
        if (RemainingRedCards == 0) {
          gameOver(1, true);
          return;
        }
        if (activePlayer == 2) {
          audioControl.playSound(1);
          switchPlayer();
        } else {
          audioControl.playSound(0);
          activeCardNumberLabel.setText(String.valueOf(--nOfRemainingPickedCards));
          ;
          if (nOfRemainingPickedCards == 0)
            switchPlayer();
        }

        break;
      case 2:
        remainingBlueCardsNumLabel.setText(String.valueOf(--RemainingBlueCards));
        if (RemainingBlueCards == 0) {
          gameOver(2, true);
          return;
        }
        if (activePlayer == 1) {
          audioControl.playSound(1);
          switchPlayer();
        } else {
          audioControl.playSound(0);
          activeCardNumberLabel.setText(String.valueOf(--nOfRemainingPickedCards));
          ;
          if (nOfRemainingPickedCards == 0)
            switchPlayer();
        }
        break;
      case 3:
        audioControl.playSound(1);
        switchPlayer();
        break;
      case 4:
        if (activePlayer == 1)
          gameOver(2, false);
        else
          gameOver(1, false);
        break;
    }

  }

  void gameOver(int winner, boolean win) {
    setAllButtonsEnabled(false);
    stopCounting();
    if (win)
      audioControl.playSound(3);
    else
      audioControl.playSound(2);

    new Thread(new Runnable() {

      @Override
      public void run() {
        Color winnerColor = winner == 1 ? Color.red : Color.red;
        Color winOrloseColor = win ? new Color(155, 215, 0) : new Color(0, 0, 0);
        for (int i = 0; i < 50; i++) {
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          setPlayerColor((i % 2) == 0 ? winOrloseColor : winnerColor);
        }

      }
    }).start();

  }

  void switchPlayer() {
    Color c = null;
    if (activePlayer == 1) {
      activePlayer = 2;
      c = Color.blue;
    } else {
      activePlayer = 1;
      c = Color.red;
    }

    setPlayerColor(c);

    activePlayerLabel.setText(String.valueOf(activePlayer));

    setAllButtonsEnabled(false);
    stopCounting();

    try {
      sendDataToPhone();
    } catch (Exception e) {
      e.printStackTrace();
    }

    activeWordLabel.setText("---");
    activeCardNumberLabel.setText("-");
  }

  private void setPlayerColor(Color c) {
    rightSidePanel.setBackground(c);
    leftSidePanel.setBackground(c);
    topPanel.setBackground(c);
  }

  void sendDataToPhone() {
    pattern = ""
        + activePlayer
        + RemainingRedCards
        + RemainingBlueCards
        + selectedSpyCard.spyCardNumber;
    globalSequence++;
  }

  void setAllButtonsEnabled(boolean b) {
    for (int i = 0; i < 20; i++) {
      buttonList[i].setDisabledIcon(buttonList[i].getIcon());
      if (!buttonList[i].getName().equals("revealed"))
        buttonList[i].setEnabled(b);
    }
  }

  void countDown() {
    timer = 180;
    countDownT = new Thread(() -> {
      int localCountdownSequence = globalCountDownSequence;
      while (localCountdownSequence == globalCountDownSequence && timer > 0) {
        timer -= 1;
        if (timer <= 0) {
          System.out.println("your time is up");
          break;
        }
        min = timer / 60;
        sec = timer % 60;
        timerLabel.setText("0" + min + ":" + (sec < 10 ? "0" : "") + sec);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
      }
      if (timer <= 0) {
        setAllButtonsEnabled(false);
        switchPlayer();
      }

    });
    countDownT.start();
    System.out.println("started countdown");
  }

  void stopCounting() {
    globalCountDownSequence++;
    timerLabel.setText("<Time>");
  }

  void addToTopPanel(JComponent item, double widthPercent) {
    int width = (int) ((frameWidth * widthPercent) / 100);
    item.setBounds(currentX, 0, width, topPanelHeight);
    currentX += width;
  }

}
