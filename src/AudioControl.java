import java.io.IOException;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioControl implements LineListener {

  boolean isPlaybackCompleted;
  String audioDirectory = "../res/audio_files/";
  String fileNames[] = { "success", "quack", "fail", "congrats" };
  AudioInputStream[] audioStreams;
  Clip[] audioClips;

  @Override
  public void update(LineEvent event) {
    if (LineEvent.Type.START == event.getType()) {
      System.out.println("Playback started.");
    } else if (LineEvent.Type.STOP == event.getType()) {
      isPlaybackCompleted = true;
      System.out.println("Playback completed.");
    }
  }

  public AudioControl()
      throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
    audioStreams = new AudioInputStream[4];
    audioClips = new Clip[4];
    System.out.println("Audio directory: " + audioDirectory);
    for (int i = 0; i < 4; i++) {
        File audioFile = new File(audioDirectory + fileNames[i] + ".wav");
        audioStreams[i] = AudioSystem.getAudioInputStream(audioFile);
        audioClips[i] = (Clip) ((Line) AudioSystem.getLine(
            new DataLine.Info(
                Clip.class, audioStreams[i].getFormat())));
        audioClips[i].open(audioStreams[i]);
        audioClips[i].addLineListener(this);
    }
  }

  public void playSound(int type) {
    Clip audioClip;
    audioClip = audioClips[type];
    try {
      audioClip.stop();
    } catch (Exception ignored) {
    }
    audioClip.setFramePosition(0);
    audioClip.start();
  }
}