package com.broadsense.newpine.bluetooth.Utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * 项目名称：${守护神}
 * 类描述：工具类
 * 创建人：${jcky}
 * 创建时间：2016/1/8 16:47
 * 修改人：${jcky}
 * 修改时间：2016/1/8 16:47
 * 修改备注：
 * 备注:  * @time 	2015-6-23 上午11:33:49
 * 		    @des	   日志级别是LEVEL_ALL显示所有信息,包括System.out.println信息
 *          @des  日志级别是LEVEL_OFF关闭所有信息,包括System.out.println信息
 */

public class LogUtils {
	/** 日志输出时的TAG */
	private static String mTag = "BluetoothCall_5";
	/** 日志输出级别NONE */
	public static final int LEVEL_OFF = 0;
	/** 日志输出级别NONE */
	public static final int LEVEL_ALL = 7;

	/** 日志输出级别V */
	public static final int LEVEL_VERBOSE = 1;
	/** 日志输出级别D */
	public static final int LEVEL_DEBUG = 2;
	/** 日志输出级别I */
	public static final int LEVEL_INFO = 3;
	/** 日志输出级别W */
	public static final int LEVEL_WARN = 4;
	/** 日志输出级别E */
	public static final int LEVEL_ERROR = 5;
	/** 日志输出级别S,自定义一个级别 */
	public static final int LEVEL_SYSTEM = 6;

	/** 是否允许输出log */
	private static int mDebuggable = LEVEL_ALL;

	/** 用于记时的变量 */
	private static long mTimestamp = 0;
	/** 写文件的锁对象 */
	private static final Object mLogLock = new Object();

	/**---------------日志输出,已固定TAG  begin---------------**/
	/** 以级别为 d 的形式输出LOG */
	public static void v(String msg) {
		if (mDebuggable >= LEVEL_VERBOSE) {
			Log.v(mTag, formatMsg("", msg));
		}
	}

	/** 以级别为 d 的形式输出LOG */
	public static void d(String msg) {
		if (mDebuggable >= LEVEL_DEBUG) {
			Log.d(mTag, formatMsg("", msg));
		}
	}

	/** 以级别为 i 的形式输出LOG */
	public static void i(String msg) {
		if (mDebuggable >= LEVEL_INFO) {
			Log.i(mTag, formatMsg("", msg));
		}
	}

	/** 以级别为 i 的形式输出Throwable */
	public static void i(Throwable tr) {
		if (mDebuggable >= LEVEL_WARN) {
			Log.i(mTag, "", tr);
		}
	}

	/** 以级别为 i 的形式输出LOG信息和Throwable */
	public static void i(String msg, Throwable tr) {
		if (mDebuggable >= LEVEL_WARN && null != msg) {
			Log.i(mTag, formatMsg("", msg), tr);
		}
	}

	/** 以级别为 w 的形式输出LOG */
	public static void w(String msg) {
		if (mDebuggable >= LEVEL_WARN) {
			Log.w(mTag, formatMsg("", msg));
		}
	}

	/** 以级别为 w 的形式输出Throwable */
	public static void w(Throwable tr) {
		if (mDebuggable >= LEVEL_WARN) {
			Log.w(mTag, "", tr);
		}
	}

	/** 以级别为 w 的形式输出LOG信息和Throwable */
	public static void w(String msg, Throwable tr) {
		if (mDebuggable >= LEVEL_WARN && null != msg) {
			Log.w(mTag, formatMsg("", msg), tr);
		}
	}

	/** 以级别为 e 的形式输出LOG */
	public static void e(String msg) {
		if (mDebuggable >= LEVEL_ERROR) {
			Log.e(mTag, formatMsg("", msg));
		}
	}

	/** 以级别为 e 的形式输出Throwable */
	public static void e(Throwable tr) {
		if (mDebuggable >= LEVEL_ERROR) {
			Log.e(mTag, "", tr);
		}
	}

	/** 以级别为 e 的形式输出LOG信息和Throwable */
	public static void e(String msg, Throwable tr) {
		if (mDebuggable >= LEVEL_ERROR && null != msg) {
			Log.e(mTag, formatMsg("", msg), tr);
		}
	}

