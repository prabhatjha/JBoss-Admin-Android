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
import android.widget.ListView;
import android.widget.TwoLineListItem;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;

import java.util.HashMap;
import java.util.Map;

public class DomainServerGroupsFragment extends SherlockListFragment {

    private static final String TAG = JMSQueuesViewController.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    private GroupAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        adapter = new GroupAdapter();
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
        Group group = adapter.getItem(position);

        DeploymentsViewFragment fragment = DeploymentsViewFragment.newInstance(group.name);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();


    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        application.getOperationsManager().fetchDomainGroups(new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                adapter.clear();

                JsonObject jsonObj = reply.getAsJsonObject();

                String name, profile;
                boolean hasDeployments = false;

                for (Map.Entry<String, JsonElement> e : jsonObj.entrySet()) {
                    name = e.getKey();

                    Map<String, String> details = new HashMap<String, String>();
                    JsonObject detailsJsonObj = e.getValue().getAsJsonObject();

                    if (detailsJsonObj.get("deployment") != null)
                        hasDeployments = true;

                    profile = detailsJsonObj.get("profile").getAsString();

                    adapter.add(new Group(name, profile, hasDeployments));
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

    class GroupAdapter extends ArrayAdapter<Group> {
        GroupAdapter() {
            super(getSherlockActivity(), android.R.layout.simple_list_item_2);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem row;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
            } else {
                row = (TwoLineListItem) convertView;
            }

            Group group = getItem(position);

            row.getText1().setText(group.name);
            row.getText2().setText(group.profile);


            return (row);
        }
    }

    class Group {
        String name;
        String profile;
        boolean hasDeployments;

        Group(String name, String profile, boolean hasDeployments) {
            this.name = name;
            this.profile = profile;
            this.hasDeployments = hasDeployments;
        }
    }

}