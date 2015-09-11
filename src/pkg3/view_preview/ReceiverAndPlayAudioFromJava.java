package pkg3.view_preview;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.xiph.speex.SpeexDecoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class ReceiverAndPlayAudioFromJava implements Runnable {

	// pour recevoir le son de Java
	private DatagramSocket socketToReceiveAndPlay_Audio_From_JavaPatient;
	private int portToReceiveAudioFromJava;
	boolean isStreamming_Images_Audio;
	private AudioTrack speaker;
	int sample_rate_ToReceiveAudio = 8000; // ou 16000
	// How much will be ideal?
	// sender and receiver must have the same value
	// Audio Configuration.

	int sample_size = 16;
	int channels = 1;

	private int channelConfigToReceive = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormatToReceive = AudioFormat.ENCODING_PCM_16BIT;

	public ReceiverAndPlayAudioFromJava(int portToReceiveAudioFromJava,
			boolean isStreamming_Images_Audio) {
		// TODO Auto-generated constructor stub
		this.portToReceiveAudioFromJava = portToReceiveAudioFromJava;
		this.isStreamming_Images_Audio = isStreamming_Images_Audio;
	}

	public void stopReceiveAndStopPlayAudio() {
		isStreamming_Images_Audio = false;

		if (socketToReceiveAndPlay_Audio_From_JavaPatient != null) {
			socketToReceiveAndPlay_Audio_From_JavaPatient.close();
		}
		speaker.flush();
		speaker.release();
		speaker = null;

	}

	@Override
	public void run() {

		try {

			socketToReceiveAndPlay_Audio_From_JavaPatient = new DatagramSocket(
					portToReceiveAudioFromJava);
			Log.d("VR", "Socket Created");

			// byte[] buffer = new byte[4096];
			byte[] buffer = new byte[15];

			// minimum buffer size. need to be careful. might
			// cause
			// problems. try setting manually if any problems
			// faced
			int minBufSize = AudioRecord.getMinBufferSize(
					sample_rate_ToReceiveAudio, channelConfigToReceive,
					audioFormatToReceive);

			speaker = new AudioTrack(AudioManager.STREAM_MUSIC,
					sample_rate_ToReceiveAudio, channelConfigToReceive,
					audioFormatToReceive, minBufSize, AudioTrack.MODE_STREAM);

			speaker.play();

			SpeexDecoder decoder = new SpeexDecoder();

			decoder.init(1, sample_rate_ToReceiveAudio, channels);

			while (isStreamming_Images_Audio) {
				try {

					DatagramPacket packet = new DatagramPacket(buffer,
							buffer.length);
					socketToReceiveAndPlay_Audio_From_JavaPatient
							.receive(packet);
					//Log.i("VR", "Packet Received");

					// reading content from packet
					buffer = packet.getData();
					Log.d("VR", "Packet data read into buffer");

					// decompress:
					decoder.processData(buffer, 0, buffer.length);
					//
					byte[] decoded_data = new byte[decoder
							.getProcessedDataByteSize()];
					int decoded = decoder.getProcessedData(decoded_data, 0);

					// sending data to the Audiotrack obj i.e.
					// speaker
					// speaker.write(buffer, 0, minBufSize);
					speaker.write(decoded_data, 0, decoded_data.length);
					Log.d("VR", "Writing buffer content to speaker");

					Thread.sleep(10);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