	/**---------------日志输出,已固定TAG  end---------------**/

	/**---------------日志输出,未固定TAG  begin---------------**/
	/** 以级别为 d 的形式输出LOG */
	public static void v(String tag, String msg) {
		if (mDebuggable >= LEVEL_VERBOSE) {
			Log.v(tag, msg);
		}
	}

	/** 以级别为 d 的形式输出LOG */
	public static void d(String tag, String msg) {
		if (mDebuggable >= LEVEL_DEBUG) {
			Log.d(tag,formatMsg(tag, msg));
//			Log.d(tag, msg);
		}
	}

	/** 以级别为 i 的形式输出LOG */
	public static void i(String tag, String msg) {
		if (mDebuggable >= LEVEL_INFO) {
			Log.i(tag, formatMsg(tag, msg));
		}
	}

	/** 以级别为 w 的形式输出LOG */
	public static void w(String tag, String msg) {
		if (mDebuggable >= LEVEL_WARN) {
			Log.w(tag, formatMsg(tag, msg));
		}
	}

	/** 以级别为 e 的形式输出LOG */
	public static void e(String tag, String msg) {
		if (mDebuggable >= LEVEL_ERROR) {
//			MyLog.d(tag, msg);
			Log.e(tag, formatMsg(tag, msg));
		}
	}


	private static String formatMsg(String tag, String msg){
		return "[" + getPid() + "]" + "[" + tag + "] [thread=" + Thread.currentThread().getId() + "] ====== " + msg;
	}

	/**
	 * @return 返回进程ID和调用该进程的ID
	 */
	private static String getPid(){
		return "pid:"+android.os.Process.myPid()+" tid:"+android.os.Process.myTid();
	}

	/**---------------日志输出,未固定TAG  end---------------**/

	/**
	 * 把Log存储到文件中
	 * 
	 * @param log
	 *            需要存储的日志
	 * @param path
	 *            存储路径
	 */
	public static void log2File(String log, String path) {
		log2File(log, path, true);
	}

	public static void log2File(String log, String path, boolean append) {
		synchronized (mLogLock) {
			writeFile(log + "\r\n", path, append);
		}
	}

	public static boolean writeFile(String content, String path, boolean append) {
        try {
            return writeFile(content.getBytes(), path, append);
        } catch (IOException e) {
            Log.e(mTag,e.getMessage());
            return false;
        }
    }

	public static boolean writeFile(byte[] content, String path, boolean append) throws IOException {
		boolean res = false;
		File f = new File(path);
		RandomAccessFile raf = null;
		try {
			if (f.exists()) {
				if (!append) {
					f.delete();
					f.createNewFile();
				}
			} else {
				f.createNewFile();
			}
			if (f.canWrite()) {
				raf = new RandomAccessFile(f, "rw");
				raf.seek(raf.length());
				raf.write(content);
				res = true;
			}
		} catch (Exception e) {
			Log.e(mTag,e.getMessage());
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
		return res;
	}

	/**
	 * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段起始点
	 * 
	 * @param msg
	 *            需要输出的msg
	 */
	public static void msgStartTime(String msg) {
		mTimestamp = System.currentTimeMillis();
		if (!TextUtils.isEmpty(msg)) {
			e("[Started：" + mTimestamp + "]" + msg);
		}
	}

	/** 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段结束点* @param msg 需要输出的msg */
	public static void elapsed(String msg) {
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - mTimestamp;
		mTimestamp = currentTime;
		e("[Elapsed：" + elapsedTime + "]" + msg);
	}

	public static <T> void printList(List<T> list) {
		if (list == null || list.size() < 1) {
			return;
		}
		int size = list.size();
		i("---begin---");
		for (int i = 0; i < size; i++) {
			i(i + ":" + list.get(i).toString());
		}
		i("---end---");
	}

	public static <T> void printArray(T[] array) {
		if (array == null || array.length < 1) {
			return;
		}
		int length = array.length;
		i("---begin---");
		for (int i = 0; i < length; i++) {
			i(i + ":" + array[i].toString());
		}
		i("---end---");
	}
}
