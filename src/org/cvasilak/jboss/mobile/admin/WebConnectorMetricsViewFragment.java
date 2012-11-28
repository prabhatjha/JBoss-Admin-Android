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

package org.cvasilak.jboss.mobile.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;
import org.cvasilak.jboss.mobile.admin.model.Metric;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.util.MetricsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebConnectorMetricsViewFragment extends SherlockListFragment {

    private static final String TAG = "WebConnectorMetricsViewFragment";

    private JBossAdminApplication application;

    private ProgressDialog progress;

    ArrayList<Metric> generalMetrics;
    ArrayList<Metric> reqPerConnectorMetrics;

    private String connectorName;

    public static WebConnectorMetricsViewFragment newInstance(String name) {
        WebConnectorMetricsViewFragment f = new WebConnectorMetricsViewFragment();

        Bundle args = new Bundle();
        args.putString("connectorName", name);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        this.connectorName = getArguments().getString("connectorName");

        application = (JBossAdminApplication) getActivity().getApplication();

        // restore state in case of a configuration change (eg orientation)
        if (savedInstanceState != null) {
            generalMetrics = savedInstanceState.getParcelableArrayList("generalMetrics");
            reqPerConnectorMetrics = savedInstanceState.getParcelableArrayList("reqPerConnectorMetrics");
        }

        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;
        List<Metric> metrics;

        // Section: General
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("General");
        adapter.addView(sectionHeader);

        if (generalMetrics == null) {
            generalMetrics = new ArrayList<Metric>();

            generalMetrics.add(new Metric("Protocol", "protocol"));
            generalMetrics.add(new Metric("Bytes Sent", "bytesSent"));
            generalMetrics.add(new Metric("Bytes Received", "bytesReceived"));
        }

        MetricsAdapter generalMetricsAdapter = new MetricsAdapter(getSherlockActivity(), generalMetrics);
        adapter.addAdapter(generalMetricsAdapter);

        // Section: Request per Connector
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Request per Connector");
        adapter.addView(sectionHeader);

        if (reqPerConnectorMetrics == null) {
            reqPerConnectorMetrics = new ArrayList<Metric>();

            reqPerConnectorMetrics.add(new Metric("Request Count", "requestCount"));
            reqPerConnectorMetrics.add(new Metric("Error Count", "errorCount"));
            reqPerConnectorMetrics.add(new Metric("Processing Time (ms)", "processingTime"));
            reqPerConnectorMetrics.add(new Metric("Max Time (ms)", "maxTime"));
        }

        MetricsAdapter reqPerConnectorMetricsAdapter = new MetricsAdapter(getSherlockActivity(), reqPerConnectorMetrics);
        adapter.addAdapter(reqPerConnectorMetricsAdapter);

        setListAdapter(adapter);

        // inform runtime that we have an action button (refresh)
        setHasOptionsMenu(true);

        // refresh only if the fragment is created
        // for the first time (no previous state)
        if (savedInstanceState == null)
            refresh();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("generalMetrics", generalMetrics);
        outState.putParcelableArrayList("reqPerConnectorMetrics", reqPerConnectorMetrics);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.context_refresh) {
            refresh();

            return (true);
        }

        return (super.onOptionsItemSelected(item));
    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        application.getOperationsManager().fetchWebConnectorMetrics(connectorName, new Callback.FetchWebConnectorMetricsCallback() {
            @Override
            public void onSuccess(Map<String, String> info) {
                progress.dismiss();

                for (Metric metric : generalMetrics) {
                    metric.setValue(info.get(metric.getKey()));
                }

                for (Metric metric : reqPerConnectorMetrics) {
                    metric.setValue(info.get(metric.getKey()));
                }

                // refresh table
                ((MergeAdapter) getListAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                progress.dismiss();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        getActivity());

                alertDialog
                        .setTitle(R.string.dialog_error_title)
                        .setMessage(e.getMessage())
                        .setPositiveButton(R.string.dialog_button_Bummer, null)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();

            }
        });
    }
}