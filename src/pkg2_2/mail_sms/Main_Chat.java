package pkg2_2.mail_sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import pkg6.entities.General_Infos;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import pkg1.main_home_screen.*;

/**
 * MessageActivity is a main Activity to show a ListView containing Message
 * items
 * 
 * @author Adil Soomro
 *
 */
public class Main_Chat extends ListActivity {
	/** Called when the activity is first created. */

	ArrayList<MessageOfSMS> messages;
	AwesomeAdapter adapter;
	EditText text;
	private Thread threadToReceiveSMS;

	private HandlerForSMS handlerForSMS;
	private boolean receiverIsRunning = false;
	private ServerSocket serverSocket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_sms);

		handlerForSMS = new HandlerForSMS();
		receiverIsRunning = true;

		threadToReceiveSMS = new Thread(new ReceiverSMS());
		threadToReceiveSMS.start();

		text = (EditText) this.findViewById(R.id.text);

		messages = new ArrayList<MessageOfSMS>();

		adapter = new AwesomeAdapter(this, messages);
		setListAdapter(adapter);

	}

	public void sendMessage(View v) {
		String newMessage = text.getText().toString().trim();
		if (newMessage.length() > 0) {
			text.setText("");
			addNewMessage(new MessageOfSMS(newMessage, true));
			messages.get(messages.size() - 1).setMessage(newMessage);
			// update the status for that
			adapter.notifyDataSetChanged();
			getListView().setSelection(messages.size() - 1);
			// new SendMessage().execute();

			Thread threadToSendMsg = new Thread(new SenderMessageToJava(
					newMessage));
			threadToSendMsg.start();
			
			setTitle("SMS");
		}
	}

	void addNewMessage(MessageOfSMS m) {
		messages.add(m);
		adapter.notifyDataSetChanged();
		getListView().setSelection(messages.size() - 1);
	}

	private class SenderMessageToJava implements Runnable {
		// String ipOfJava = "10.30.80.90";
		// String ipOfJava = "192.168.1.53";
		String ipOfJava = General_Infos.IP_OF_OTHER_SIDE;

		int portToSendMsg = 4321;
		private String message;

		public SenderMessageToJava(String message) {
			this.message = message;
		}

		public void run() {
			try {
				Socket socketToSendMsg = new Socket(ipOfJava, portToSendMsg);
				DataOutputStream dos = new DataOutputStream(
						socketToSendMsg.getOutputStream());

				Log.i("Msg to send: ", message);

				dos.writeUTF(message);

				dos.flush();
				dos.close();
				socketToSendMsg.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	}

	private class ReceiverSMS implements Runnable {
		private int portForSMS = 4321;

		public void run() {
			try {
				serverSocket = new ServerSocket(portForSMS);
				while (receiverIsRunning) {
					Socket socket = serverSocket.accept();
					DataInputStream dis = new DataInputStream(
							socket.getInputStream());
					String smsArrive = dis.readUTF();

					Log.i("Sms arrive: ", smsArrive);
					
					// ring to notify
					Thread threadToRing = new Thread(new Ringing_SmsArrive(
							"/mnt/sdcard/AHien/tiny-bell-sms.mp3"));
					threadToRing.start();
					
					// send sms to main ui
					handlerForSMS.obtainMessage(107, smsArrive).sendToTarget();

					dis.close();
					socket.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
	}

	private class HandlerForSMS extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 107: {
				String newMessage = (String) msg.obj;

				if (newMessage.length() > 0) {
					text.setText("");
					addNewMessage(new MessageOfSMS(newMessage, false));
					messages.get(messages.size() - 1).setMessage(newMessage);
					// update the status for that
					adapter.notifyDataSetChanged();
					getListView().setSelection(messages.size() - 1);
					setTitle("Votre Correspondance");

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

	public void onDestroy() {
		super.onDestroy();
		receiverIsRunning = false;
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threadToReceiveSMS = null;

	}
}