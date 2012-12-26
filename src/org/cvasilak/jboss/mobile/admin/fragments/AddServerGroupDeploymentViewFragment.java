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
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.model.Deployment;
import org.cvasilak.jboss.mobile.admin.net.Callback;

import java.util.Arrays;
import java.util.Map;

public class AddServerGroupDeploymentViewFragment extends SherlockListFragment {

    private static final String TAG = AddServerGroupDeploymentViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    private ArrayAdapter<Deployment> adapter;

    private String group;
    private boolean showAll;

    public static AddServerGroupDeploymentViewFragment newInstance(String group, boolean showAll) {
        AddServerGroupDeploymentViewFragment f = new AddServerGroupDeploymentViewFragment();

        Bundle args = new Bundle();
        args.putString("group", group);
        args.putBoolean("showAll", showAll);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        if (getArguments() != null) {
            this.group = getArguments().getString("group");
            this.showAll = getArguments().getBoolean("showAll");
        }

        application = (JBossAdminApplication) getActivity().getApplication();

        adapter = new ArrayAdapter<Deployment>(
                getActivity(),
                android.R.layout.simple_list_item_single_choice);

        setListAdapter(adapter);

        // inform runtime that we have action buttons
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "@onActivityCreated()");

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            final Deployment deployment = adapter.getItem(getListView().getCheckedItemPosition());

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    getActivity());

            alertDialog.setTitle(R.string.dialog_confirm_deployment_operation)
                    .setItems(R.array.add_to_group_options, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (which == 2) // user cancelled
                                return;

                            progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.applyingAction));

                            application.getOperationsManager().addDeploymentContent(deployment.getBYTES_VALUE(), deployment.getName(), Arrays.asList(group), which == 0, new Callback() {
                                @Override
                                public void onSuccess(JsonElement reply) {
                                    progress.dismiss();

                                    getFragmentManager().popBackStack();
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
                    }).show();

            return (true);
        }

        return (super.onOptionsItemSelected(item));
    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        application.getOperationsManager().fetchDeployments(showAll ? null : this.group, new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                adapter.clear();

                String name, runtimeName, BYTES_VALUE;
                boolean enabled = false;

                JsonObject jsonObj = reply.getAsJsonObject();

                for (Map.Entry<String, JsonElement> e : jsonObj.entrySet()) {
                    name = e.getKey();

                    JsonObject detailsJsonObj = e.getValue().getAsJsonObject();

                    if (detailsJsonObj.get("enabled") != null)
                        enabled = detailsJsonObj.get("enabled").getAsBoolean();

                    runtimeName = detailsJsonObj.get("runtime-name").getAsString();

                    // "content" : [{"hash" : { "BYTES_VALUE" : "Pb4xyzgJmsxruKEf5eGOLu6lBjw="}}],
                    BYTES_VALUE = detailsJsonObj.get("content").getAsJsonArray().get(0).getAsJsonObject().
                            get("hash").getAsJsonObject().
                            get("BYTES_VALUE").getAsString();

                    adapter.add(new Deployment(name, runtimeName, enabled, BYTES_VALUE));
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

    public void addContent(final Deployment deployment) {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.applyingAction));

        application.getOperationsManager().addDeploymentContent(deployment.getBYTES_VALUE(), deployment.getName(), Arrays.asList(group), true, new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                adapter.remove(deployment);
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