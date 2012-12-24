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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.model.Metric;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.util.MetricsAdapter;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionMetricsViewFragment extends SherlockListFragment {

    private static final String TAG = TransactionMetricsViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    ArrayList<Metric> sucFailMetrics;
    ArrayList<Metric> failOriginMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        // restore state in case of a configuration change (eg orientation)
        if (savedInstanceState != null) {
            sucFailMetrics = savedInstanceState.getParcelableArrayList("sucFailMetrics");
            failOriginMetrics = savedInstanceState.getParcelableArrayList("failOriginMetrics");
        }

        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;

        // Section: Success/Failure Ratio
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Success/Failure Ratio");
        adapter.addView(sectionHeader);

        if (sucFailMetrics == null) {
            sucFailMetrics = new ArrayList<Metric>();

            sucFailMetrics.add(new Metric("Total", "number-of-transactions"));
            sucFailMetrics.add(new Metric("Commited", "number-of-committed-transactions"));
            sucFailMetrics.add(new Metric("Aborted", "number-of-aborted-transactions"));
            sucFailMetrics.add(new Metric("Timed Out", "number-of-timed-out-transactions"));
        }

        MetricsAdapter sucFailMetricsAdapter = new MetricsAdapter(getSherlockActivity(), sucFailMetrics);
        adapter.addAdapter(sucFailMetricsAdapter);

        // Section: Failure Origin
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Failure Origin");
        adapter.addView(sectionHeader);

        if (failOriginMetrics == null) {
            failOriginMetrics = new ArrayList<Metric>();

            failOriginMetrics.add(new Metric("Applications", "number-of-application-rollbacks"));
            failOriginMetrics.add(new Metric("Resources", "number-of-resource-rollbacks"));
        }

        MetricsAdapter failOriginMetricsAdapter = new MetricsAdapter(getSherlockActivity(), failOriginMetrics);
        adapter.addAdapter(failOriginMetricsAdapter);

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

        outState.putParcelableArrayList("sucFailMetrics", sucFailMetrics);
        outState.putParcelableArrayList("failOriginMetrics", failOriginMetrics);
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

        application.getOperationsManager().fetchTranscationMetrics(new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                JsonObject jsonObj = reply.getAsJsonObject();

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

                for (Metric metric : sucFailMetrics) {
                    metric.setValue(info.get(metric.getKey()));
                }

                for (Metric metric : failOriginMetrics) {
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