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
import java.util.Map;

public class JVMMetricsViewFragment extends SherlockListFragment {

    private static final String TAG = "JVMMetricsViewFragment";

    private JBossAdminApplication application;

    private ProgressDialog progress;

    ArrayList<Metric> osMetrics;
    ArrayList<Metric> heapMetrics;
    ArrayList<Metric> nonHeapMetrics;
    ArrayList<Metric> threadUsageMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        // restore state in case of a configuration change (eg orientation)
        if (savedInstanceState != null) {
            osMetrics = savedInstanceState.getParcelableArrayList("os");
            heapMetrics = savedInstanceState.getParcelableArrayList("heap-usage");
            nonHeapMetrics = savedInstanceState.getParcelableArrayList("nonHeap-usage");
            threadUsageMetrics = savedInstanceState.getParcelableArrayList("threading");
        }

        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;

        // Section: Operating System Information
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Operating System");
        adapter.addView(sectionHeader);

        if (osMetrics == null) {
            osMetrics = new ArrayList<Metric>();

            osMetrics.add(new Metric("Name", "name"));
            osMetrics.add(new Metric("Version", "version"));
            osMetrics.add(new Metric("Processors", "available-processors"));
        }

        MetricsAdapter osMetricsAdapter = new MetricsAdapter(getSherlockActivity(), osMetrics);
        adapter.addAdapter(osMetricsAdapter);

        // Section: Heap Usage
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Heap Usage");
        adapter.addView(sectionHeader);

        if (heapMetrics == null) {
            heapMetrics = new ArrayList<Metric>();

            heapMetrics.add(new Metric("Max", "max"));
            heapMetrics.add(new Metric("Used", "used"));
            heapMetrics.add(new Metric("Committed", "committed"));
            heapMetrics.add(new Metric("Init", "init"));
        }

        MetricsAdapter heapMetricsAdapter = new MetricsAdapter(getSherlockActivity(), heapMetrics);
        adapter.addAdapter(heapMetricsAdapter);

        // Section: Non Heap Usage
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Non Heap Usage");
        adapter.addView(sectionHeader);

        if (nonHeapMetrics == null) {
            nonHeapMetrics = new ArrayList<Metric>();

            nonHeapMetrics.add(new Metric("Max", "max"));
            nonHeapMetrics.add(new Metric("Used", "used"));
            nonHeapMetrics.add(new Metric("Committed", "committed"));
            nonHeapMetrics.add(new Metric("Init", "init"));
        }

        MetricsAdapter nonHeapMetricsAdapter = new MetricsAdapter(getSherlockActivity(), nonHeapMetrics);
        adapter.addAdapter(nonHeapMetricsAdapter);

        // Section: Thread Usage
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Thread Usage");
        adapter.addView(sectionHeader);

        if (threadUsageMetrics == null) {
            threadUsageMetrics = new ArrayList<Metric>();

            threadUsageMetrics.add(new Metric("Live", "thread-count"));
            threadUsageMetrics.add(new Metric("Daemon", "daemon"));
        }

        MetricsAdapter threadUsageMetricsAdapter = new MetricsAdapter(getSherlockActivity(), threadUsageMetrics);
        adapter.addAdapter(threadUsageMetricsAdapter);

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

        outState.putParcelableArrayList("os", osMetrics);
        outState.putParcelableArrayList("heap-usage", heapMetrics);
        outState.putParcelableArrayList("nonHeap-usage", nonHeapMetrics);
        outState.putParcelableArrayList("threading", threadUsageMetrics);
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

        application.getOperationsManager().fetchJVMMetrics(new Callback.FetchJVMMetricsCallback() {
            @Override
            public void onSuccess(Map<String, Map<String, String>> info) {
                progress.dismiss();

                Map<String, String> os = info.get("os");
                for (Metric metric : osMetrics) {
                    metric.setValue(os.get(metric.getKey()));
                }

                Map<String, String> heapUsage = info.get("heap-usage");
                for (Metric metric : heapMetrics) {
                    metric.setValue(heapUsage.get(metric.getKey()));
                }

                Map<String, String> nonHeapUsage = info.get("nonHeap-usage");
                for (Metric metric : nonHeapMetrics) {
                    metric.setValue(nonHeapUsage.get(metric.getKey()));
                }

                Map<String, String> threading = info.get("threading");
                for (Metric metric : threadUsageMetrics) {
                    metric.setValue(threading.get(metric.getKey()));
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