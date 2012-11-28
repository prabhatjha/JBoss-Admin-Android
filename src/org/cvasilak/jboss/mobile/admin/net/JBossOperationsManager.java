package org.cvasilak.jboss.mobile.admin.net;

import android.content.Context;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.util.ParametersMap;

import java.util.*;

public class JBossOperationsManager {
    private static final String TAG = "JBossOperationsManager";

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
        this.callback = null;
    }

    public void attach(Callback callback) {
        this.callback = callback;
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

    public void fetchJBossVersion(final Callback.FetchJBossVersionCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-attribute")
                .add("name", "release-version");

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                if (callback != null)
                    callback.onSuccess(json.getAsString());
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchActiveServerInformation(final Callback.FetchActiveServerInfoCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-resources")
                .add("child-type", "host");

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject hosts = json.getAsJsonObject();

                String host = null;
                String server = null;

                for (Map.Entry<String, JsonElement> e : hosts.entrySet()) {
                    host = e.getKey();

                    JsonObject hostInfo = e.getValue().getAsJsonObject();
                    JsonObject servers = hostInfo.getAsJsonObject("server");

                    for (Map.Entry<String, JsonElement> p : servers.entrySet()) {
                        server = p.getKey();

                        break;
                    }
                    if (server != null)
                        break;
                }

                if (callback != null)
                    callback.onSuccess(host, server);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchConfigurationInformation(final Callback.FetchConfigurationInfoCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("/")))
                .add("include-runtime", Boolean.TRUE);

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();

                Map<String, String> info = new HashMap<String, String>();

                info.put("release-codename", jsonObj.get("release-codename").getAsString());
                info.put("release-version", jsonObj.get("release-version").getAsString());
                info.put("server-state", jsonObj.get("server-state").getAsString());

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchExtensionsInformation(final Callback.FetchExtensionsCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("/")))
                .add("child-type", "extension");

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonArray jsonArray = json.getAsJsonArray();

                List<String> extensions = new ArrayList<String>();

                for (JsonElement entry : jsonArray) {
                    extensions.add(entry.getAsString());
                }

                if (callback != null)
                    callback.onSuccess(extensions);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchPropertiesInformation(final Callback.FetchPropertiesCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-attribute")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("core-service", "platform-mbean", "type", "runtime")))
                .add("name", "system-properties");

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();

                Map<String, String> info = new LinkedHashMap<String, String>();

                for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
                    info.put(entry.getKey(), entry.getValue().toString());
                }

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchJVMMetrics(final Callback.FetchJVMMetricsCallback callback) {
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

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();
                Map<String, Map<String, String>> info = new HashMap<String, Map<String, String>>();

                JsonObject step1 = jsonObj.getAsJsonObject("step-1").getAsJsonObject("result");

                /* Memory */
                long max, committed, init, used;
                float usedPerc;

                // Heap Usage
                JsonObject jsonHeapUsage = step1.getAsJsonObject("heap-memory-usage");
                Map<String, String> heapUsage = new HashMap<String, String>();

                max = jsonHeapUsage.getAsJsonPrimitive("max").getAsLong() / 1024 / 1024;
                committed = jsonHeapUsage.getAsJsonPrimitive("committed").getAsLong() / 1024 / 1024;
                init = jsonHeapUsage.getAsJsonPrimitive("init").getAsLong() / 1024 / 1024;

                used = jsonHeapUsage.getAsJsonPrimitive("used").getAsLong() / 1024 / 1024;
                usedPerc = (max != 0 ? ((float) used / max) * 100 : 0);

                heapUsage.put("max", String.format("%d MB", max));
                heapUsage.put("used", String.format("%d MB (%.0f%%)", used, usedPerc));
                heapUsage.put("committed", String.format("%d MB", committed));
                heapUsage.put("init", String.format("%d MB", init));

                info.put("heap-usage", heapUsage);

                // Non Heap Usage
                JsonObject jsonNonHeapUsage = step1.getAsJsonObject("non-heap-memory-usage");
                Map<String, String> nonHeapUsage = new HashMap<String, String>();

                max = jsonNonHeapUsage.getAsJsonPrimitive("max").getAsLong() / 1024 / 1024;
                committed = jsonNonHeapUsage.getAsJsonPrimitive("committed").getAsLong() / 1024 / 1024;
                init = jsonNonHeapUsage.getAsJsonPrimitive("init").getAsLong() / 1024 / 1024;

                used = jsonNonHeapUsage.getAsJsonPrimitive("used").getAsLong() / 1024 / 1024;
                usedPerc = (max != 0 ? ((float) used / max) * 100 : 0);

                nonHeapUsage.put("max", String.format("%d MB", max));
                nonHeapUsage.put("used", String.format("%d MB (%.0f%%)", used, usedPerc));
                nonHeapUsage.put("committed", String.format("%d MB", committed));
                nonHeapUsage.put("init", String.format("%d MB", init));

                info.put("nonHeap-usage", nonHeapUsage);

                // Threading
                JsonObject jsonThreading = jsonObj.getAsJsonObject("step-2").getAsJsonObject("result");
                Map<String, String> threading = new HashMap<String, String>();

                int threadCount = jsonThreading.getAsJsonPrimitive("thread-count").getAsInt();
                int daemonThreadCount = jsonThreading.getAsJsonPrimitive("daemon-thread-count").getAsInt();
                float daemonUsedPerc = (threadCount != 0 ? ((float) daemonThreadCount / threadCount) * 100 : 0);

                threading.put("thread-count", String.format("%d", threadCount));
                threading.put("daemon", String.format("%d (%.0f%%)", daemonThreadCount, daemonUsedPerc));

                info.put("threading", threading);

                // OS
                JsonObject jsonOS = jsonObj.getAsJsonObject("step-3").getAsJsonObject("result");
                Map<String, String> os = new HashMap<String, String>();

                os.put("name", jsonOS.get("name").getAsString());
                os.put("version", jsonOS.get("version").getAsString());
                os.put("available-processors", jsonOS.get("available-processors").getAsString());

                info.put("os", os);

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchDataSourceList(final Callback.FetchDataSourceListCallback callback) {
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

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();
                Map<String, List<String>> info = new HashMap<String, List<String>>();

                JsonArray jsonDsList = jsonObj.getAsJsonObject("step-1").getAsJsonArray("result");
                List<String> dsList = new ArrayList<String>();

                for (JsonElement ds : jsonDsList) {
                    dsList.add(ds.getAsString());
                }

                info.put("ds", dsList);

                JsonArray jsonXADsList = jsonObj.getAsJsonObject("step-2").getAsJsonArray("result");
                List<String> dsXAList = new ArrayList<String>();

                for (JsonElement ds : jsonXADsList) {
                    dsXAList.add(ds.getAsString());
                }

                info.put("xa-ds", dsXAList);

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchDataSourceMetrics(String dsName, DataSourceType dsType, final Callback.FetchDataSourceMetricsCallback callback) {
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

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();
                Map<String, String> info = new HashMap<String, String>();

                JsonObject jsonPool = jsonObj.getAsJsonObject("step-1").getAsJsonObject("result");
                int availCount = jsonPool.getAsJsonPrimitive("AvailableCount").getAsInt();
                int activeCount = jsonPool.getAsJsonPrimitive("ActiveCount").getAsInt();
                int maxUsedCount = jsonPool.getAsJsonPrimitive("MaxUsedCount").getAsInt();

                float usedPerc = (availCount != 0 ? ((float) activeCount / availCount) * 100 : 0);

                info.put("AvailableCount", String.format("%d", availCount));
                info.put("ActiveCount", String.format("%d (%.0f%%)", activeCount, usedPerc));
                info.put("MaxUsedCount", String.format("%d", maxUsedCount));

                JsonObject jsonJDBC = jsonObj.getAsJsonObject("step-2").getAsJsonObject("result");
                int curSize = jsonJDBC.getAsJsonPrimitive("PreparedStatementCacheCurrentSize").getAsInt();
                int hitCount = jsonJDBC.getAsJsonPrimitive("PreparedStatementCacheHitCount").getAsInt();
                float hitPerc = (curSize != 0 ? ((float) hitCount / curSize) * 100 : 0);

                int misUsed = jsonJDBC.getAsJsonPrimitive("PreparedStatementCacheMissCount").getAsInt();
                float misPerc = (curSize != 0 ? ((float) misUsed / curSize) * 100 : 0);

                info.put("PreparedStatementCacheCurrentSize", String.format("%d", curSize));
                info.put("PreparedStatementCacheHitCount", String.format("%d (%.0f%%)", hitCount, hitPerc));
                info.put("PreparedStatementCacheMissCount", String.format("%d (%.0f%%)", misUsed, misPerc));

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchJMSMessagingModelList(JMSType type, final Callback.FetchJMSMessagingModelListCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "messaging",
                        "hornetq-server", "default")))
                .add("child-type", (type == JMSType.QUEUE ? "jms-queue" : "jms-topic"));

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonArray jsonArray = json.getAsJsonArray();

                List<String> list = new ArrayList<String>();

                for (JsonElement entry : jsonArray) {
                    list.add(entry.getAsString());
                }

                if (callback != null)
                    callback.onSuccess(list);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchJMSQueueMetrics(String name, final JMSType type, final Callback.FetchJMSMetricsCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "messaging",
                        "hornetq-server", "default",
                        (type == JMSType.QUEUE ? "jms-queue" : "jms-topic"),
                        name)))
                .add("include-runtime", Boolean.TRUE);

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();

                Map<String, String> info = new HashMap<String, String>();

                // common metrics
                int msgCount = jsonObj.getAsJsonPrimitive("message-count").getAsInt();
                int delivCount = jsonObj.getAsJsonPrimitive("delivering-count").getAsInt();
                float delivPerc = (msgCount != 0 ? ((float) delivCount / msgCount) * 100 : 0);
                int msgAdded = jsonObj.getAsJsonPrimitive("messages-added").getAsInt();

                info.put("message-count", String.format("%d", msgCount));
                info.put("delivering-count", String.format("%d (%.0f%%)", delivCount, delivPerc));
                info.put("messages-added", String.format("%d", msgAdded));

                switch (type) {
                    case QUEUE:   // extra metrics for Queue
                        int schCount = jsonObj.getAsJsonPrimitive("scheduled-count").getAsInt();
                        int consCount = jsonObj.getAsJsonPrimitive("consumer-count").getAsInt();

                        info.put("scheduled-count", String.format("%d", schCount));
                        info.put("consumer-count", String.format("%d", consCount));

                        break;

                    case TOPIC: // extra metrics for Topic
                        int durCount = jsonObj.getAsJsonPrimitive("durable-message-count").getAsInt();
                        float durPerc = (msgAdded != 0 ? ((float) durCount / msgAdded) * 100 : 0);

                        int nonDurCount = jsonObj.getAsJsonPrimitive("non-durable-message-count").getAsInt();
                        float nonDurPerc = (msgAdded != 0 ? ((float) nonDurCount / msgAdded) * 100 : 0);

                        int subCount = jsonObj.getAsJsonPrimitive("subscription-count").getAsInt();
                        int durSubCount = jsonObj.getAsJsonPrimitive("durable-subscription-count").getAsInt();
                        int nonDurSubCount = jsonObj.getAsJsonPrimitive("non-durable-subscription-count").getAsInt();

                        info.put("durable-message-count", String.format("%d (%.0f%%)", durCount, durPerc));
                        info.put("non-durable-message-count", String.format("%d (%.0f%%)", nonDurCount, nonDurPerc));
                        info.put("subscription-count", String.format("%d", subCount));
                        info.put("durable-subscription-count", String.format("%d", durSubCount));
                        info.put("non-durable-subscription-count", String.format("%d", nonDurSubCount));

                        break;

                    default:
                        break;
                }

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchTranscationMetrics(final Callback.FetchTransactionMetricsCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "transactions")))
                .add("include-runtime", Boolean.TRUE);

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();

                Map<String, String> info = new HashMap<String, String>();

                int total = jsonObj.getAsJsonPrimitive("number-of-transactions").getAsInt();
                int committed = jsonObj.getAsJsonPrimitive("number-of-committed-transactions").getAsInt();
                float committedPerc = (total != 0 ? ((float) committed / total) * 100 : 0);

                int aborted = jsonObj.getAsJsonPrimitive("number-of-aborted-transactions").getAsInt();
                float abortedPerc = (total != 0 ? ((float) aborted / total) * 100 : 0);

                int timedOut = jsonObj.getAsJsonPrimitive("number-of-timed-out-transactions").getAsInt();
                float timedOutPerc = (total != 0 ? ((float) timedOut / total) * 100 : 0);

                int appRollbacks = jsonObj.getAsJsonPrimitive("number-of-application-rollbacks").getAsInt();
                int resRollbacks = jsonObj.getAsJsonPrimitive("number-of-resource-rollbacks").getAsInt();

                info.put("number-of-transactions", String.format("%d", total));
                info.put("number-of-committed-transactions", String.format("%d (%.0f%%)", committed, committedPerc));
                info.put("number-of-aborted-transactions", String.format("%d (%.0f%%)", aborted, abortedPerc));
                info.put("number-of-timed-out-transactions", String.format("%d (%.0f%%)", timedOut, timedOutPerc));
                info.put("number-of-application-rollbacks", String.format("%d", appRollbacks));
                info.put("number-of-resource-rollbacks", String.format("%d", resRollbacks));

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchWebConnectorsList(final Callback.FetchWebConnectorsListCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "web")))
                .add("child-type", "connector");

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonArray jsonArray = json.getAsJsonArray();

                List<String> list = new ArrayList<String>();

                for (JsonElement entry : jsonArray) {
                    list.add(entry.getAsString());
                }

                if (callback != null)
                    callback.onSuccess(list);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }

    public void fetchWebConnectorMetrics(String name, final Callback.FetchWebConnectorMetricsCallback callback) {
        ParametersMap params = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", prefixAddressWithDomainServer(Arrays.asList("subsystem", "web", "connector", name)))
                .add("include-runtime", Boolean.TRUE);

        this.task = new TalkToJBossServerTask(context, server, new TalkToJBossServerTask.Reply() {
            @Override
            public void onSuccess(JsonElement json) {
                JsonObject jsonObj = json.getAsJsonObject();

                Map<String, String> info = new HashMap<String, String>();

                long bytesSent = jsonObj.getAsJsonPrimitive("bytesSent").getAsLong();
                long bytesReceived = jsonObj.getAsJsonPrimitive("bytesReceived").getAsLong();
                int requestCount = jsonObj.getAsJsonPrimitive("requestCount").getAsInt();
                int errorCount = jsonObj.getAsJsonPrimitive("errorCount").getAsInt();
                float errorPerc = (requestCount != 0 ? ((float) errorCount / requestCount) * 100 : 0);
                int processingTime = jsonObj.getAsJsonPrimitive("processingTime").getAsInt();
                int maxTime = jsonObj.getAsJsonPrimitive("maxTime").getAsInt();

                info.put("protocol", jsonObj.get("protocol").getAsString());
                info.put("bytesSent", String.format("%d", bytesSent));
                info.put("bytesReceived", String.format("%d", bytesReceived));
                info.put("requestCount", String.format("%d", requestCount));
                info.put("errorCount", String.format("%d (%.0f%%)", errorCount, errorPerc));
                info.put("processingTime", String.format("%d", processingTime));
                info.put("maxTime", String.format("%d", maxTime));

                if (callback != null)
                    callback.onSuccess(info);
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null)
                    callback.onFailure(e);
            }
        });

        this.task.execute(params);
    }
}
