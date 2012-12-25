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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.model.Metric;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.net.JBossOperationsManager.DataSourceType;
import org.cvasilak.jboss.mobile.admin.util.MetricsAdapter;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataSourceMetricsViewFragment extends SherlockListFragment {

    private static final String TAG = DataSourceMetricsViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    ArrayList<Metric> poolMetrics;
    ArrayList<Metric> prepStatementMetrics;

    private String dsName;
    private DataSourceType dsType;

    public static DataSourceMetricsViewFragment newInstance(String name, DataSourceType type) {
        DataSourceMetricsViewFragment f = new DataSourceMetricsViewFragment();

        Bundle args = new Bundle();
        args.putString("dsName", name);
        args.putString("dsType", type.name());
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        this.dsName = getArguments().getString("dsName");
        this.dsType = DataSourceType.valueOf(getArguments().getString("dsType"));

        application = (JBossAdminApplication) getActivity().getApplication();

        // restore state in case of a configuration change (eg orientation)
        if (savedInstanceState != null) {
            poolMetrics = savedInstanceState.getParcelableArrayList("poolMetrics");
            prepStatementMetrics = savedInstanceState.getParcelableArrayList("prepStatementMetrics");
        }

        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;

        // Section: Pool Usage
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Pool Usage");
        adapter.addView(sectionHeader);

        if (poolMetrics == null) {
            poolMetrics = new ArrayList<Metric>();

            poolMetrics.add(new Metric("Available", "AvailableCount"));
            poolMetrics.add(new Metric("Active Count", "ActiveCount"));
            poolMetrics.add(new Metric("Max Used", "MaxUsedCount"));
        }

        MetricsAdapter poolMetricsAdapter = new MetricsAdapter(getSherlockActivity(), poolMetrics);
        adapter.addAdapter(poolMetricsAdapter);

        // Section: Prepared Statement Pool Usage
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Prepared Statement Pool Usage");
        adapter.addView(sectionHeader);

        if (prepStatementMetrics == null) {
            prepStatementMetrics = new ArrayList<Metric>();

            prepStatementMetrics.add(new Metric("Current Size", "PreparedStatementCacheCurrentSize"));
            prepStatementMetrics.add(new Metric("Hit Count", "PreparedStatementCacheHitCount"));
            prepStatementMetrics.add(new Metric("Miss Used", "PreparedStatementCacheMissCount"));
        }

        MetricsAdapter prepStatementMetricsAdapter = new MetricsAdapter(getSherlockActivity(), prepStatementMetrics);
        adapter.addAdapter(prepStatementMetricsAdapter);

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

        outState.putParcelableArrayList("poolMetrics", poolMetrics);
        outState.putParcelableArrayList("prepStatementMetrics", prepStatementMetrics);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refresh();

            return (true);
        }

        return (super.onOptionsItemSelected(item));
    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        application.getOperationsManager().fetchDataSourceMetrics(this.dsName, this.dsType, new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                JsonObject jsonObj = reply.getAsJsonObject();

                Map<String, String> info = new HashMap<String, String>();

                JsonObject jsonPool = jsonObj.getAsJsonObject("step-1").getAsJsonObject("result");
                int availCount = jsonPool.getAsJsonPrimitive("AvailableCount").getAsInt();
                int activeCount = jsonPool.getAsJsonPrimitive("ActiveCount").getAsInt();
                int maxUsedCount = jsonPool.getAsJsonPrimitive("MaxUsedCount").getAsInt();

                float usedPerc = (availCount != 0 ? ((float) activeCount / availCount) * 100 : 0);

                info.put("AvailableCount", String.format("%d", availCount));
                info.put("ActiveCount", String.format("%d (%.0f%%)", activeCount, usedPerc));
                info.put("MaxUsedCount", String.format("%d", maxUsedCount));

                for (Metric metric : poolMetrics) {
                    metric.setValue(info.get(metric.getKey()));
                }

                JsonObject jsonJDBC = jsonObj.getAsJsonObject("step-2").getAsJsonObject("result");
                int curSize = jsonJDBC.getAsJsonPrimitive("PreparedStatementCacheCurrentSize").getAsInt();
                int hitCount = jsonJDBC.getAsJsonPrimitive("PreparedStatementCacheHitCount").getAsInt();
                float hitPerc = (curSize != 0 ? ((float) hitCount / curSize) * 100 : 0);

                int misUsed = jsonJDBC.getAsJsonPrimitive("PreparedStatementCacheMissCount").getAsInt();
                float misPerc = (curSize != 0 ? ((float) misUsed / curSize) * 100 : 0);

                info.put("PreparedStatementCacheCurrentSize", String.format("%d", curSize));
                info.put("PreparedStatementCacheHitCount", String.format("%d (%.0f%%)", hitCount, hitPerc));
                info.put("PreparedStatementCacheMissCount", String.format("%d (%.0f%%)", misUsed, misPerc));

                for (Metric metric : prepStatementMetrics) {
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