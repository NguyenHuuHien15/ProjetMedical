package pkg1.main_home_screen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import pkg6.entities.General_Infos;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

public class Main_Home_Screen extends Activity {
	DatePicker datePicker;
	// TimePicker timePicker;

	TextView editTextNote;

	Button btnListenACall;

	ImageButton btnStartCallFunction;
	ImageButton btn_Sms_Mail;
	ImageButton btn_Apps;
//	ImageButton btnFunction4;
//	ImageButton btnFunction5;

	// declaration pour recevoir msg d'appel de java
	private ServerSocket sskReceiveCallFromJava;
	private Socket skForJavaClient;
	private int portReceiveCallFromJava = General_Infos.PORT_FOR_CALL_RECEIVECALL;
	private Thread threadReceiveCallFromJava;
	private Thread threadForRinging;
	private int timeOut = 19; // en secondes
	private int intervalBlink = 200; // en milisecondes
	private boolean isAcceptedCommunication = false;
	private boolean stopBybtnStop = false;
	private boolean canBeCallerToJava = true;
	private MessageHandler messageHandler;

	private MediaPlayer mediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main__home__screen);

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);

		int widthOfScreen = display.widthPixels;
		int heightOfScreen = display.heightPixels;

		datePicker = (DatePicker) findViewById(R.id.datePicker);
		datePicker.setX(0);
		datePicker.setY(10);
		// datePicker.setActivated(false);
		datePicker.setEnabled(false);
		datePicker.setBackgroundColor(Color.TRANSPARENT);

		editTextNote = (TextView) findViewById(R.id.editTextNote);
		editTextNote.setMinHeight(heightOfScreen * 2 / 10);
		editTextNote.setMaxHeight(heightOfScreen * 2 / 10);
		editTextNote.setX(0);
		editTextNote.setY(heightOfScreen * 4 / 10 + 30);
		editTextNote
		.setText(" - 03/07/2015 à 18h: RDV avec Mlle. Iga\n - 03/07/2015 à 19h: Jouer au football "
				+ "\n - 04/07/2015 à 19h: Jouer au volley \n - 04/07/2015 à 20h: Jouer au footsal"
				+ "\n - 05/07/2015 à 21hh: Jouer au football\n - 06/07/2015 à 19h: Jouer au football");
		// editTextNote.setEnabled(false);
		editTextNote.setMovementMethod(new ScrollingMovementMethod());

		btnListenACall = (Button) findViewById(R.id.btnListenACall);
		btnListenACall.setMinimumWidth((int) (widthOfScreen));
		btnListenACall.setMaxWidth((int) (widthOfScreen));
		btnListenACall.setX(0);
		btnListenACall.setY(heightOfScreen - 190 - 150);
		btnListenACall.setEnabled(false);
		//btnListenACall.setVisibility(View.INVISIBLE);

		btnStartCallFunction = (ImageButton) findViewById(R.id.btn_StartCallFunction);
		btnStartCallFunction.setMinimumHeight(heightOfScreen / 10);
		btnStartCallFunction.setMaxHeight(heightOfScreen / 10);
		btnStartCallFunction.setMinimumWidth((int) (widthOfScreen / 3));
		btnStartCallFunction.setMaxWidth((int) (widthOfScreen / 3));
		btnStartCallFunction.setX(0);
		btnStartCallFunction.setY(heightOfScreen - 190);

		btn_Sms_Mail = (ImageButton) findViewById(R.id.btn_Sms_Mail);
		btn_Sms_Mail.setMinimumWidth((int) (widthOfScreen / 3));
		btn_Sms_Mail.setMaxWidth((int) (widthOfScreen / 3));
		btn_Sms_Mail.setX(widthOfScreen / 3);
		btn_Sms_Mail.setY(heightOfScreen - 190);

		btn_Apps = (ImageButton) findViewById(R.id.btn_Apps);
		btn_Apps.setMinimumWidth((int) (widthOfScreen / 3));
		btn_Apps.setMaxWidth((int) (widthOfScreen / 3));
		btn_Apps.setX(widthOfScreen * 2 / 3);
		btn_Apps.setY(heightOfScreen - 190);

