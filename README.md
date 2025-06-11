**Introduction**
- Use this proof of concept OSGi module to override the out of the box DefaultAuditRouter OSGi component to control the entities that are Audited in the environment.
- Build and deploy the com.mw.custom.audit.router-1.0.0.jar artifact
- Classes that are not explicitly included will be excluded from being Audited e.g.
```
[CustomAuditRouter:65] Skipping com.liferay.portal.kernel.model.User
```

**Improvements**
- The current logic is very simple - discard the Audit record if the entity class name (i.e. Resource Name) is not explicitly included in the hardcoded array:
```
String[] allowedClasses = {DLFileEntry.class.getCanonicalName(), DLFolder.class.getCanonicalName()};
```
- The logic could be changed to explicitly exclude certain classes rather than explicitly including certain classes.
- The logic could also be made more complex e.g. to also include or exclude Audit records based on additional Audit Message field values e.g. eventType or companyId etc.
- Move the configuration to custom System Settings.

**Notes**
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.
- This should be tested in a non-production environment.
- The module has been tested in a local environment with JDK 21 and Liferay DXP 2025.q1.0-lts
