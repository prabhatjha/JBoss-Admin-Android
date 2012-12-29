package org.cvasilak.jboss.mobile.admin.net;

import android.app.Activity;
import android.content.Context;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.util.ParametersMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JBossOperationsManager {

    private static final String TAG = JBossOperationsManager.class.getSimpleName();

    private Context context;

    private Server server;
    private String domainHost;
    private String domainServer;

    private Callback callback;

    private TalkToJBossServerTask task;

    private boolean isDomainController;

    public static enum DataSourceType {StandardDataSource, XADataSource}

    public static enum JMSType {QUEUE, TOPIC}

    public JBossOperationsManager(Context context, Server server) {
        this.context = context;
        this.server = server;
    }

    public Callback getCallback() {
        return callback;
    }

    public void detach() {
        if (task != null)
            this.task.detach();
    }

    public void attach(Callback callback) {
        if (task != null)
            this.task.attach(callback);
    }

    public boolean isTaskFinished() {
        if (task != null)
            return task.isTaskFinished();

        return true;
    }

    public String getDomainHost() {
        return domainHost;
    }

    public String getDomainServer() {
        return domainServer;
    }

    public boolean isDomainController() {
        return isDomainController;
    }

    public void changeActiveMonitoringServer(String host, String server) {
        isDomainController = true;

        this.domainHost = host;
        this.domainServer = server;
    }

    private List<String> prefixAddressWithDomainServer(List<String> address) {
        if (isDomainController) {
            List<String> convAddress = new ArrayList<String>();
            convAddress.add("host");
            convAddress.add(domainHost);
            convAddress.add("server");
            convAddress.add(domainServer);
            convAddress.addAll(address);

            return convAddress;
        }

        return address;
    }

    private List<String> prefixAddressWithDomainGroup(String group, List<String> address) {
        if (isDomainController) {
            List<String> convAddress = new ArrayList<String>();

            if (group != null) {
                convAddress.add("server-group");
                convAddress.add(group);
            }

            if (!address.get(0).equals("/")) {
                convAddress.addAll(address);
            }

            return convAddress;
        }

        return address;
    }

    public void uploadFilename(File file, Activity activity, final Callback callback) {
        UploadToJBossServerTask task = new UploadToJBossServerTask(activity, server, callback);
        task.execute(file);
    }

    public void fetchJBossVersion(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-attribute")
                .add("name", "release-version");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchActiveServerInformation(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-resources")
                .add("child-type", "host");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchConfigurationInformation(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("/")))
                .add("include-runtime", Boolean.TRUE);

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchExtensionsInformation(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("/")))
                .add("child-type", "extension");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchPropertiesInformation(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-attribute")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("core-service", "platform-mbean", "type", "runtime")))
                .add("name", "system-properties");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchJVMMetrics(final Callback callback) {
        attach(callback);

        ParametersMap memoryParams = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("core-service", "platform-mbean", "type", "memory")))
                .add("include-runtime", Boolean.TRUE);

        ParametersMap threadingParams = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("core-service", "platform-mbean", "type", "threading")))
                .add("include-runtime", Boolean.TRUE);

        ParametersMap os = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("core-service", "platform-mbean", "type", "operating-system")))
                .add("include-runtime", Boolean.TRUE);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "composite")
                .add("steps", Arrays.asList(memoryParams, threadingParams, os));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchDataSourceList(final Callback callback) {
        attach(callback);

        ParametersMap dsParams = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "datasources")))
                .add("child-type", "data-source");

        ParametersMap xadsParams = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "datasources")))
                .add("child-type", "xa-data-source");

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "composite")
                .add("steps", Arrays.asList(dsParams, xadsParams));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchDataSourceMetrics(String dsName, DataSourceType dsType, final Callback callback) {
        attach(callback);

        ParametersMap poolParams = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "datasources",
                        (dsType == DataSourceType.XADataSource ? "xa-data-source" : "data-source"),
                        dsName,
                        "statistics", "pool")))
                .add("include-runtime", Boolean.TRUE);

        ParametersMap jdbcParams = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "datasources",
                        (dsType == DataSourceType.XADataSource ? "xa-data-source" : "data-source"),
                        dsName,
                        "statistics", "jdbc")))
                .add("include-runtime", Boolean.TRUE);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "composite")
                .add("steps", Arrays.asList(poolParams, jdbcParams));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchJMSMessagingModelList(JMSType type, final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "messaging",
                        "hornetq-server", "default")))
                .add("child-type", (type == JMSType.QUEUE ? "jms-queue" : "jms-topic"));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchJMSQueueMetrics(String name, final JMSType type, final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "messaging",
                        "hornetq-server", "default",
                        (type == JMSType.QUEUE ? "jms-queue" : "jms-topic"),
                        name)))
                .add("include-runtime", Boolean.TRUE);

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchTranscationMetrics(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "transactions")))
                .add("include-runtime", Boolean.TRUE);

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchWebConnectorsList(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "web")))
                .add("child-type", "connector");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchWebConnectorMetrics(String name, final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "web", "connector", name)))
                .add("include-runtime", Boolean.TRUE);

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchDeployments(String group, final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-resources")
                .add("address", prefixAddressWithDomainGroup(group, Arrays.asList("/")))
                .add("child-type", "deployment");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void fetchDomainGroups(final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-resources")
                .add("child-type", "server-group");

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void changeDeploymentStatus(String name, String group, boolean enable, final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", enable ? "deploy" : "undeploy")
                .add("address", prefixAddressWithDomainGroup(group, Arrays.asList("deployment", name)));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void removeDeployment(String name, String group, final Callback callback) {
        attach(callback);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "remove")
                .add("address", prefixAddressWithDomainGroup(group, Arrays.asList("deployment", name)));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void addDeploymentContent(String hash, String name, List<String> groups, boolean enable, final Callback callback) {
        attach(callback);

        ParametersMap params;

        ArrayList<ParametersMap> steps = new ArrayList<ParametersMap>();

        for (String group : groups) {

            HashMap<String, String> BYTES_VALUE = new HashMap<String, String>();
            BYTES_VALUE.put("BYTES_VALUE", hash);

            HashMap<String, HashMap<String, String>> HASH = new HashMap<String, HashMap<String, String>>();
            HASH.put("hash", BYTES_VALUE);

            params = ParametersMap.newMap()
                    .add("operation", "add")
                    .add("address", prefixAddressWithDomainGroup(group, Arrays.asList("deployment", name)))
                    .add("name", name)
                    .add("content", Arrays.asList(HASH));

            steps.add(params);

            if (enable) {
                params = ParametersMap.newMap()
                        .add("operation", "deploy")
                        .add("address", prefixAddressWithDomainGroup(group, Arrays.asList("deployment", name)));

                steps.add(params);
            }
        }

        params = ParametersMap.newMap()
                .add("operation", "composite")
                .add("steps", steps);

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void addDeploymentContent(String hash, String name, String runtimeName, final Callback callback) {
        attach(callback);

        HashMap<String, String> BYTES_VALUE = new HashMap<String, String>();
        BYTES_VALUE.put("BYTES_VALUE", hash);

        HashMap<String, HashMap<String, String>> HASH = new HashMap<String, HashMap<String, String>>();
        HASH.put("hash", BYTES_VALUE);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "add")
                .add("address", Arrays.asList("deployment", name))
                .add("name", name)
                .add("runtime-name", runtimeName)
                .add("content", Arrays.asList(HASH));

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }

    public void genericRequest(ParametersMap params, final Callback callback) {
        attach(callback);

        task = new TalkToJBossServerTask(context, server, callback);
        task.execute(params);
    }
}
