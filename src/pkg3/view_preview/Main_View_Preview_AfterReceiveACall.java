package pkg3.view_preview;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import pkg1.main_home_screen.R;
import pkg6.entities.General_Infos;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Main_View_Preview_AfterReceiveACall extends Activity {

	// declaration pour appeler ou recevoir un msg d'appel de java
	// declaration pour appeler au java

	private ImageButton btnStop;

	// private MessageHandler messageHandler;
	private int timeOutWaitResponse = 19; // en secondes
	private Thread threadToCalculeTimesWaitResponse = null;
	private boolean isReceivedFromJava = false;
	private boolean canBeCallerToJava = true;

	private int timeOut = 19; // en secondes
	private int intervalBlink = 200; // en milisecondes
	private boolean isAcceptedCommunication = false;
	private boolean stopBybtnStop = false;

	private MediaPlayer mediaPlayer;

	// finir la declaration pour la fonction d'appel

	// declaration pour envoyer et recevoir images
	// private String ipOfPatient_Java = "192.168.1.57";
	// private String ipOfPatient_Java = "192.168.1.52";
	// private String ipOfPatient_Java = "10.30.80.90";
	// private String ipOfPatient_Java = "192.168.1.13";
	//private String ipOfPatient_Java = "192.168.43.91";
	//private String ipOfPatient_Java = "192.168.43.1";
	private String ipOfPatient_Java = General_Infos.IP_OF_OTHER_SIDE;
	
	//private String ipOfPatient_Java = "192.168.1.47";
	//private String ipOfPatient_Java = "192.168.1.63";

	private CameraPreview camPreview;
	private ImageView MyCameraPreview = null;
	private FrameLayout mainLayout;
	private int PreviewSizeWidth = 640; // 640 program run, 200 run on lg
	
	private int PreviewSizeHeight = 480; // 480 program run, 300 run on lg

	DatagramSocket socketToSend_Images_ToJava = null;
	//int portToSendImagesToJava = 44449;
	// here
	int portToSendImagesToJava = General_Infos.PORT_FOR_IMAGES;

	// declaration pour recevoir le video de java_patient
	DatagramSocket socketToReceive_Images = null;
	DatagramPacket packetToReceive_Images = null;

	Thread threadToReceive_Images = null;
	int portToReceiveImageFromJava = General_Infos.PORT_FOR_IMAGES;
	static final int SIZE_OF_BUF_TO_RECEIVE_IMAGES = 22000;
	

	Bitmap bitmap;
	ImageView imageView;
	// ViewGroup layout;

	// pour envoyer le son a java
	RecordAndSend_Audio_To_Java recorderAndSenderAudio;
	private int portToSendAudioToJava = General_Infos.PORT_FOR_AUDIO;

	private boolean isStreamming_Images_Audio = false; // verifier statut du
														// streaming apres
														// appeler ou accepter
														// un appel
	Thread threadToRecordAndSend_Audio_To_Java_Patient = null;

	// pour recevoir le son de Java
	//private int portToReceiveAudioFromJava = 50011;
	private int portToReceiveAudioFromJava = General_Infos.PORT_FOR_AUDIO;
	
	ReceiverAndPlayAudioFromJava receiverAndPlayerAudio;
	Thread threadToReceiveAndPlay_Audio_From_JavaPatient = null;

	private MessageHandler messageHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set this APK Full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Set this APK no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// l'interface pour audio

		setContentView(R.layout.view_preview_after_send_receive_acall);

		isStreamming_Images_Audio = false;
		// startReceiving();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int heightOfSreen = displaymetrics.heightPixels;
		int widthOfScreen = displaymetrics.widthPixels;

		btnStop = (ImageButton) findViewById(R.id.stop_button);
		btnStop.setMinimumWidth(widthOfScreen / 5);
		btnStop.setMaxWidth(widthOfScreen / 5);
		btnStop.setMinimumHeight(heightOfSreen / 16);
		btnStop.setMaxHeight(heightOfSreen / 16);

		btnStop.setOnClickListener(stopListener);

		// pour envoyer et recevoir images
		// Create my camera preview
		//
		MyCameraPreview = new ImageView(this);

		SurfaceView camView = new SurfaceView(this);
		SurfaceHolder camHolder = camView.getHolder();

		camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight,
				MyCameraPreview, ipOfPatient_Java, portToSendImagesToJava);

		camHolder.addCallback(camPreview);
		camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mainLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mainLayout.addView(camView, new LayoutParams(PreviewSizeWidth,
				PreviewSizeHeight));
		mainLayout.addView(MyCameraPreview, new LayoutParams(PreviewSizeWidth,
				PreviewSizeHeight));

		// Interface pour recevoir video de Java_patient
		imageView = (ImageView) findViewById(R.id.imageView);

		messageHandler = new MessageHandler();

		isReceivedFromJava = false;
		canBeCallerToJava = true;

		isAcceptedCommunication = true;
		// ouvrir all threads pour la communication video-audio
		isStreamming_Images_Audio = true;

		try {
			socketToSend_Images_ToJava = new DatagramSocket();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		camPreview
				.setSocketToSend_Images_To_JavaPatient(socketToSend_Images_ToJava);

		camPreview.setStatus(isStreamming_Images_Audio);

		receiverAndPlayerAudio = new ReceiverAndPlayAudioFromJava(
				portToReceiveAudioFromJava, isStreamming_Images_Audio);
		threadToReceiveAndPlay_Audio_From_JavaPatient = new Thread(
				receiverAndPlayerAudio);
		threadToReceiveAndPlay_Audio_From_JavaPatient.start();

		// open threads for images
		threadToReceive_Images = new Thread(new ReceiverBytesFromJava_Patient());
		// threadToReceive_Images.setPriority(Thread.MAX_PRIORITY);
		threadToReceive_Images.start();

		recorderAndSenderAudio = new RecordAndSend_Audio_To_Java(
				ipOfPatient_Java, portToSendAudioToJava,
				isStreamming_Images_Audio);
		threadToRecordAndSend_Audio_To_Java_Patient = new Thread(
				recorderAndSenderAudio);
		threadToRecordAndSend_Audio_To_Java_Patient.start();

	}

	private final OnClickListener stopListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			// arreter la communication (streaming)
			if (isStreamming_Images_Audio == true) {

				btnStop.setEnabled(false);

				isStreamming_Images_Audio = false;

				recorderAndSenderAudio.stopRecordAndSendAudio();
				threadToRecordAndSend_Audio_To_Java_Patient = null;

				receiverAndPlayerAudio.stopReceiveAndStopPlayAudio();
				threadToReceiveAndPlay_Audio_From_JavaPatient = null;

				
				socketToReceive_Images.close();
				threadToReceive_Images = null;

				// la fermeture le thread pour envoyer images est realise
				// dans la classe CameraPreview
				socketToSend_Images_ToJava.close();
				// socketToSend_Images_ToJava = null;
				camPreview.setStatus(isStreamming_Images_Audio);
				camPreview.closeSendSocket();
			}

			canBeCallerToJava = true;

			// arreter de recevoir et refuser le msg d'appel de java
			// arreter d'appeler et attendre de la reponse du patient java

			btnStop.setEnabled(false);

			// socketToCallJava.close();
			// threadCallToPatient_Java = null;

			// arreter de play audio et liberer media player
			isAcceptedCommunication = false;

			stopBybtnStop = true;
			// threadForBlinking = null;
			if (mediaPlayer != null) {
				mediaPlayer.release();
			}

			threadToCalculeTimesWaitResponse = null;

			finish();

		}

	};

	protected void onPause() {
		if (camPreview != null)
			camPreview.onPause();
		super.onPause();
	}

	class ReceiverBytesFromJava_Patient implements Runnable {
		public void run() {

			try {

				socketToReceive_Images = new DatagramSocket(
						portToReceiveImageFromJava);

				// modifier par Hien
				//socketToReceive_Images.setSoTimeout(40);

				byte[] receiveBuffer = new byte[SIZE_OF_BUF_TO_RECEIVE_IMAGES];

				while (isStreamming_Images_Audio == true) {
					packetToReceive_Images = new DatagramPacket(receiveBuffer,
							receiveBuffer.length);

					// Log.i("Nombre d'octets peut etre recus: ", ""
					// + receiveBuffer.length);
					socketToReceive_Images.receive(packetToReceive_Images);
					Log.i("Nombre d'octets d'images recus en realite: ", ""
							 + packetToReceive_Images.getLength());

					byte[] byteArray1 = packetToReceive_Images.getData();
					byte[] byteArray2 = new byte[packetToReceive_Images.getLength()];
					
					System.arraycopy(byteArray1, 0, byteArray2, 0, byteArray2.length);

					messageHandler.obtainMessage(106, byteArray2)
							.sendToTarget();
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	}

	public class MessageHandler extends Handler {
		// @SuppressLint("ParserError")
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 106: {
				byte[] byteArray = (byte[]) msg.obj;

				// afficher image depuis table d'octets

				bitmap = BitmapFactory.decodeByteArray((byte[]) byteArray, 0,
						byteArray.length);

				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
					bitmap = null;
				}

				break;
			}

			default: {
				super.handleMessage(msg);
				break;
			}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// arreter tous les threads
		Thread threadToStopAllWhenDestroy = new Thread() {
			public void run() {
				try {

					// arreter la communication (streaming)
					if (isStreamming_Images_Audio == true) {

						btnStop.setEnabled(false);

						isStreamming_Images_Audio = false;

						recorderAndSenderAudio.stopRecordAndSendAudio();
						threadToRecordAndSend_Audio_To_Java_Patient = null;

						receiverAndPlayerAudio.stopReceiveAndStopPlayAudio();
						threadToReceiveAndPlay_Audio_From_JavaPatient = null;

						
						socketToReceive_Images.close();
						threadToReceive_Images = null;

						// la fermeture le thread pour envoyer images est
						// realise
						// dans la classe CameraPreview
						socketToSend_Images_ToJava.close();
						// socketToSend_Images_ToJava = null;
						camPreview.setStatus(isStreamming_Images_Audio);
						camPreview.closeSendSocket();
					}

					canBeCallerToJava = true;

					// arreter de recevoir et refuser le msg d'appel de java
					// arreter d'appeler et attendre de la reponse du patient
					// java

					btnStop.setEnabled(false);

					// socketToCallJava.close();
					// threadCallToPatient_Java = null;

					// arreter de play audio et liberer media player
					isAcceptedCommunication = false;

					stopBybtnStop = true;
					// threadForBlinking = null;
					if (mediaPlayer != null) {
						mediaPlayer.release();
					}

					threadToCalculeTimesWaitResponse = null;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		threadToStopAllWhenDestroy.start();
		try {
			threadToStopAllWhenDestroy.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finish();
	}

}
