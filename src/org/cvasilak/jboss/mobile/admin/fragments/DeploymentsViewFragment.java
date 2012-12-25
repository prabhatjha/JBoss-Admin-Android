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
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;

import java.util.Map;

public class DeploymentsViewFragment extends SherlockListFragment {

    private static final String TAG = DeploymentsViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    private DeploymentAdapter adapter;

    private String group;

    private int selectedItemPos = -1;
    private ActionMode mActionMode;

    public static DeploymentsViewFragment newInstance(String group) {
        DeploymentsViewFragment f = new DeploymentsViewFragment();

        Bundle args = new Bundle();
        args.putString("group", group);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        if (getArguments() != null)
            this.group = getArguments().getString("group");

        application = (JBossAdminApplication) getActivity().getApplication();

        adapter = new DeploymentAdapter();
        setListAdapter(adapter);

        // inform runtime that we have action buttons
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "@onActivityCreated()");

        // Define the contextual action mode
        //registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                if (mActionMode != null) {
                    return false;
                }

                selectedItemPos = position;
                getListView().setItemChecked(position, true);

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = getSherlockActivity().startActionMode(new ActionModeCallback());

                return true;
            }
        });

        refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_add_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.deployments_context_refresh) {
            refresh();

            return (true);
        }

        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {


    }

    private final class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            getSherlockActivity().getSupportMenuInflater().inflate(R.menu.deployments_list_context_menu,
                    menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem item = menu.findItem(R.id.deployments_context_action);

            final Deployment deployment = adapter.getItem(selectedItemPos);

            item.setTitle(deployment.enabled ? R.string.disable : R.string.enable);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final Deployment deployment = adapter.getItem(selectedItemPos);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    getActivity());

            switch (item.getItemId()) {
                case R.id.deployments_context_delete:
                    alertDialog
                            .setTitle(getString(R.string.dialog_confirm_delete_title))
                            .setMessage(String.format(getString(R.string.dialog_confirm_delete_msg),
                                    deployment.name))
                            .setIcon(R.drawable.ic_action_delete)
                            .setNegativeButton(getString(R.string.dialog_button_NO),
                                    null)
                            .setPositiveButton(getString(R.string.dialog_button_YES),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

                                            application.getOperationsManager().removeDeployment(deployment.name, group, new Callback() {
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
                                    }).show();

                    mActionMode.finish();

                    return true;
                case R.id.deployments_context_action:
                    alertDialog
                            .setTitle(getString(R.string.dialog_confirm_deployment_title))
                            .setMessage(String.format(getString(R.string.dialog_confirm_deployment_action),
                                    deployment.enabled ? "disable" : "enable", deployment.name))
                            .setNegativeButton(getString(R.string.dialog_button_NO),
                                    null)
                            .setPositiveButton(getString(R.string.dialog_button_YES),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.applyingAction));

                                            application.getOperationsManager().changeDeploymentStatus(deployment.name, group, !deployment.enabled, new Callback() {
                                                @Override
                                                public void onSuccess(JsonElement reply) {
                                                    progress.dismiss();

                                                    deployment.enabled = !deployment.enabled;

                                                    adapter.notifyDataSetChanged();
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

                    mActionMode.finish();
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        application.getOperationsManager().fetchDeployments(group, new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                adapter.clear();

                String name, runtimeName;
                boolean enabled = false;

                JsonObject jsonObj = reply.getAsJsonObject();

                for (Map.Entry<String, JsonElement> e : jsonObj.entrySet()) {
                    name = e.getKey();

                    JsonObject detailsJsonObj = e.getValue().getAsJsonObject();

                    if (detailsJsonObj.get("enabled") != null)
                        enabled = detailsJsonObj.get("enabled").getAsBoolean();

                    runtimeName = detailsJsonObj.get("runtime-name").getAsString();

                    adapter.add(new Deployment(name, runtimeName, enabled));
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

    class DeploymentAdapter extends ArrayAdapter<Deployment> {
        DeploymentAdapter() {
            super(getSherlockActivity(), R.layout.deployment_row);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            DeploymentHolder holder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = inflater.inflate(R.layout.deployment_row, parent, false);
                holder = new DeploymentHolder(row);
                row.setTag(holder);

            } else {
                holder = (DeploymentHolder) row.getTag();
            }

            holder.populateFrom(getItem(position));

            return (row);
        }
    }

    class DeploymentHolder {
        ImageView icon = null;
        TextView name = null;

        DeploymentHolder(View row) {
            this.icon = (ImageView) row.findViewById(R.id.deployment_icon);
            this.name = (TextView) row.findViewById(R.id.deployment_name);
        }

        void populateFrom(Deployment deployment) {
            name.setText(deployment.name);
            // check and set correct icon for the deployment
            icon.setImageResource((deployment.enabled ? R.drawable.ic_deployment_up : R.drawable.ic_deployment_down));
        }
    }

    class Deployment {
        String name;
        String runtimeName;
        boolean enabled;

        Deployment(String name, String runtimeName, boolean enabled) {
            this.name = name;
            this.runtimeName = runtimeName;
            this.enabled = enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Deployment that = (Deployment) o;

            if (enabled != that.enabled) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (runtimeName != null ? !runtimeName.equals(that.runtimeName) : that.runtimeName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (runtimeName != null ? runtimeName.hashCode() : 0);
            result = 31 * result + (enabled ? 1 : 0);
            return result;
        }
    }
}