package com.mw.audit.router;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.audit.AuditException;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.configuration.AuditConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(
	configurationPid = "com.liferay.portal.security.audit.configuration.AuditConfiguration",
	service = AuditRouter.class
)
public class CustomAuditRouter implements AuditRouter {
	
	// MW START
	// TODO Externalize this to configuration for example...
	String[] allowedClasses = {DLFileEntry.class.getCanonicalName(), DLFolder.class.getCanonicalName()};
	// MW END

	@Override
	public boolean isDeployed() {
		Set<String> keys = _serviceTrackerMap.keySet();

		if (keys.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public void route(AuditMessage auditMessage) throws AuditException {
		if (!_auditEnabled) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Audit disabled, not processing message: " + auditMessage);
			}

			return;
		}
		
		// MW START
		String className = auditMessage.getClassName();

		if (!Arrays.asList(allowedClasses).contains(className)) {
			_log.info("Skipping " + className);
			
			return;
		} else {
			_log.info("Processing " + className);
		}
		// MW END		

		List<AuditMessageProcessor> globalAuditMessageProcessors =
			_serviceTrackerMap.getService(StringPool.STAR);

		if (globalAuditMessageProcessors != null) {
			for (AuditMessageProcessor globalAuditMessageProcessor :
					globalAuditMessageProcessors) {

				globalAuditMessageProcessor.process(auditMessage);
			}
		}

		List<AuditMessageProcessor> auditMessageProcessors =
			_serviceTrackerMap.getService(auditMessage.getEventType());

		if (auditMessageProcessors != null) {
			for (AuditMessageProcessor auditMessageProcessor :
					auditMessageProcessors) {

				auditMessageProcessor.process(auditMessage);
			}
		}
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {
		
		_log.info("Activating...");

		AuditConfiguration auditConfiguration =
			ConfigurableUtil.createConfigurable(
				AuditConfiguration.class, properties);

		_auditEnabled = auditConfiguration.enabled();

		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, AuditMessageProcessor.class,
			AuditConstants.EVENT_TYPES);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	@Modified
	protected void modified(Map<String, Object> properties) {
		AuditConfiguration auditConfiguration =
			ConfigurableUtil.createConfigurable(
				AuditConfiguration.class, properties);

		_auditEnabled = auditConfiguration.enabled();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomAuditRouter.class);

	private volatile boolean _auditEnabled;
	private ServiceTrackerMap<String, List<AuditMessageProcessor>>
		_serviceTrackerMap;

}