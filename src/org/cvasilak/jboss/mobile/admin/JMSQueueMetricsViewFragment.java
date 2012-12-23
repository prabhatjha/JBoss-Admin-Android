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
import org.cvasilak.jboss.mobile.admin.model.Metric;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.net.JBossOperationsManager.JMSType;
import org.cvasilak.jboss.mobile.admin.util.MetricsAdapter;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.ArrayList;
import java.util.Map;

public class JMSQueueMetricsViewFragment extends SherlockListFragment {

    private static final String TAG = JMSQueueMetricsViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    ArrayList<Metric> inFlightMetrics;
    ArrayList<Metric> msgProcessedMetrics;
    ArrayList<Metric> consumerMetrics;

    private String queueName;

    public static JMSQueueMetricsViewFragment newInstance(String name) {
        JMSQueueMetricsViewFragment f = new JMSQueueMetricsViewFragment();

        Bundle args = new Bundle();
        args.putString("queueName", name);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        this.queueName = getArguments().getString("queueName");

        application = (JBossAdminApplication) getActivity().getApplication();

        // restore state in case of a configuration change (eg orientation)
        if (savedInstanceState != null) {
            inFlightMetrics = savedInstanceState.getParcelableArrayList("inFlightMetrics");
            msgProcessedMetrics = savedInstanceState.getParcelableArrayList("msgProcessedMetrics");
            consumerMetrics = savedInstanceState.getParcelableArrayList("consumerMetrics");
        }

        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;

        // Section: In-Flight Messages
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("In-Flight Messages");
        adapter.addView(sectionHeader);

        if (inFlightMetrics == null) {
            inFlightMetrics = new ArrayList<Metric>();

            inFlightMetrics.add(new Metric("Messages In Queue", "message-count"));
            inFlightMetrics.add(new Metric("In Delivery", "delivering-count"));
        }

        MetricsAdapter inFlightMetricsAdapter = new MetricsAdapter(getSherlockActivity(), inFlightMetrics);
        adapter.addAdapter(inFlightMetricsAdapter);

        // Section: Messages Processed
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Messages Processed");
        adapter.addView(sectionHeader);

        if (msgProcessedMetrics == null) {
            msgProcessedMetrics = new ArrayList<Metric>();

            msgProcessedMetrics.add(new Metric("Messages Added", "messages-added"));
            msgProcessedMetrics.add(new Metric("Messages Scheduled", "scheduled-count"));
        }

        MetricsAdapter msgProcessedMetricsAdapter = new MetricsAdapter(getSherlockActivity(), msgProcessedMetrics);
        adapter.addAdapter(msgProcessedMetricsAdapter);

        // Section: Consumer
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText("Consumer");
        adapter.addView(sectionHeader);

        if (consumerMetrics == null) {
            consumerMetrics = new ArrayList<Metric>();

            consumerMetrics.add(new Metric("Number of Consumer", "consumer-count"));
        }

        MetricsAdapter consumerMetricsAdapter = new MetricsAdapter(getSherlockActivity(), consumerMetrics);
        adapter.addAdapter(consumerMetricsAdapter);

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

        outState.putParcelableArrayList("inFlightMetrics", inFlightMetrics);
        outState.putParcelableArrayList("msgProcessedMetrics", msgProcessedMetrics);
        outState.putParcelableArrayList("consumerMetrics", consumerMetrics);
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

        application.getOperationsManager().fetchJMSQueueMetrics(queueName, JMSType.QUEUE, new Callback.FetchJMSMetricsCallback() {
            @Override
            public void onSuccess(Map<String, String> info) {
                progress.dismiss();

                for (Metric metric : inFlightMetrics) {
                    metric.setValue(info.get(metric.getKey()));
                }

                for (Metric metric : msgProcessedMetrics) {
                    metric.setValue(info.get(metric.getKey()));
                }

                for (Metric metric : consumerMetrics) {
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