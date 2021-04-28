package common.modules.util;

public class IThreadUtil {

	public static void trySleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void start(final Runnable runnable, final Runnable finalRunnable) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					finalRunnable.run();
				}
			}
		}).start();
	}

}
