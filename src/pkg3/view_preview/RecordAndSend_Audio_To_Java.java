package pkg3.view_preview;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.xiph.speex.SpeexEncoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class RecordAndSend_Audio_To_Java implements Runnable {

	// pour envoyer le son a java
	public DatagramSocket socketToSend_Audio_To_JavaPatient;
	private String ipOfPatient_Java;
	private int portToSendAudioToJava;
	AudioRecord recorderToSend = null;

	private int sampleRateToSend = 8000;
	// private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int channelConfigToSend = AudioFormat.CHANNEL_IN_MONO;
	private int audioFormatToSend = AudioFormat.ENCODING_PCM_16BIT;

	private boolean isStreamming_Images_Audio = false; // verifier statut du
														// streaming apres
														// appeler ou accepter
														// un appel
	SpeexEncoder encoder;
	int sample_size = 16;
	int channels = 1;

	public RecordAndSend_Audio_To_Java(String ipOfPatient_Java,
			int portToSendAudioToJava, boolean isStreamming_Images_Audio) {
		// TODO Auto-generated constructor stub
		this.ipOfPatient_Java = ipOfPatient_Java;
		this.portToSendAudioToJava = portToSendAudioToJava;
		this.isStreamming_Images_Audio = isStreamming_Images_Audio;
	}

	public void stopRecordAndSendAudio() {
		isStreamming_Images_Audio = false;
		
		if (socketToSend_Audio_To_JavaPatient != null) {
			socketToSend_Audio_To_JavaPatient.close();
		}

		recorderToSend.release();
		recorderToSend = null;
	}

	@Override
	public void run() {
		try {

			socketToSend_Audio_To_JavaPatient = new DatagramSocket();
			Log.d("VS", "Socket Created");

			final InetAddress destination = InetAddress
					.getByName(ipOfPatient_Java);
			Log.d("VS", "Address retrieved");

			int minBufSize = AudioRecord.getMinBufferSize(sampleRateToSend,
					channelConfigToSend, audioFormatToSend);

			recorderToSend = new AudioRecord(
					MediaRecorder.AudioSource.VOICE_RECOGNITION,
					sampleRateToSend, channelConfigToSend, audioFormatToSend,
					minBufSize);
			Log.d("VS", "Recorder initialized");

			DatagramPacket packet;

			encoder = new SpeexEncoder();

			encoder.init(1, 1, sampleRateToSend, 1);

			int raw_block_size = encoder.getFrameSize() * channelConfigToSend
					* (sample_size / 8);

			byte[] buffer = new byte[raw_block_size];

			recorderToSend.startRecording();

			while (isStreamming_Images_Audio == true) {

				// reading data from MIC into buffer
				int read = recorderToSend.read(buffer, 0, buffer.length);
				// Log.i("VS", "Buffer created of size "
				// + minBufSize);
				//
				// Log.i("VS", "Bytes read: " + minBufSize);

				if (!encoder.processData(buffer, 0, raw_block_size)) {
					System.err.println("Could not encode data!");
					break;
				}

				// putting buffer in the packet
				int encoded = encoder.getProcessedData(buffer, 0);

				//Log.i("VS", "After compress: " + encoded);

				byte[] encoded_data = new byte[encoded];

				System.arraycopy(buffer, 0, encoded_data, 0, encoded);

				packet = new DatagramPacket(encoded_data, encoded_data.length,
						destination, portToSendAudioToJava);

				socketToSend_Audio_To_JavaPatient.send(packet);

//				Log.i("Send audio", "The number of bytes: "
//						+ packet.getData().length);

				// in here

				Thread.sleep(10);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
