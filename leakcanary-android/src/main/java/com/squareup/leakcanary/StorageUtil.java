package com.squareup.leakcanary;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StorageUtil {
	private final static String TAG = "StorageUtil";
	
	/**得到应用存储区的可用空间大小，大小为kb
	 * @param context 用来得到应用路径的上下文
	 * @return 可用空间大小
	 */
	@SuppressWarnings("deprecation")
	public static long getApplicationDirFreeMemoryByKB(Context context) {
		long blockSize = 0;
		long totalBlocks = 0;
		try {
			File path = new File(getApplicationPath(context));
			StatFs stat = new StatFs(path.getPath());
			blockSize = stat.getBlockSize();
			totalBlocks = stat.getAvailableBlocks();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalBlocks * blockSize / 1024;

	}

	/**得到sd卡的可用空间，大小为kb
	 * @return 可用空间大小
	 */
	@SuppressWarnings("deprecation")
	public static long getSDCardFreeMemoryByKB() {
		long freeKb = 0;
		try {
			File sdcardDir = new File(getSDCardPath());
			StatFs sf = new StatFs(sdcardDir.getPath());
			long bSize = sf.getBlockSize();
			long availBlocks = sf.getAvailableBlocks();

			freeKb = bSize * availBlocks / 1024;

		} catch (Exception e) {

			e.printStackTrace();
		}
		return freeKb;

	}
	
	/**
	 * @return 得到sd卡的存储路径
	 */
	public static String getSDCardPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	/**根据上下文(即：context)，得到其应用的存储路径
	 * @param context 上下文
	 * @return 应用的存储路径
	 */
	public static String getApplicationPath(Context context){
		return context.getApplicationContext().getFilesDir().getAbsolutePath();
	}
	
	/**判断是否存在sd卡
	 * @return 有SD卡返回true，否则false
	 */
	public static boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}
	
	/** 判断文件夹是否存在， 如果不存在会去创建，创建失败返回false
	 * @param filePath 文件路径
	 * @return true or false
	 */
	public static boolean isDirExist(String filePath){
		File file = new File(filePath);
		if(!file.exists()){
			if(file.mkdirs()){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
	
	/** 判断文件或者文件夹是否存在
	 * @param filePath 路径
	 * @return true or false
	 */
	public static boolean isFileExist(String filePath){
		File file = new File(filePath);
		if(file.exists()){
			return true;
		}else{
			return false;
		}
	}
	
	/** 如果文件不存在，返回false
	 * @param filePath
	 * @return 删除成功返回true
	 */
	public static boolean deleteFile(String filePath){
		File file = new File(filePath);
		if(file.exists()){
			return file.delete();
		}else{
			return false;
		}
	}
	
	/**写对象数据到文件
	 * @param obj
	 * @param file
	 */
	public static void writeObjectToFile(Object obj, File file) {
		if(file.exists()){
			file.delete();
		}
		
		FileOutputStream out = null;
		ObjectOutputStream objOut = null;
		try {
			file.createNewFile();
			out = new FileOutputStream(file);
			objOut = new ObjectOutputStream(out);
			objOut.writeObject(obj);
			objOut.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeSilently(out);
			closeSilently(objOut);
		}
	}
	
	public static final void closeSilently(Closeable close) {
		if (close == null) {
			return;
		}
		
		try {
			close.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**从文件中读出对象
	 * @param file
	 * @return
	 */
	public static Object readObjectFromFile(File file) {
		Object temp = null;
		FileInputStream in = null;
		ObjectInputStream objIn = null;
		try {
			in = new FileInputStream(file);
			objIn = new ObjectInputStream(in);
			temp = objIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			closeSilently(in);
			closeSilently(objIn);
		}
		return temp;
	}
	
	public static byte[] int2byteArray(int num) {
		byte[] result = new byte[4];
		result[0] = (byte) (num >>> 24);// 取最高8位放到0下标
		result[1] = (byte) (num >>> 16);// 取次高8为放到1下标
		result[2] = (byte) (num >>> 8); // 取次低8位放到2下标
		result[3] = (byte) (num); // 取最低8位放到3下标
		return result;
	}

	public byte[] long2byteArray(long num) {
		byte[] result = new byte[8];
		result[7] = (byte) (num >> 56);
		result[6] = (byte) (num >> 48);
		result[5] = (byte) (num >> 40);
		result[4] = (byte) (num >> 32);
		result[3] = (byte) (num >> 24);
		result[2] = (byte) (num >> 16);
		result[1] = (byte) (num >> 8);
		result[0] = (byte) (num >> 0);
		
		return result;
	} 

	public byte[] float2byteArray(float f) {
		int num = Float.floatToIntBits(f);
		byte[] result = new byte[4];
		result[3] = (byte) (num >> 24);
		result[2] = (byte) (num >> 16);
		result[1] = (byte) (num >> 8);
		result[0] = (byte) (num >> 0);
		return result;
	} 
	
	public byte[] double2byteArray(double d) {
		long num = Double.doubleToLongBits(d);
		byte[] result = new byte[8];
		result[7] = (byte) (num >> 56);
		result[6] = (byte) (num >> 48);
		result[5] = (byte) (num >> 40);
		result[4] = (byte) (num >> 32);
		result[3] = (byte) (num >> 24);
		result[2] = (byte) (num >> 16);
		result[1] = (byte) (num >> 8);
		result[0] = (byte) (num >> 0);
		
		return result;
	} 
}
