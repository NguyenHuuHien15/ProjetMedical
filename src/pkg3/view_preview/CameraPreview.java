package pkg3.view_preview;

/*
 *  CameraPreview.java
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.NoSuchElementException;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

public class CameraPreview implements SurfaceHolder.Callback,
		Camera.PreviewCallback {
	private Camera mCamera = null;
	private ImageView MyCameraPreview = null;
	private Bitmap bitmap = null;
	private int[] pixels = null;
	private byte[] FrameData = null;
	private int imageFormat;
	private int PreviewSizeWidth;
	private int PreviewSizeHeight;
	private boolean bProcessing = true;
	Handler mHandler = new Handler(Looper.getMainLooper());

	// pour envoyer a Java
	private DatagramSocket socketToSendToJava = null;
	private DatagramPacket packcetToSendToJava = null;

	private String ipOfPatient_Java;
	private int portToSendImagesToJava;

	FileInputStream fis = null;
	BufferedInputStream bis = null;
	// byte[] byteArray;
	Thread threadToSendToJava = null;
	private boolean status = false;

	public void setSocketToSend_Images_To_JavaPatient(DatagramSocket socket) {
		socketToSendToJava = socket;
	}

	public void closeSendSocket() {
		socketToSendToJava.close();
		threadToSendToJava = null;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight,
			ImageView CameraPreview, String ipOfPatient_Java,
			int portToSendImagesToJava) {
		PreviewSizeWidth = PreviewlayoutWidth;
		PreviewSizeHeight = PreviewlayoutHeight;
		MyCameraPreview = CameraPreview;
		bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight,
				Bitmap.Config.ARGB_8888);

		pixels = new int[PreviewSizeWidth * PreviewSizeHeight];

		this.ipOfPatient_Java = ipOfPatient_Java;
		this.portToSendImagesToJava = portToSendImagesToJava;
	}

	public void rotateNV21(byte[] input, byte[] output, int width, int height,
			int rotation) {
		boolean swap = (rotation == 90 || rotation == 270);
		boolean yflip = (rotation == 90 || rotation == 180);
		boolean xflip = (rotation == 270 || rotation == 180);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int xo = x, yo = y;
				int w = width, h = height;
				int xi = xo, yi = yo;
				if (swap) {
					xi = w * yo / h;
					yi = h * xo / w;
				}
				if (yflip) {
					yi = h - yi - 1;
				}
				if (xflip) {
					xi = w - xi - 1;
				}
				output[w * yo + xo] = input[w * yi + xi];
				int fs = w * h;
				int qs = (fs >> 2);
				xi = (xi >> 1);
				yi = (yi >> 1);
				xo = (xo >> 1);
				yo = (yo >> 1);
				w = (w >> 1);
				h = (h >> 1);
				// adjust for interleave here
				int ui = fs + (w * yi + xi) * 2;
				int uo = fs + (w * yo + xo) * 2;
				// and here
				int vi = ui + 1;
				int vo = uo + 1;
				output[uo] = input[ui];
				output[vo] = input[vi];
			}
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		// At preview mode, the frame data will push to here.
		// if (imageFormat == ImageFormat.NV21) {
		// We only accept the NV21(YUV420) format.
		// if (!bProcessing) {
		if (status == true) {
			FrameData = data;
			try {
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();

				byte[] output = new byte[data.length];
				rotateNV21(data, output, size.width, size.height, 0); // rotation d'image envoyee

				YuvImage image = new YuvImage(output,
						parameters.getPreviewFormat(), size.width, size.height,
						null);
				
				File file = File.createTempFile("hien", "jpg");
				file.deleteOnExit();

				FileOutputStream filecon = new FileOutputStream(file);

				image.compressToJpeg(
						new Rect(0, 0, image.getWidth(), image.getHeight()),
						30, filecon); // 50 est la qualite pour compresser

				// load file, write to bytes array and send to Java

				int length = (int) file.length();

				byte[] byteArray = new byte[length];
				fis = new FileInputStream(file);
				/*
				 * bis = new BufferedInputStream(fis); bis.read(byteArray);
				 * bis.close();
				 */
				fis.read(byteArray);

				fis.close();

				// ajouter le moment d'envoie au android
				long momentToSendImage_ToAndroid = System.currentTimeMillis();

				byte[] tempBytes = new byte[8];
				Utils.putLong(momentToSendImage_ToAndroid, tempBytes, 0);

				// ajouter longeur d'image
				int lengthOfImage = byteArray.length;
				byte[] tempBytesForLength = new byte[4];
				Utils.putInt(lengthOfImage, tempBytesForLength, 0);

				// attacher ce moment et longeur avec donnees d'image
				byte[] byteArray1 = new byte[byteArray.length + 12];
				for (int i = 0; i < byteArray1.length; i++) {
					if (i < 8) {
						byteArray1[i] = tempBytes[i];
					} else if (i >= 8 && i < 12) {
						byteArray1[i] = tempBytesForLength[i - 8];
					} else {
						byteArray1[i] = byteArray[i - 12];
					}
				}

				// call thread to send bytes array to java_patient
				Log.i("Bytes of image to send: ", "" + byteArray1.length);
				threadToSendToJava = new Thread(new SendToJava_Patient(
						byteArray));
				threadToSendToJava.start();

				Thread.sleep(41);

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		} else {
			// socketToSendToJava.close();
			// threadToSendToJava = null;

		}

	}

	public void onPause() {
		mCamera.stopPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Parameters parameters;

		parameters = mCamera.getParameters();

		// Set the camera preview size
		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

		imageFormat = parameters.getPreviewFormat();

		mCamera.setParameters(parameters);

		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// mCamera = Camera.open(); // to set back camera (default)
		mCamera = getFrontFacingCamera();
		
		mCamera.setDisplayOrientation(0); // 0 ou 180 pour orientation horizontale, 90 pur orientation verticale

		try {
			// If did not set the SurfaceHolder, the preview area will be black.
			mCamera.setPreviewDisplay(arg0);
			mCamera.setPreviewCallback(this);
		} catch (IOException e) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	Camera getFrontFacingCamera() throws NoSuchElementException {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++) {
			Camera.getCameraInfo(cameraIndex, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					return Camera.open(cameraIndex);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		throw new NoSuchElementException("Can't find front camera.");
	}

	private class SendToJava_Patient implements Runnable {
		private byte[] bytesArray;

		public SendToJava_Patient(byte[] bytesArray) {
			this.bytesArray = bytesArray;
		}

		public void run() {
			try {
				// send bytes array
				packcetToSendToJava = new DatagramPacket(bytesArray,
						bytesArray.length,
						InetAddress.getByName(ipOfPatient_Java),
						portToSendImagesToJava);

				socketToSendToJava.send(packcetToSendToJava);

				// Log.i("Doctor End", "Longeur de fichier envoye en realite: "
				// + packcetToSendToJava.getData().length);

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}

	}
}
