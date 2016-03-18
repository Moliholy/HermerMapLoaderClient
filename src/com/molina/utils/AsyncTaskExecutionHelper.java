package com.molina.utils;

import java.util.concurrent.Executor;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * Clase que abstrae la ejecuci�n de AsyncTask en paralelo o en serie,
 * independientemente de la SDK de Android utilizada en el dispositivo.
 * 
 * @author Ricardo Lechuga (ricardo.cion@gmail.com)
 * 
 */
public abstract class AsyncTaskExecutionHelper {
	static class HoneycombExecutionHelper {
		@TargetApi(11)
		public static <P> void execute(AsyncTask<P, ?, ?> asyncTask,
				boolean parallel, P... params) {
			Executor executor = parallel ? AsyncTask.THREAD_POOL_EXECUTOR
					: AsyncTask.SERIAL_EXECUTOR;
			asyncTask.executeOnExecutor(executor, params);
		}
	}

	/**
	 * Ejecuta una AsyncTask en paralelo
	 * 
	 * @param asyncTask Tarea que queremos ejecutar
	 * @param params Par�metros de ejecuci�n de la tarea
	 */
	public static <P> void executeParallel(AsyncTask<P, ?, ?> asyncTask,
			P... params) {
		execute(asyncTask, true, params);
	}

	/**
	 * Ejecuta una AsyncTask en serie
	 * 
	 * @param asyncTask Tarea que queremos ejecutar
	 * @param params Par�metros de ejecuci�n de la tarea
	 */
	public static <P> void executeSerial(AsyncTask<P, ?, ?> asyncTask,
			P... params) {
		execute(asyncTask, false, params);
	}

	private static <P> void execute(AsyncTask<P, ?, ?> asyncTask,
			boolean parallel, P... params) {
		if (Build.VERSION.SDK_INT >= 11) {
			HoneycombExecutionHelper.execute(asyncTask, parallel, params);
		} else {
			asyncTask.execute(params);
		}
	}
}