package com.ANT.MiddleWare.Entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.ANT.MiddleWare.Entities.FileFragment.FileFragmentException;

import android.util.Log;

public class Segment {
	private static final String TAG = Segment.class.getSimpleName();

	private int segmentID;
	private ArrayList<FileFragment> segmentList;
	private int segLength = -1;
	private boolean Intergrity = false;
	private static Random random = new Random();
	private int percent = 0;

	public synchronized void setSegLength(int segLength) {
		if (this.segLength == -1) {
			this.segLength = segLength;
		}
	}

	public Segment(int id, int length) {
		this.segmentID = id;
		this.segLength = length;
		segmentList = new ArrayList<FileFragment>();
	}

	public boolean insert(FileFragment fm) {
		synchronized (this) {
			if (fm.isWritten() && fm.getSegmentID() == segmentID && !Intergrity) {
				percent += fm.getFragLength();
				segmentList.add(fm.clone());
				Collections.sort(segmentList);
				setSegLength(fm.getSegmentLen());
				return true;
			}
			return false;
		}
	}

	private void merge() throws SegmentException {
		if (segmentList == null || segmentList.size() <= 1) {
			return;
		}
		int size = segmentList.size();
		for (int i = 0; i < size - 1; i++) {
			FileFragment prev = segmentList.get(i);
			FileFragment next = segmentList.get(i + 1);
			if (prev.getStopIndex() < next.getStartIndex()) {
				continue;
			}
			if (prev.getStartIndex() == next.getStartIndex()) {
				if (prev.getFragLength() <= next.getFragLength()) {
					segmentList.remove(i);
					percent -= prev.getFragLength();
				} else {
					segmentList.remove(i + 1);
					percent -= next.getFragLength();
				}
				size--;
				i--;
				continue;
			} else if (prev.getStartIndex() < next.getStartIndex()) {
				if (prev.getStopIndex() < next.getStopIndex()) {
					percent -= prev.getFragLength();
					prev.setData(next.getData(), next.getStartIndex());
					percent += prev.getFragLength();
					Log.d(TAG, "" + segLength + " " + prev.getStopIndex());
				}
				segmentList.remove(i + 1);
				percent -= next.getFragLength();
				size--;
				i--;
			} else {
				throw new SegmentException("Not Sort");
			}
		}
	}

	public boolean checkIntegrity() {
		if (Intergrity)
			return Intergrity;
		synchronized (this) {
			if (segmentList == null) {
				return Intergrity;
			}
			try {
				merge();
			} catch (SegmentException e) {
				e.printStackTrace();
			}

			if (segmentList.size() == 1
					&& segmentList.get(0).getFragLength() == segLength) {
				Intergrity = true;
//				Log.w(TAG, "Percent " + getPercent());
				return Intergrity;
			}
//			Log.w(TAG, "Percent " + getPercent());
			return Intergrity;
		}

	}

	public byte[] getData() {
		synchronized (this) {
			if (segmentList == null || segmentList.size() == 0)
				return null;
			checkIntegrity();
			return segmentList.get(0).getData();
		}

	}

	public byte[] getData(int start) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		synchronized (this) {
			if (segmentList == null || segmentList.size() == 0) {
				return null;
			}
			checkIntegrity();
			for (FileFragment f : segmentList) {
				byte[] tmp = f.getData(start);
				if (tmp != null)
					return tmp;
			}
			return null;
		}
	}

	public FileFragment getFragment(int start) throws FileFragmentException {
		byte[] data = getData(start);
		if (data == null)
			return null;
		FileFragment f = new FileFragment(start, start + data.length, segmentID,segLength);
		f.setData(data);
		return f;
	}

	public int getMiss() throws SegmentException {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		synchronized (this) {
			if (segmentList == null) {
				return 0;
			}
			int size = segmentList.size();
			if (size == 0) {
				return 0;
			}
			if (checkIntegrity())
				throw new SegmentException("No Fragment Miss");
			if (size == 1) {
				return segmentList.get(0).getStopIndex();
			}
			return segmentList.get(random.nextInt(size - 1)).getStopIndex();
		}
	}

	public double getPercent() {
		return percent * 100.0 / segLength;
	}

	public class SegmentException extends Exception {
		private static final long serialVersionUID = 1187571347280690149L;

		public SegmentException(String string) {
			super(string);
		}
	}
}