//		btnFunction4 = (ImageButton) findViewById(R.id.btn_Function4);
//		btnFunction4.setMinimumWidth((int) (widthOfScreen / 5));
//		btnFunction4.setMaxWidth((int) (widthOfScreen / 5));
//		btnFunction4.setX(widthOfScreen * 3 / 5);
//		btnFunction4.setY(heightOfScreen - 190);
//
//		btnFunction5 = (ImageButton) findViewById(R.id.btn_Function5);
//		btnFunction5.setMinimumWidth((int) (widthOfScreen / 5));
//		btnFunction5.setMaxWidth((int) (widthOfScreen / 5));
//		btnFunction5.setX(widthOfScreen * 4 / 5);
//		btnFunction5.setY(heightOfScreen - 190);

		messageHandler = new MessageHandler();

		// ouvrir un thread pour ecouter un appel du patient
		threadReceiveCallFromJava = new Thread(new ReceiveCallMsgFromJava());
		threadReceiveCallFromJava.start();

		btnStartCallFunction.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// ouvrir la fenetre de repertoire
				startActivity(new Intent("pkg2.directory.Directory"));
			}
		});

		btn_Sms_Mail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent("pkg2_2.mail_sms.Main_Chat"));
			}
		});
		
		btn_Apps.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(
						"pkg2_3.apps.music.AndroidBuildingMusicPlayerActivity"));
			}
		});

		btnListenACall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnListenACall.setEnabled(false);
				//btnListenACall.setVisibility(View.INVISIBLE);
				btnListenACall.setText("Écoute d'un appel en cours...");
				btnListenACall.clearAnimation();
				if (mediaPlayer != null) {
					mediaPlayer.release();
				}

				threadForRinging = null;
				isAcceptedCommunication = true;

			}
		});

	}

	class ReceiveCallMsgFromJava implements Runnable {
		public void run() {
			try {
				Log.i("ReceiveCall", "Waiting call ...");
				sskReceiveCallFromJava = new ServerSocket(
						portReceiveCallFromJava);

				DataOutputStream dos;
				DataInputStream dis;
				byte[] data = new byte[2];

				while (true) {
					skForJavaClient = sskReceiveCallFromJava.accept();
					dos = new DataOutputStream(
							skForJavaClient.getOutputStream());
					dis = new DataInputStream(skForJavaClient.getInputStream());
					stopBybtnStop = false;

					// recevoir un appel de Java
					int i = dis.read(data);
					canBeCallerToJava = false;
					Log.i("ReceiveCall", "Call msg of java: " + i);

					messageHandler.sendEmptyMessage(104);
					
					// clignoter le bouton, pas encore fait
					messageHandler.sendEmptyMessage(105); // pour clignoter le
															// boutton

					// ringing
					threadForRinging = new Thread(new Ringing());
					threadForRinging.start();

					

					// repondre a java
					isAcceptedCommunication = false;
					long begin = System.currentTimeMillis();
					long waitTime = 0;
					while (isAcceptedCommunication == false
							&& (waitTime <= timeOut * 1000)) {
						waitTime = System.currentTimeMillis() - begin;
					}

					Log.i("ReceiveCall", "isAcctepted: "
							+ isAcceptedCommunication);

					if (isAcceptedCommunication) {
						byte[] bufToReponseToJava = new byte[] { 1, 1 };
						dos.write(bufToReponseToJava);
						dos.flush();
						messageHandler.sendEmptyMessage(102);
					} else {
						messageHandler.sendEmptyMessage(103); // pour pouvoir
																// rappeler au
																// java
					}

					skForJavaClient.close();

				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		private class Ringing implements Runnable {
			public void run() {
				try {
					// String filePath = "/mnt/sdcard/AHien/HaoHanCa1.mp3";
					String filePath = "/mnt/sdcard/AHien/Ringing.mp3";

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
					System.arraycopy(audioData, 0, audioData1, 0,
							audioData1.length);

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
					File tempMp3 = File.createTempFile("hien", "mp3");

					tempMp3.deleteOnExit();
					FileOutputStream fos = new FileOutputStream(tempMp3);
					fos.write(mp3SoundByteArray);
					fos.close();

					mediaPlayer = new MediaPlayer();

					FileInputStream fis = new FileInputStream(tempMp3);
					mediaPlayer.setDataSource(fis.getFD());

					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IOException ex) {
					String s = ex.toString();
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main__home__screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class MessageHandler extends Handler {
		// @SuppressLint("ParserError")
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 102: {
				btnListenACall.setEnabled(false);
				canBeCallerToJava = false;
				// creer une autre activite pour la communication
				startActivity(new Intent(
						"pkg3.view_preview.Main_View_Preview_AfterReceiveACall"));
				break;

			}

			case 103: {
				btnListenACall.setEnabled(false);

				canBeCallerToJava = true;
				btnListenACall.clearAnimation();
				break;
			}

			case 104: {
				btnListenACall.setEnabled(true);
				btnListenACall.setVisibility(View.VISIBLE);
				btnListenACall.setText("Acceptez ?");

				break;
			}

			// clignoter le boutton
			case 105: {
				btnListenACall.setVisibility(View.VISIBLE);
				btnListenACall.setEnabled(true);

				final Animation animation = new AlphaAnimation(1, 0); // Change
																		// alpha
																		// from
																		// fully
																		// visible
																		// to
																		// invisible
				animation.setDuration(500); // duration - half a second
				animation.setInterpolator(new LinearInterpolator()); // do not
																		// alter
																		// animation
																		// rate
				animation.setRepeatCount(Animation.INFINITE); // Repeat
																// animation
																// infinitely
				animation.setRepeatMode(Animation.REVERSE); // Reverse animation
															// at the end so the
															// button will fade
															// back in

				btnListenACall.setAnimation(animation);
				btnListenACall.startAnimation(animation);

				break;
			}

			default: {
				super.handleMessage(msg);
				break;
			}
			}

		}
	}
}
