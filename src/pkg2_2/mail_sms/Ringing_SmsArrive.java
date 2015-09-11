package pkg2_2.mail_sms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

public class Ringing_SmsArrive implements Runnable {
	private int timeOut = 1;
	private MediaPlayer mediaPlayer;
	private String pathOfMp3File;
	
	public Ringing_SmsArrive(String pathOfMp3File){
		this.pathOfMp3File = pathOfMp3File;
	}

	public void run() {
		try {
			mediaPlayer = new MediaPlayer();
			// String filePath = "/mnt/sdcard/AHien/HaoHanCa1.mp3";
			String filePath = pathOfMp3File;

			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			byte[] audioData = new byte[(int) file.length()];
			fis.read(audioData);

			MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
			metaRetriever.setDataSource(filePath);

			// convert duration to miliseconds
			String duration = metaRetriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

			long dur = Long.parseLong(duration);
			int durInt = (int) (dur / 1000);
			byte[] audioData1 = new byte[(int) (timeOut * file.length() / durInt)];
			System.arraycopy(audioData, 0, audioData1, 0, audioData1.length);

			// play
			playMp3(audioData1);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	private void playMp3(byte[] mp3SoundByteArray) {
		try {
			// create temp file that will hold byte array
			File tempMp3 = File.createTempFile("hien1", "mp3");

			tempMp3.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempMp3);
			fos.write(mp3SoundByteArray);
			fos.close();

			mediaPlayer = new MediaPlayer();

			FileInputStream fis = new FileInputStream(tempMp3);
			mediaPlayer.setDataSource(fis.getFD());

			mediaPlayer.prepare();
			mediaPlayer.start();
			//mediaPlayer.release();
		} catch (IOException ex) {
			String s = ex.toString();
			ex.printStackTrace();
		}
	}
}
