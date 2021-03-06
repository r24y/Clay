package com.mypapyri.clay.event;

/*
 *  Copyright (c) 2012 Michael Zucchi
 *
 *  This file is part of ReaderZ, a Java e-ink reader application.
 *
 *  ReaderZ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReaderZ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReaderZ.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.awt.AWTEvent;
import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import com.mypapyri.clay.ClaySystem;

/**
 * Read the touch input on the kobo.
 * 
 * @author notzed
 * @author ryanthejuggler
 */
public class KoboTouchInput extends EventInput<TouchEvent, TouchEventListener> {

	/*
	 * AWTEvent type 0 (Sync) AWTEvent type 1 (Key) AWTEvent code 258 (Btn2)
	 * AWTEvent code 330 (Touch) AWTEvent type 3 (Absolute) AWTEvent code 0 (X)
	 * Value 308 Min 0 Max 1200 AWTEvent code 1 (Y) Value 328 Min 0 Max 1600
	 * AWTEvent code 16 (Hat0X) Value 0 Min 0 Max 1200 AWTEvent code 17 (Hat0Y)
	 * Value 0 Min 0 Max 1600 AWTEvent code 24 (Pressure) Value 0 Min 0 Max 2048
	 */

	ReadableByteChannel AWTEvent;
	ByteBuffer data = ByteBuffer.allocate(16).order(ByteOrder.nativeOrder());
	// initial press location - for filtering out small drags
	boolean dragged = false;

	int xdown;
	int ydown;
	long timedown;
	// current state
	public int xpos = 0;
	public int ypos = 0;
	int press = 0; // pressure
	public int touch;
	//
	boolean mouse1 = false;
	AWTEvent lastAWTEvent;

	public KoboTouchInput(String path) throws IOException {
		super();
		setReadableByteChannel(path);
	}

	Point start = null;
	long startTime = 0;
	Point end = null;
	long releaseTime = 0;

	public void readEvent() throws IOException {
		boolean sync = false;
		int xout = xpos;
		int yout = ypos;
		int pout = press;
		do {
			switch (readRawEvent()) {
			case 0:
				sync = true;
				break;
			case 1:
				switch (code) {
				case 258:
					break;
				case 330:
					touch = value;
					break;
				}
				break;
			case 3:
				switch (code) {
				case 0: // x
					xout = value;
					break;
				case 1: // y
					yout = value;
					break;
				case 24: // pressure
					pout = value;
					break;
				}
			}

		} while (!sync);

		if (touch > 0) {
			xpos = xout;
			ypos = yout;
			press = pout;
		}

		if (mouse1) // not sure what this does
		{
			if (touch == 0) // finger has been lifted
			{

				long timeup = time_s * 1000 + time_us / 1000;

				fireEvent(new TouchEvent(System.currentTimeMillis(),
						xout, yout, TouchEvent.MOUSE_RELEASED));

				mouse1 = false;

				// if we're in portrait mode, we have to adjust the coordinates
				releaseTime = timeup;
				// not dealing with swipes right now

				if (timeup - timedown > ClaySystem
						.getLongClickThreshold())
					fireEvent(
							new TouchEvent(System.currentTimeMillis(), xout,
									yout, TouchEvent.MOUSE_LONG_CLICKED));
				else
					fireEvent(new TouchEvent(System.currentTimeMillis(),
							xout, yout,TouchEvent.MOUSE_CLICKED));

			}
			// else if (dragged)
			// {
			// AWTEventType = AWTEventType.MouseDragged;
			// mouse1 = true;
			// }
			// else if (Math.abs(xdown - xpos) >= dragThreshold
			// || Math.abs(ydown - ypos) >= dragThreshold)
			// {
			// dragged = true;
			// AWTEventType = AWTEventType.MouseDragged;
			// mouse1 = true;
			// }
		} else {
			if (touch == 1) {
				mouse1 = true;
				dragged = false;
				xdown = xpos;
				ydown = ypos;
				timedown = time_s * 1000 + time_us / 1000;
				startTime = timedown;
				fireEvent(new TouchEvent(System.currentTimeMillis(),xpos,ypos,TouchEvent.MOUSE_PRESSED));
			}
		}
	}
	
	@Override
	public void fireEvent(TouchEvent ev) {
		
		//Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ev);
		
		
		if (!ClaySystem.isLandscape())
			ev = ev.toLandscape();
		
		int type = ev.getType();
		ArrayList<TouchEventListener> listeners = getListeners();

		for (TouchEventListener l : listeners) {

			switch (type) {
			case TouchEvent.MOUSE_CLICKED:
				l.onTap(ev);
				break;
			case TouchEvent.MOUSE_LONG_CLICKED:
				l.onLongTap(ev);
				break;
			case TouchEvent.MOUSE_PRESSED:
				l.onTouchDown(ev);
				break;
			case TouchEvent.MOUSE_RELEASED:
				l.onTouchUp(ev);
				break;
			case TouchEvent.MOUSE_DRAGGED:
				l.onDrag(ev);
				break;
			}
		}
	}
}
