/*
 * Promenade au Thabor
 * Copyright (C) 2011 40degree (Marc Haussaire & Fabien Ric)
 *
 * http://www.40degree.com
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package com.fortydegree.utils.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.fortydegree.ra.data.CallBack;

public class Downloader {

	private WebService webservice;

	class Job {
		List<String> parameters;
		CallBack<String> callback;
		CallBack<Throwable> error;

		public Job(List<String> parameters, CallBack<String> callback, CallBack<Throwable> error) {
			this.parameters = parameters;
			this.callback = callback;
			this.error = error;
		}

		public void download() {
			try {
				InputStream st = webservice.get(parameters);
				String res = getHttpInputString(st);
				callback.execute(res);
			} catch (Exception e) {
				if (error != null)
					error.execute(e);
			}
		}
	}

	Thread thread;
	boolean stopped = false;
	BlockingQueue<Job> stack;

	public Downloader(WebService webservice) {
		this.webservice = webservice;
		this.stack = new ArrayBlockingQueue<Downloader.Job>(10);

		this.thread = new Thread() {
			public void run() {
				stopped = false;
				Job job;

				try {
					while (!stopped) {
						job = stack.take();
						job.download();
					}
				} catch (InterruptedException e) {
					stack.clear();
					stopped = true;
					return;
				}
			}
		};

	}

	public void start() {
		this.thread.start();
	}

	public void close() {
		this.thread.interrupt();
	}

	public void download(List<String> parameters, CallBack<String> onload, CallBack<Throwable> error) {
		try {
			this.stack.put(new Job(parameters, onload, error));
		} catch (InterruptedException e) {
			// ok do nothing
		}

	}

	public static String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
