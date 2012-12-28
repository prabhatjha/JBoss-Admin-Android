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
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.net.JBossOperationsManager.DataSourceType;

public class DataSourcesViewFragment extends SherlockListFragment {

    private static final String TAG = DataSourcesViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    private DataSourceAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        adapter = new DataSourceAdapter();
        setListAdapter(adapter);

        // inform runtime that we have an action button (refresh)
        setHasOptionsMenu(true);

        refresh();
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

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        DataSource selectedDS = adapter.getItem(position);
        DataSourceMetricsViewFragment fragment = DataSourceMetricsViewFragment.newInstance(selectedDS.name, selectedDS.type);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right, android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);

        transaction.addToBackStack(null)
                .replace(android.R.id.content, fragment, null)
                .commit();
    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        application.getOperationsManager().fetchDataSourceList(new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                adapter.clear();

                JsonObject jsonObj = reply.getAsJsonObject();

                JsonArray jsonDsList = jsonObj.getAsJsonObject("step-1").getAsJsonArray("result");
                for (JsonElement ds : jsonDsList) {
                    adapter.add(new DataSource(ds.getAsString(), DataSourceType.StandardDataSource));
                }

                JsonArray jsonXADsList = jsonObj.getAsJsonObject("step-2").getAsJsonArray("result");
                for (JsonElement ds : jsonXADsList) {
                    adapter.add(new DataSource(ds.getAsString(), DataSourceType.XADataSource));
                }
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

    class DataSourceAdapter extends ArrayAdapter<DataSource> {
        DataSourceAdapter() {
            super(getActivity(), R.layout.datasource_row);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            DataSourceHolder holder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = inflater.inflate(R.layout.datasource_row, parent, false);
                holder = new DataSourceHolder(row);
                row.setTag(holder);

            } else {
                holder = (DataSourceHolder) row.getTag();
            }

            holder.populateFrom(getItem(position));

            return (row);
        }
    }

    static class DataSourceHolder {
        ImageView icon = null;
        TextView name = null;

        DataSourceHolder(View row) {
            this.icon = (ImageView) row.findViewById(R.id.datasource_icon);
            this.name = (TextView) row.findViewById(R.id.datasource_name);
        }

        void populateFrom(DataSource ds) {
            name.setText(ds.name);
            // check and set the XA icon to distinqush XA Data sources
            icon.setImageResource((ds.type == DataSourceType.XADataSource ? R.drawable.ic_xa_ds : 0));
        }
    }

    class DataSource {
        String name;
        DataSourceType type;

        DataSource(String name, DataSourceType type) {
            this.name = name;
            this.type = type;
        }
    }
}