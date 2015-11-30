package test;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * $ adb shell
 * root@android:/ # echo -BOC-
 * echo -BOC-
 * -BOC-
 * root@android:/ # id
 * id
 * uid=0(root) gid=0(root)
 * root@android:/ #
 * </pre>
 * 
 * shellbin : su sh /system/bin/su /system/xbin/su
 * 
 * @see https://github.com/Chainfire/libsuperuser/
 * @see http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 * @author fangss
 * 
 */
public class ShellHelper {
	private static final String TAG = "ShellHelper";
	public static final String lineSeparator = System.getProperty("line.separator", "\n");
	public static final String SU = "su";
	public static final String SH = "sh";
	/** byte[] UTF8_BYTES_OF_EXIT_LF = new byte[]{101, 120, 105, 116, 10}; */
	public static final String EXIT = "exit\n"; // "\nexit\n"
	private final Process mProcess;

	/**
	 * 读取程序的stdout和stderr都是阻塞的操作，这意味着必须在两个线程里分别读取
	 * 
	 * @param process
	 * @param gobbleStdout
	 * @param gobbleStderr
	 */
	public ShellHelper(Process process, InputStream stdout, InputStream stderr) {
		mProcess = process;
		// any error message? 一般来说，程序的错误消息不大，应该不会充斥整个缓冲区
		StreamGobbler.gobbleStream(process, stderr, "err_");
		// any output? 输出可以任意大小，很有可能充斥整个缓冲区
		StreamGobbler.gobbleStream(process, stdout, "out_");
	}

	/**
	 * 
	 * @param prog
	 * @see java.lang.Runtime#exec(String prog)
	 * @return if {@code Runtime.exec(String prog)} failed,
	 */
	public static ShellHelper exec(String prog) {
		log(TAG + "[^]", prog);
		try {
			Process process = Runtime.getRuntime().exec(prog);
			ShellHelper shell = new ShellHelper(process, process.getInputStream(), process.getErrorStream());
			return shell;
		} catch (IOException e) {
			// shell probably not found
			log(TAG, "Runtime.getRuntime().exec(" + prog + ")", e);
		}
		return null;
	}

	private static void log(String tag, String msg) {
		System.out.println(tag + ", " + msg);
	}

	private static void log(String tag, String msg, Throwable tr) {
		System.out.println(tag + ", " + msg + ", " + tr);
	}

	/**
	 * 不会自动flush
	 * 
	 * @param command
	 *          The <code>String</code> to be printed
	 */
	public ShellHelper print(String command) {
		if (null != command) {
			log(TAG + "[+]", command);
			writeUTF8(getOutputStream(), command, false);
		}
		return this;
	}

	/**
	 * 自动flush
	 * 
	 * @param command
	 *          The <code>String</code> to be printed
	 */
	public ShellHelper println(String command) {
		if (null != command) {
			print(command);
			println();
		}
		return this;
	}

	/**
	 * 自动flush Prints a newline.
	 */
	public ShellHelper println() {
		writeUTF8(getOutputStream(), lineSeparator, true);
		return this;
	}

