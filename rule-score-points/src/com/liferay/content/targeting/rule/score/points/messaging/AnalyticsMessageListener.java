/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.content.targeting.rule.score.points.messaging;

import com.liferay.content.targeting.rule.score.points.api.model.ScorePointsAssigner;
import com.liferay.osgi.util.service.ServiceTrackerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;

import javax.portlet.UnavailableException;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Eudaldo Alonso
 */
public class AnalyticsMessageListener implements MessageListener {

	@Override
	public void receive(Message message) {
		try {
			doReceive(message);
		}
		catch (Exception e) {
			_log.error("Unable to process message " + message, e);
		}
	}

	protected void doReceive(Message message) throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		if (bundle == null) {
			throw new UnavailableException(
				"Can't find a reference to the OSGi bundle") {

				@Override
				public boolean isPermanent() {
					return true;
				}
			};
		}

		ScorePointsAssigner scorePointsAssigner =
			ServiceTrackerUtil.getService(
				ScorePointsAssigner.class, bundle.getBundleContext());

		String className = message.getString("className");
		long classPK = message.getLong("classPK");
		long anonymousUserId = message.getLong("anonymousUserId");
		long groupId = message.getLong("scopeGroupId");

		try {
			scorePointsAssigner.assignPoints(
				groupId, anonymousUserId, className, classPK);
		}
		catch (NullPointerException npe) {
			_log.error("No score point assigners available");
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		AnalyticsMessageListener.class);

}