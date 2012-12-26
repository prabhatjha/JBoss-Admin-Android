/*
 * JBoss Admin
 * Copyright 2012, Christos Vasilakis, and individual contributors.
 * See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.cvasilak.jboss.mobile.admin.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeViewFragment extends SherlockListFragment {

    private static final String TAG = RuntimeViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        // if not already set
        if (application.getOperationsManager().getDomainHost() == null) {
            // determine whether we are running in STANDALONE or DOMAIN mode
            progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.fetchingDomainInfo));

            application.getOperationsManager().fetchActiveServerInformation(new Callback() {
                @Override
                public void onSuccess(JsonElement reply) {
                    progress.dismiss();

                    JsonObject hosts = reply.getAsJsonObject();

                    String host = null;
                    String server = null;

                    for (Map.Entry<String, JsonElement> e : hosts.entrySet()) {
                        host = e.getKey();

                        JsonObject hostInfo = e.getValue().getAsJsonObject();
                        JsonObject servers = hostInfo.getAsJsonObject("server");

                        // TODO restructure loop
                        for (Map.Entry<String, JsonElement> p : servers.entrySet()) {
                            server = p.getKey();

                            break;
                        }
                        if (server != null)
                            break;
                    }

                    application.getOperationsManager().changeActiveMonitoringServer(host, server);

                    updateTable();
                }

                @Override
                public void onFailure(Exception e) {
                    progress.dismiss();

                    // HTTP/1.1 500 Internal Server Error
                    // occurred doing :read-children-resources(child-type=host)
                    // the server is running in standalone mode, we can live with that
                    updateTable();
                }
            });
        }
    }

    private void updateTable() {
        Map<String, List<String>> table = new HashMap<String, List<String>>();

        table.put("Server Status", Arrays.asList("Configuration", "JVM"));
        table.put("Subsystem Metrics", Arrays.asList("Data Sources", "JMS Destinations", "Transactions", "Web"));

        if (application.getOperationsManager().isDomainController()) {
            table.put("Deployments", Arrays.asList("Deployment Content", "Server Groups"));
        } else {
            table.put("Deployments", Arrays.asList("Manage Deployments"));
        }

        MergeAdapter adapter = new MergeAdapter();

        for (Map.Entry<String, List<String>> entry : table.entrySet()) {
            // add section header
            TextView section = new TextView(getActivity());
            section.setBackgroundColor(Color.DKGRAY);
            section.setPadding(15, 10, 0, 10);
            section.setText(entry.getKey());
            adapter.addView(section);

            // add section data
            adapter.addAdapter(new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    entry.getValue()));
        }

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        String value = (String) list.getItemAtPosition(position);

        Fragment fragment = null;

        if (value.equals("Configuration")) {
            fragment = new ConfigurationViewFragment();
        } else if (value.equals("JVM")) {
            fragment = new JVMMetricsViewFragment();
        } else if (value.equals("Data Sources")) {
            fragment = new DataSourcesViewFragment();
        } else if (value.equals("JMS Destinations")) {
            fragment = new JMSTypeSelectorViewFragment();
        } else if (value.equals("Transactions")) {
            fragment = new TransactionMetricsViewFragment();
        } else if (value.equals("Web")) {
            fragment = new WebConnectorTypeSelectorViewFragment();
        } else if (value.equals("Deployment Content")) {
            fragment = DeploymentsViewFragment.newInstance(null, DeploymentsViewFragment.Mode.DOMAIN_MODE);
        } else if (value.equals("Server Groups")) {
            fragment = new DomainServerGroupsFragment();
        } else if (value.equals("Manage Deployments")) {
            fragment = DeploymentsViewFragment.newInstance(null, DeploymentsViewFragment.Mode.STANDALONE_MODE);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }
}