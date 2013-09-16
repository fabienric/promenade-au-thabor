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
package com.fortydegree.ra.model.mvc;

import java.util.HashSet;
import java.util.Set;

public class BaseEventPublisher implements IEventPublisher {

	protected Set<IEventSubscriber> subscribers = null;
	protected Object source = null;

	public BaseEventPublisher(Object source) {
		this.source = source;
	}

	public void register(IEventSubscriber subscriber) {
		if (subscribers == null)
			subscribers = new HashSet<IEventSubscriber>();

		if (subscriber != null)
			subscribers.add(subscriber);
	}

	public void unregister(IEventSubscriber subscriber) {
		if (subscriber != null)
			subscribers.remove(subscriber);
	}

	public void publish(String eventName) {
		if (subscribers != null) {
			for (IEventSubscriber s : subscribers)
				s.eventReceived(eventName, source);
		}
	}
}
