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
package com.fortydegree.ra.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Handler;
import android.view.View;

import com.fortydegree.ra.R;
import com.fortydegree.ra.RAContext;
import com.fortydegree.ra.model.Marker;
import com.fortydegree.ra.model.POIFinder;
import com.fortydegree.ra.model.POIFinder.CalcResult;
import com.fortydegree.ra.model.POIFinder.FindInfo;
import com.fortydegree.ra.model.mvc.IEventSubscriber;

public class AugmentedView extends View implements IEventSubscriber {
	protected Paint paintFg;
	protected Paint paintText;
	protected Paint paintTextInfo;
	protected Paint paintTextTitle;
	protected Paint paintArrow;
	protected boolean debug = false;
	protected RAContext model = null;
	protected Shader shd;
	protected Paint paintBg;
	protected Paint paintFg2;
	protected int topInfo = 0;
	protected int panelHeight = 60;
	protected int inFrontArrowWidth = 24;
	protected int marginX = 1;
	protected int marginY = -8;
	protected RectF rect = new RectF();
	protected RectF infoRect = new RectF();
	protected Bitmap treeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
	protected TextLayout text = null;
	protected Path arrow = new Path();
	private final Handler handler = new ViewHandler(this);

	public AugmentedView(Context context) {
		this(context, null);
	}

	public AugmentedView(Context context, RAContext model) {
		super(context);
		createPaint();
		setModel(model);
	}

	public void setModel(RAContext model) {
		if (this.model != null) {
			this.model.getSensor().unregister(this);
			this.model.getFinder().unregister(this);
		}
		this.model = model;
		if (this.model != null) {
			this.model.getSensor().register(this);
			this.model.getFinder().register(this);
		}

		// FIXME
		this.model.getSensor().setAngle(0);
	}

	public void eventReceived(String eventName, Object source) {
		if (eventName.equals("update")) {
			handler.sendEmptyMessage(0);
		} else {
			this.invalidate();
		}
	}

