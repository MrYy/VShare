package com.ANT.MiddleWare.PartyPlayerActivity.test;

import java.util.Random;

import junit.framework.Assert;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Entities.FileFragment.FileFragmentException;
import com.ANT.MiddleWare.Entities.Segment;

import android.test.AndroidTestCase;

public class FileFragmentTest extends AndroidTestCase {
	private static final String TAG = SegmentTest.class.getSimpleName();
	private static final int base = 50;

	public void setUp() throws Exception {
	}

	public final void test() {
		FileFragment.LIMIT_LEN = 3;
		FileFragment f = new FileFragment(0, base, 1,base);
		try {
			f.setData(getRandomString(base).getBytes());
		} catch (FileFragmentException e) {
			e.printStackTrace();
		}
		FileFragment[] data = null;
		try {
			data = f.split();
		} catch (FileFragmentException e) {
			e.printStackTrace();
		}
		Segment s = new Segment(1, base);
		for (FileFragment f1 : data) {
			s.insert(f1);
			s.checkIntegrity();
		}
		Assert.assertTrue(s.checkIntegrity());
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
