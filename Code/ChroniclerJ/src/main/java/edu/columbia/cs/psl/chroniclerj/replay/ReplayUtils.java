package edu.columbia.cs.psl.chroniclerj.replay;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import edu.columbia.cs.psl.chroniclerj.CallbackInvocation;
import edu.columbia.cs.psl.chroniclerj.ExportedLog;
import edu.columbia.cs.psl.chroniclerj.ExportedSerializableLog;
import edu.columbia.cs.psl.chroniclerj.Log;

public class ReplayUtils {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int getNextIndex(HashMap replayIndexMap, String[] threadEntries, int fill) {
		String threadName = Thread.currentThread().getName();
		if (threadName.equals("Finalizer"))
			threadName = threadName + curFinalizer;
		if (!replayIndexMap.containsKey(threadName))
			replayIndexMap.put(threadName, 0);
		int r = (int) replayIndexMap.get(threadName);
		while (r <= fill && threadEntries[r] != null && !threadEntries[r].equals(threadName)) {
			r++;
		}
		checkForDispatch();
		if (threadEntries[r] == null) {
			// System.out.println(Arrays.deepToString(threadEntries));

			// System.out.println(Arrays.deepToString(ExportedSerializableLog.iLog_owners));
			return r;
		}
		if (threadEntries[r] != null && threadEntries[r].equals(threadName)) {
			replayIndexMap.put(threadName, r + 1);
			return r;
		}

		// System.out.println("Skipping " + threadEntries[r] + " vs " +
		// threadName);
		return -1;
	}

	public static HashMap<Integer, CallbackInvocation> dispatchesToRun;

	public static void checkForDispatch() {
		int curClock = ExportedLog.globalReplayIndex;
		// System.out.println("Looking for dispatches at " + curClock);
		if (dispatchesToRun != null && dispatchesToRun.get(curClock) != null) {
			// System.out.println("Invoke " + dispatchesToRun.get(curClock));
			if (dispatchesToRun.get(curClock).invoke()) {
				// System.out.println("Success");
				ExportedLog.globalReplayIndex++;
				checkForDispatch();
			}
		}
		curClock++;
		if (dispatchesToRun != null && dispatchesToRun.get(curClock) != null) {
			// System.out.println("Invoke " + dispatchesToRun.get(curClock));
			if (dispatchesToRun.get(curClock).invoke()) {
				// System.out.println("Success");
				ExportedLog.globalReplayIndex += 2;
				checkForDispatch();
			}
		}
	}

	public static long curFinalizer;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int getNextIndexO(HashMap replayIndexMap, String[] threadEntries, int fill, Object[] log) {

		String threadName = Thread.currentThread().getName();
		if (threadName.equals("Finalizer"))
			threadName = threadName + curFinalizer;
		if (!replayIndexMap.containsKey(threadName))
			replayIndexMap.put(threadName, 0);
		int r = (int) replayIndexMap.get(threadName);
		while (r <= fill && threadEntries[r] != null && !threadEntries[r].equals(threadName)) {
			r++;
		}

		checkForDispatch();
		if (threadEntries[r] == null) {
			System.err.println("Replay log ended in thread " + threadName);
			System.exit(-1);
		}
		if (threadEntries[r].equals(threadName)) {
			replayIndexMap.put(threadName, r + 1);
			return r;
		}

		return -1;
	}

	static ObjectInputStream in;
	static boolean connected = false;
	private static Socket socket;

	public static void connect() {
		if (!connected) {
			while (true) {
				try {
					socket = new Socket("localhost", 1235);
					in = new ObjectInputStream(socket.getInputStream());
					Boolean ready = in.readBoolean();
					while (!ready) {
						try {
							Thread.sleep(100);
							ready = in.readBoolean();
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
					break;
				} catch (IOException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			}
			connected = true;
		}
	}

	public static Object getNextObject() {
		Log.logLock.lock();
		try {
			int idx = ReplayUtils.getNextIndexO(ExportedLog.aLog_replayIndex, ExportedLog.aLog_owners, ExportedLog.aLog_fill, ExportedLog.aLog);
			while (idx < 0) {
				idx = ReplayUtils.getNextIndexO(ExportedLog.aLog_replayIndex, ExportedLog.aLog_owners, ExportedLog.aLog_fill, ExportedLog.aLog);
				ReplayRunner.loadNextLog("edu/columbia/cs/psl/chroniclerj/ExportedLog");
			}
			ExportedLog.globalReplayIndex++;
			return ExportedLog.aLog[idx];
		} finally {
			Log.logLock.unlock();
		}
		/*Object data = null;
		try {
			data = in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return data;*/
	}

	public static int getNextI() {
		int data = 0;
		try {
			data = in.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static float getNextF() {
		float data = 0;
		try {
			data = in.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static short getNextS() {
		short data = 0;
		try {
			data = in.readShort();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static long getNextJ() {
		long data = 0;
		try {
			data = in.readLong();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static boolean getNextZ() {
		boolean data = false;
		try {
			data = in.readBoolean();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static byte getNextB() {
		byte data = 0;
		try {
			data = in.readByte();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static char getNextC() {
		char data = 0;
		try {
			data = in.readChar();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static double getNextD() {
		double data = 0;
		try {
			data = in.readDouble();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static void copyInto(Object dest, Object src, int len) {
		System.arraycopy(src, 0, dest, 0, len);
	}
}