	protected void createPaint() {
		shd = new LinearGradient(0, 0, getWidth(), panelHeight * 2 / 3, Color.rgb(80, 80, 80), Color.rgb(50, 50, 50),
				TileMode.MIRROR);
		paintBg = new Paint();
		paintBg.setStyle(Paint.Style.FILL_AND_STROKE);
		paintBg.setShader(shd);
		paintBg.setAntiAlias(true);
		paintBg.setAlpha(200);

		paintFg2 = new Paint();
		paintFg2.setStyle(Paint.Style.STROKE);
		paintFg2.setAntiAlias(true);
		paintFg2.setStrokeCap(Cap.ROUND);
		paintFg2.setStrokeWidth(2F);
		paintFg2.setARGB(200, 255, 255, 255);

		paintFg = new Paint();
		paintFg.setStrokeWidth(this.getWidth() / 100);
		paintFg.setStyle(Paint.Style.STROKE);
		paintFg.setColor(Color.WHITE);
		paintFg.setSubpixelText(true);
		paintFg.setAntiAlias(true);
		paintFg.setTextSize(18);

		paintText = new Paint();
		paintText.setStrokeWidth(this.getWidth() / 100);
		paintText.setStyle(Paint.Style.STROKE);
		paintText.setColor(Color.rgb(200, 200, 200));
		paintText.setSubpixelText(true);
		paintText.setAntiAlias(true);
		paintText.setTextSize(14);

		paintTextInfo = new Paint();
		paintTextInfo.setStrokeWidth(this.getWidth() / 100);
		paintTextInfo.setStyle(Paint.Style.STROKE);
		paintTextInfo.setColor(Color.rgb(111, 199, 0));
		paintTextInfo.setSubpixelText(true);
		paintTextInfo.setAntiAlias(true);
		paintTextInfo.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		paintTextInfo.setTextSize(14);

		paintTextTitle = new Paint();
		paintTextTitle.setStrokeWidth(this.getWidth() / 100);
		paintTextTitle.setStyle(Paint.Style.STROKE);
		paintTextTitle.setColor(Color.rgb(111, 199, 0));
		paintTextTitle.setSubpixelText(true);
		paintTextTitle.setAntiAlias(true);
		paintTextTitle.setTextAlign(Align.CENTER);
		paintTextTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		paintTextTitle.setTextSize(32);

		paintArrow = new Paint();
		paintArrow.setColor(Color.rgb(111, 199, 0));
		paintArrow.setStyle(Paint.Style.FILL);
		paintArrow.setAntiAlias(true);

		text = new TextLayout(paintFg);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (model == null || model.getFinder() == null || model.getSensor() == null)
			return;

		CalcResult calcRes = model.getFinder().getResultinfo();
		if (calcRes == null)
			return;

		float angle = calcRes.angle;
		float radius = calcRes.radius;
		float distance = calcRes.distance;

		// Information panel
		topInfo = this.getHeight() - marginY - panelHeight;
		rect.set(marginX, topInfo, this.getWidth() - marginX, this.getHeight() - marginY);

		canvas.drawRoundRect(rect, 8F, 8F, paintBg);
		canvas.drawRoundRect(rect, 8F, 8F, paintFg2);

		// Debug mode : draw location and angle

		if (debug) {

			text.start(10, this.getHeight() * 1 / 3, 20);
			text.drawline(canvas, "Debug");

			Location currentLocation = model.getSensor().getLocation();
			if (null != currentLocation) {
				text.drawline(canvas,
						"lat=" + currentLocation.getLatitude() + " long=" + currentLocation.getLongitude());
			}

			text.drawline(canvas, "trigAngle=" + model.getFinder().getTrigAngle(model.getSensor().getAngle()));
			text.drawline(canvas, "calculatedAngle=" + angle);
			text.drawline(canvas, "calculatedDistance=" + distance);
			text.drawline(canvas, "calculatedRadius=" + radius);
		}

		// Infos
		float textX = 46;
		float textY = topInfo + marginY + 30;
		float textDistX = getWidth() - 50;
		float textDistY = textY;
		float textInfosX = getWidth() - 70;
		float textInfosY = topInfo + 40;

		POIFinder.FindInfo info = model.getFinder().getInfo();
		Marker bestplace = model.getFinder().getTheGoodPlace();
		if (bestplace != null) {
			canvas.drawText(getResources().getString(R.string.moreInfo), textInfosX, textInfosY, paintTextInfo);

			String dist = formatDist(distance);
			String infoLabel = "";
			boolean isInFront = (info == FindInfo.OK);
			if (isInFront) {
				infoLabel = getResources().getString(R.string.inFrontOfYou);
			} else if (info == FindInfo.TOO_NEAR) {
				infoLabel = getResources().getString(R.string.nearYou);
			} else if (info == FindInfo.TOO_FAR) {
				infoLabel = getResources().getString(R.string.nextStep);
			}

			canvas.drawText(infoLabel, textX, textY, paintFg);
			canvas.drawText(bestplace.title, textX, textY + 20, paintFg);
			canvas.drawText(dist, textDistX, textDistY, paintText);

			int r = 12;
			float x = 0;
			float y = 0;
			arrow.reset();
			arrow.moveTo(x - r / 3, y + r);
			arrow.lineTo(x + r / 3, y + r);
			arrow.lineTo(x + r / 3, y);
			arrow.lineTo(x + r, y);
			arrow.lineTo(x, y - r);
			arrow.lineTo(x - r, y);
			arrow.lineTo(x - r / 3, y);
			arrow.close();

			paintPath(canvas, arrow, r, getHeight() - panelHeight / 2 - r + 4, r * 2, r * 2, -radius, paintArrow);

			if (isInFront) {
				arrow.reset();
				arrow.moveTo(0, 0);
				arrow.lineTo(inFrontArrowWidth / 2, -inFrontArrowWidth / 2);
				arrow.lineTo(inFrontArrowWidth, 0);
				arrow.close();

				paintPath(canvas, arrow, this.getWidth() / 2 - inFrontArrowWidth, topInfo, inFrontArrowWidth,
						-inFrontArrowWidth / 2, 0, paintArrow);
			}
		} else {
			canvas.drawText(getResources().getString(R.string.appName), 0, 0, paintTextTitle);
			canvas.drawBitmap(treeBitmap, (getWidth() - treeBitmap.getWidth()) / 2,
					(getHeight() - treeBitmap.getHeight()) / 2, paintFg);
			canvas.drawText(getResources().getString(R.string.searchPOINearby), textX, textY, paintFg);
			canvas.drawText(getResources().getString(R.string.checkYourLocation), textX, textY + 20, paintFg);
		}
	}

	public boolean isInfoPanelClicked(float x, float y) {
		return y >= topInfo;
	}

	private static String formatDist(float meters) {
		if (meters < 1000) {
			return ((int) meters) + "m";
		} else if (meters < 10000) {
			return formatDec(meters / 1000f, 1) + "km";
		} else {
			return ((int) (meters / 1000f)) + "km";
		}
	}

	private static String formatDec(float val, int dec) {
		int factor = (int) Math.pow(10, dec);

		int front = (int) (val);
		int back = (int) Math.abs(val * (factor)) % factor;

		return front + "." + back;
	}

	private static void paintPath(Canvas canvas, Path path, float x, float y, float width, float height,
			float rotation, Paint paint) {
		canvas.save();
		canvas.translate(x + width / 2, y + height / 2);
		canvas.rotate(rotation);
		canvas.drawPath(path, paint);
		canvas.restore();
	}
}
