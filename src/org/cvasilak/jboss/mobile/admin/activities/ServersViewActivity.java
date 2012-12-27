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

package org.cvasilak.jboss.mobile.admin.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonElement;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.model.ServersManager;
import org.cvasilak.jboss.mobile.admin.net.Callback;

public class ServersViewActivity extends SherlockListActivity {

    private static final String TAG = ServersViewActivity.class.getSimpleName();

    public final static String ID_EXTRA = "org.cvasilak.servers._ID";

    private JBossAdminApplication application;
    private ServersManager serversManager;

    private ServerAdapter adapter;

    private int selectedItemPos = -1;

    private ProgressDialog progress;

    private ActionMode mActionMode;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = ((JBossAdminApplication) getApplication());
        serversManager = application.getServersManager();

        adapter = new ServerAdapter();

        setListAdapter(adapter);

        // Define the contextual action mode
        //registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                if (mActionMode != null) {
                    return false;
                }

                selectedItemPos = position;
                getListView().setItemChecked(position, true);

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = startActionMode(new ActionModeCallback());

                return true;
            }
        });

        Callback callback = (Callback) getLastNonConfigurationInstance();

        if (callback != null) {
            if (!application.getOperationsManager().isTaskFinished()) {
                progress = ProgressDialog.show(this, "", getString(R.string.connecting));
                application.getOperationsManager().attach(callback);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater()
                .inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            startActivity(new Intent(ServersViewActivity.this,
                    ServerDetailActivity.class));

            return (true);
        }

        return (super.onOptionsItemSelected(item));
    }

    private final class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            getSupportMenuInflater().inflate(R.menu.context_menu_servers_list,
                    menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.servers_context_edit:
                    // Launch activity to view/edit the currently selected item
                    Intent i = new Intent(ServersViewActivity.this,
                            ServerDetailActivity.class);

                    i.putExtra(ID_EXTRA, selectedItemPos);
                    startActivity(i);

                    mActionMode.finish();
                    return true;

                case R.id.servers_context_delete:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            ServersViewActivity.this);

                    alertDialog
                            .setTitle(String.format(getString(R.string.dialog_confirm_action_title), getString(R.string.action_delete)))
                            .setMessage(String.format(getString(R.string.dialog_confirm_action_body),
                                    getString(R.string.action_delete), serversManager.serverAtIndex(selectedItemPos).getName()))

                            .setIcon(R.drawable.ic_action_delete)
                            .setNegativeButton(getString(R.string.dialog_button_NO),
                                    null)
                            .setPositiveButton(getString(R.string.dialog_button_YES),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            // Delete the server that the context
                                            // menu is for
                                            serversManager.removeServerAtIndex(selectedItemPos);
                                            try {
                                                serversManager.save();
                                            } catch (Exception e) {
                                                Log.d(TAG, "exception on save", e);

                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                                        ServersViewActivity.this);

                                                alertDialog
                                                        .setTitle(R.string.dialog_error_title)
                                                        .setMessage(R.string.error_on_save)
                                                        .setPositiveButton(R.string.dialog_button_Bummer, null)
                                                        .setCancelable(false)
                                                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                                            }

                                            // TODO: deprecated
                                            adapter.notifyDataSetChanged();
                                        }
                                    }).show();

                    mActionMode.finish();

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        application.setCurrentActiveServer(serversManager.serverAtIndex(position));

        progress = ProgressDialog.show(this, "", getString(R.string.connecting));

        application.getOperationsManager().fetchJBossVersion(new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.hide();

                Intent i = new Intent(ServersViewActivity.this,
                        RootViewActivity.class);

                startActivity(i);

                Log.d(TAG, reply.getAsString());
            }

            @Override
            public void onFailure(Exception e) {
                progress.hide();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        ServersViewActivity.this);

                alertDialog
                        .setTitle(R.string.dialog_error_title)
                        .setMessage(e.getMessage())
                        .setPositiveButton(R.string.dialog_button_Bummer, null)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return application.getOperationsManager().getCallback();
    }

    class ServerAdapter extends ArrayAdapter<Server> {
        ServerAdapter() {
            super(ServersViewActivity.this, android.R.layout.simple_list_item_2, serversManager.getServers());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem row;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
            } else {
                row = (TwoLineListItem) convertView;
            }

            Server server = getItem(position);

            row.getText1().setText(server.getName());
            row.getText2().setText(server.getHostname() + ":" + server.getPort());

            return (row);
        }
    }
}