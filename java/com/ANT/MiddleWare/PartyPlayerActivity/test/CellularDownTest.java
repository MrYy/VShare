/**
 * 
 */
package com.ANT.MiddleWare.PartyPlayerActivity.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.PartyPlayerActivity.ConfigureData;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;

/**
 * @author zxyqwe
 * 
 */
public class CellularDownTest extends AndroidTestCase {
	private static final String TAG = CellularDownTest.class.getSimpleName();
	private String data;
	private HashSet<Integer> numSet = new HashSet<Integer>();
	public static Stack<FileFragment> fraList = new Stack<FileFragment>();
	public static int base = 5000;
	private int fra = 50;
	private static DashProxyServer server = new DashProxyServer();

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
	 * {@link com.example.Celluar.CellularDown#queryFragment(java.lang.String)}.
	 */
	public final void testQueryFragment() {
		ViewVideoActivity.configureData
				.setWorkingMode(ConfigureData.WorkMode.JUNIT_TEST_MODE);
		final IntegrityCheck iTC = IntegrityCheck.getInstance();
		byte[] tmp = iTC.getSegments(1);
		Assert.assertNotNull(tmp);
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
