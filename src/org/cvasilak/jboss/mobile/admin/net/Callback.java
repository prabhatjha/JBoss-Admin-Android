package org.cvasilak.jboss.mobile.admin.net;

import java.util.List;
import java.util.Map;

public interface Callback {

    public void onFailure(Exception e);

    interface FetchJBossVersionCallback extends Callback {
        public void onSuccess(String version);
    }

    interface FetchLaunchTypeCallback extends Callback {
        public void onSuccess(String launchType);
    }

    interface FetchActiveServerInfoCallback extends Callback {
        public void onSuccess(String host, String server);
    }

    interface FetchConfigurationInfoCallback extends Callback {
        public void onSuccess(Map<String, String> info);
    }

    interface FetchExtensionsCallback extends Callback {
        public void onSuccess(List<String> extensions);
    }

    interface FetchPropertiesCallback extends Callback {
        public void onSuccess(Map<String, String> info);
    }

    interface FetchJVMMetricsCallback extends Callback {
        public void onSuccess(Map<String, Map<String, String>> info);
    }

    interface FetchDataSourceListCallback extends Callback {
        public void onSuccess(Map<String, List<String>> info);
    }

    interface FetchDataSourceMetricsCallback extends Callback {
        public void onSuccess(Map<String, String> info);
    }

    interface FetchJMSMessagingModelListCallback extends Callback {
        public void onSuccess(List<String> list);
    }

    interface FetchJMSMetricsCallback extends Callback {
        public void onSuccess(Map<String, String> info);
    }

    interface FetchTransactionMetricsCallback extends Callback {
        public void onSuccess(Map<String, String> info);
    }

    interface FetchWebConnectorsListCallback extends Callback {
        public void onSuccess(List<String> list);
    }

    interface FetchWebConnectorMetricsCallback extends Callback {
        public void onSuccess(Map<String, String> info);
    }

}




