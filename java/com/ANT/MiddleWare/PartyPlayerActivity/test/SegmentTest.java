package com.ANT.MiddleWare.PartyPlayerActivity.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Entities.Segment;

/**
 * @author zxyqwe
 * 
 */
public class SegmentTest extends AndroidTestCase {
	private static final String TAG = SegmentTest.class.getSimpleName();
	private String data;
	private HashSet<Integer> numSet = new HashSet<Integer>();
	private LinkedList<FileFragment> fraList = new LinkedList<FileFragment>();
	private int base = 5000;
	private int fra = 50;

	/**
	 * @throws java.lang.Exception
	 */
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

			FileFragment tmp = new FileFragment(start, stop, 1,base);
			tmp.setData(data.substring(start, stop).getBytes());
			fraList.add(tmp);
		}
		Collections.shuffle(fraList);
	}

	/**
	 * Test method for
	 * {@link com.example.entities.Segment#insert(com.example.entities.FileFragment)}
	 * .
	 */
	public final void testInsert() {
		Segment seg = new Segment(1, base);
		for (FileFragment ff : fraList) {
			seg.insert(ff);
		}
	}

	/**
	 * Test method for {@link com.example.entities.Segment#checkIntegrity()}.
	 */
	public final void testCheckIntegrity() {
		Segment seg = new Segment(1, base);
		boolean b = seg.checkIntegrity();
		Assert.assertFalse(b);
		for (FileFragment ff : fraList) {
			seg.insert(ff);
		}
		b = seg.checkIntegrity();
		Assert.assertTrue(b);
	}

	/**
	 * Test method for {@link com.example.entities.Segment#getData()}.
	 */
	public final void testGetData() {
		Random random = new Random();
		Segment seg = new Segment(1, base);
		byte[] tmp = seg.getData();
		Assert.assertNull(tmp);
		for (FileFragment ff : fraList) {
			seg.insert(ff);
			for (int i = 0; i < fra; i++) {
				int start = random.nextInt(base);
				byte[] buf = seg.getData(start);
				if (buf != null) {
					Assert.assertTrue(new String(buf).compareTo(data.substring(
							start, start + buf.length)) == 0);
				}
			}
		}
		tmp = seg.getData();
		// Assert.assertFalse(new String(tmp).compareTo(data) == 0);
		boolean b = seg.checkIntegrity();
		Assert.assertTrue(b);
		tmp = seg.getData();
		Assert.assertTrue(new String(tmp).compareTo(data) == 0);
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
