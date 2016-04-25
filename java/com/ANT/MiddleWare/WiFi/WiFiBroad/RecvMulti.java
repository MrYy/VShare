package com.ANT.MiddleWare.WiFi.WiFiBroad;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RecvMulti extends Thread {
	private static final String TAG = RecvMulti.class.getSimpleName();

	private Context activity;
	private MulticastSocket socket;
	private PipedOutputStream po;

	public RecvMulti(PipedOutputStream po, Context activity,
			MulticastSocket socket) {
		this.activity = activity;
		this.socket = socket;
		this.po = po;
	}

	@Override
	public void run() {
		int buflen = 0;
		try {
			buflen = socket.getReceiveBufferSize();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		((Activity) activity).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, "buflen", Toast.LENGTH_SHORT).show();
			}
		});
		if (buflen <= 0) {
			return;
		}
		byte[] buf = new byte[buflen];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		while (true) {
			try {
				socket.receive(packet);
				Log.d(TAG, packet.toString());
				po.write(packet.getData(), packet.getOffset(),
						packet.getLength());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
