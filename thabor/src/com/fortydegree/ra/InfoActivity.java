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
package com.fortydegree.ra;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;

import com.fortydegree.ra.data.TreeSpecies;
import com.fortydegree.ra.data.wikipedia.WikipediaClient;
import com.fortydegree.utils.webservice.WebService;

public class InfoActivity extends Activity {

	private static final String TAG = InfoActivity.class.getName();
	private static final WikipediaClient wikipedia = new WikipediaClient(new WebService(
			ApplicationConstants.WIKIPEDIA_ENDPOINT));
	private Bundle extras = null;
	private Handler progressHandler = new Handler();
	private ProgressDialog mProgress = null;
	private WebView webView = null;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.info);

		mProgress = ProgressDialog.show(this, null, getResources().getString(R.string.loading), true);

		Button button = (Button) findViewById(R.id.btnClick);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		webView = (WebView) findViewById(R.id.textdescription);
		webView.setBackgroundColor(0);

		extras = getIntent().getExtras();

		new Thread(new Runnable() {
			public void run() {

				String title = extras.getString(ApplicationConstants.METADATA_TITLE);
				String type = extras.getString(ApplicationConstants.METADATA_TYPE);
				String text = null;

				if (type != null) {
					if (type.equals(ApplicationConstants.METADATA_TYPE_TREE)) {

						String treeCode = TreeSpecies.getSpecieCode(
								extras.getString(ApplicationConstants.METADATA_TREE_SPECIE),
								extras.getString(ApplicationConstants.METADATA_TREE_SPECIE_COM));

						if (treeCode != null) {
							treeCode = treeCode.toLowerCase();
							text = wikipedia.getPage(treeCode);
						}
					} else if (type.equals(ApplicationConstants.METADATA_TYPE_POI)) {
						text = extras.getString(ApplicationConstants.METADATA_CONTENT);
						text = text.replaceAll("\n", "<br>");
					}
				}

				if (text == null) {
					text = getResources().getString(R.string.noInfo);
				} else {

					try {
						text = new String(text.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}

				text = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><style>h1, h2{color:#6FC700} body{color:white}</style></head><body><h1>"
						+ title + "</h1>" + text + "</body></html>";
				webView.loadData(text, "text/html; charset=utf-8", "utf-8");

				progressHandler.post(new Runnable() {
					public void run() {
						mProgress.dismiss();
					}
				});
			}
		}).start();

	}

}
