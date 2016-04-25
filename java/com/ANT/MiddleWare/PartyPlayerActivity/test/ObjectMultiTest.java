package com.ANT.MiddleWare.PartyPlayerActivity.test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.WiFi.WiFiBroad.ObjectMulti;
import com.ANT.MiddleWare.WiFi.WiFiBroad.SendMulti;

public class ObjectMultiTest extends AndroidTestCase {
	private static final String TAG = ObjectMultiTest.class.getSimpleName();
	private String data;
	private HashSet<Integer> numSet = new HashSet<Integer>();
	private LinkedList<FileFragment> fraList = new LinkedList<FileFragment>();
	private int base = 5000;
	private int fra = 50;

	@Override
	public void setUp() throws Exception {
		data = getRandomString(base);
		Random random = new Random();
		numSet.add(0);
		numSet.add(base);
		while (true) {
			if (numSet.size() == fra + 1)
				break;
			numSet.add(random.nextInt(base));
		}
		LinkedList<Integer> setSort = new LinkedList<Integer>(numSet);
		Collections.sort(setSort);
		for (int i = 0; i < fra; i++) {
			int start = i == 0 ? 0 : random.nextInt(setSort.get(i));
			int stop = i == fra - 1 ? base : setSort.get(i + 1)
					+ random.nextInt(base - setSort.get(i + 1));

			FileFragment tmp = new FileFragment(start, stop, 1, base);
			tmp.setData(data.substring(start, stop).getBytes());
			fraList.add(tmp);
		}
		Collections.shuffle(fraList);
	}

	public final void testRun() {
		final PipedOutputStream po = new PipedOutputStream();
		try {
			final PipedInputStream pi = new PipedInputStream(po);

			ObjectMulti objThd = new ObjectMulti(pi, getContext(),
					new SendMulti(null, null));
			objThd.start();
			for (FileFragment f : fraList) {
				po.write(f.toBytes());
			}
			final IntegrityCheck iTC = IntegrityCheck.getInstance();
			byte[] tmp = iTC.getSegments(1);
			Assert.assertNotNull(tmp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
}