	/**
	 * Returns an output stream that is connected to the standard input stream <em>(stdin)</em> of the native process represented
	 * by this object.
	 * 
	 * @return the output stream to write to the input stream associated with the native process.
	 * @see Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return null == mProcess ? null : mProcess.getOutputStream();
	}

	public int exit() {
		return exit(mProcess);
	}

	/**
	 * 
	 * @return The process's exit value
	 */
	public static int exit(Process process) {
		if (null != process) {
			// 在调用阻塞方法Process.waitFor()前：
			// 1. 迫使进程尽快执行退出命令，因为诸如Windows上cmd程序，linux上sh需要输入exit退出
			// 2. 如果有等待标准输出或者错误流的消耗者的线程，应该也会自动退出
			OutputStream stdin = process.getOutputStream();
			// writeUTF8(stdin, lineSeparator + "exit" + lineSeparator, true);
			writeUTF8(stdin, EXIT, true);

			// wait for our process to finish, while we gobble away in the background
			// 如果有等待标准输出或者错误流的消耗者的线程，则应该会自动退出
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				log(TAG, process.toString(), e);
			}

			// make sure our threads are done gobbling, our streams are closed,
			// and the process is destroyed - while the latter two shouldn't be
			// needed in theory, and may even produce warnings, in "normal" Java
			// they are required for guaranteed cleanup of resources, so lets be
			// safe and do this on Android as well
			// 标准输入流不再使用，先关闭它，以便任何等待标准输出或者错误流的消耗者得到信号，即 stdout/err返回EOF
			IoClose(stdin);

			IoClose(process.getErrorStream());
			IoClose(process.getInputStream());

			// Terminates this process and closes any associated streams
			process.destroy();
			return process.exitValue();
		}
		return 0;
	}

	/**
	 * 效率性能低
	 * <p>
	 * 
	 * Causes the current thread to wait, if necessary, until the subprocess represented by this {@code Process} object has
	 * terminated, or the specified waiting time elapses.
	 * 
	 * <p>
	 * If the subprocess has already terminated then this method returns immediately with the value {@code true}. If the process
	 * has not terminated and the timeout value is less than, or equal to, zero, then this method returns immediately with the
	 * value {@code false}.
	 * 
	 * <p>
	 * The default implementation of this methods polls the {@code exitValue} to check if the process has terminated. Concrete
	 * implementations of this class are strongly encouraged to override this method with a more efficient implementation.
	 * 
	 * @param timeout
	 *          the maximum time to wait
	 * @param unit
	 *          the time unit of the {@code timeout} argument
	 * @return {@code true} if the subprocess has exited and {@code false} if the waiting time elapsed before the subprocess has
	 *         exited.
	 * @throws InterruptedException
	 *           if the current thread is interrupted while waiting.
	 * @throws NullPointerException
	 *           if unit is null
	 * @since 1.8
	 */
	public static Integer waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
		long startTime = System.nanoTime();
		long rem = unit.toNanos(timeout);
		do {
			try {
				return process.exitValue();
			} catch (IllegalThreadStateException ex) {
				if (rem > 0)
					Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
			}
			rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
		} while (rem > 0);
		return null;
	}

	/**
	 * Attempts to deduce if the shell command refers to a su shell
	 * 
	 * @param shellbin
	 *          Shell command to run, e.g "su", "/system/bin/su", "/system/xbin/su"
	 * @return Shell command appears to be su
	 */
	public static boolean isSU(String shellbin) {
		// Strip parameters
		int pos = shellbin.indexOf(' ');
		if (pos >= 0) {
			shellbin = shellbin.substring(0, pos);
		}

		// Strip path
		pos = shellbin.lastIndexOf('/');
		if (pos >= 0) {
			shellbin = shellbin.substring(pos + 1);
		}

		return shellbin.equals("su");
	}

	public static int readAllLines(InputStream in, Printer lineGobbler) {
		return readAllLines(new BufferedReader(new InputStreamReader(in)), lineGobbler);
	}

	public static int readAllLines(BufferedReader br, Printer lineGobbler) {
		int lineNumber = 0;
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				if (null != lineGobbler) {
					lineGobbler.println(line);
				}
			}
		} catch (IOException e) {
			log(TAG, "read", e);
		}
		return lineNumber;
	}

	public interface Printer {
		/**
		 * @param line
		 *          the contents of the line or null if no characters were produced.
		 */
		public void println(String line);
	}

	/**
	 * Thread utility class continuously reading from an InputStream<br>
	 * 
	 * @diff 这个线程多增加了一个一层循环，线程不会自动退出，需要调用exit退出。
	 * @more java文档上说,由于有些本地平台为标准输入和输出流所提供的缓冲区大小有限,如果不能及时写入子进程的输入流或者读取子进程的输出流,可能导致子进程阻塞,甚至陷入死锁。
	 * @see https://github.com/Chainfire/libsuperuser/blob/master/libsuperuser/src/eu/chainfire/libsuperuser/StreamGobbler.java
	 * @see http://docs.oracle.com/javase/7/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
	 */
	public static class StreamGobbler extends Thread implements Printer {
		/** 只负责读，关闭由进程控制{@link Process#destroy()} */
		private final InputStream stream;

		/**
		 * <p>
		 * StreamGobbler constructor
		 * </p>
		 * 
		 * <p>
		 * We use this class because shell stdoutGobbler and stderrGobbler should be read as quickly as possible to prevent a
		 * deadlock from occurring, or Process.waitFor() never returning (as the buffer is full, pausing the native process)
		 * </p>
		 * 
		 * @param inputStream
		 *          InputStream to read from
		 */
		public StreamGobbler(InputStream inputStream) {
			stream = inputStream;
		}

		public static StreamGobbler gobbleStream(Process process, InputStream stream, String streamType) {
			if (null != stream) {
				StreamGobbler gobbler = new StreamGobbler(stream);
				gobbler.setName(process + "/" + streamType + Integer.toHexString(gobbler.hashCode()));
				gobbler.start();
				return gobbler;
			}
			return null;
		}

		@Override
		public void run() {
			log(getName(), "entrys run(); at " + System.currentTimeMillis());
			readAllLines(stream, this);
			log(getName(), "leaves run(); at " + System.currentTimeMillis());
		}

		@Override
		public void println(String line) {
			log(TAG, line);
		}
	}

	private static void writeUTF8(OutputStream out, String str, boolean flush) {
		if (null != out) {
			try {
				out.write(str.getBytes("UTF-8"));
				if (flush) {
					out.flush();
				}
			} catch (InterruptedIOException x) {
				log(TAG, "interrupt", x);
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				log(TAG, str, e);
			}
		}
	}

	public static <C extends Closeable> C IoClose(C c) {
		if (null != c)
			try {
				c.close();
			} catch (IOException e) {
				// IGNORED
			}
		return null;
	}

	public Process exec(String command, Printer outGobbler, Printer errGobbler) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		readAllLines(process.getInputStream(), outGobbler);
		readAllLines(process.getErrorStream(), errGobbler);
		return process;
	}

	public static Printer newPrinter(final PrintStream ps) throws IOException {
		return new Printer() {
			@Override
			public void println(String line) {
				ps.println(line);
			}
		};
	}

	public static void main(String[] args) {
		// ShellHelper shell = ShellHelper.exec("cmd");
		// shell.println("dir");
		// shell.println("ping 8.8.8.8");
		// shell.println("dir D:");
		// shell.exit();

		try {
			Process process = Runtime.getRuntime().exec("ping 127.0.0.1");
			System.err.println(readAllLines(process.getErrorStream(), null)); // newPrinter(System.err)
			System.err.println(readAllLines(process.getInputStream(), newPrinter(System.out))); //
			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
