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
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddDeploymentViewFragment extends SherlockListFragment {

    private static final String TAG = AddDeploymentViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    private ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_single_choice);

        setListAdapter(adapter);

        // inform runtime that we have an action button (refresh)
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

            final File file = new File(application.getLocalDeploymentsDirectory(), adapter.getItem(getListView().getCheckedItemPosition()));

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    getActivity());

            alertDialog
                    .setTitle(String.format(getString(R.string.dialog_confirm_action_title), getString(R.string.action_upload)))
                    .setMessage(String.format(getString(R.string.dialog_confirm_action_body),
                            getString(R.string.action_upload), file.getName()))
                    .setNegativeButton(getString(R.string.dialog_button_NO),
                            null)
                    .setPositiveButton(getString(R.string.dialog_button_YES),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    application.getOperationsManager().uploadFilename(file, getActivity(), new Callback() {

                                        @Override
                                        public void onSuccess(JsonElement reply) {
                                            getFragmentManager().popBackStack();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                                    getActivity());

                                            alertDialog
                                                    .setTitle(R.string.dialog_error_title)
                                                    .setMessage(e.getMessage())
                                                    .setPositiveButton(R.string.dialog_button_Bummer, null)
                                                    .setCancelable(false)
                                                    .setIcon(android.R.drawable.ic_dialog_alert).show();
                                        }
                                    }

                                    );

                                }
                            }).show();

            return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    public void refresh() {
        adapter.clear();

        File root = application.getLocalDeploymentsDirectory();

        List<String> files = Arrays.asList(root.list());

        if (files.size() == 0) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    getActivity());

            alertDialog
                    .setTitle(R.string.directory_empty_title)
                    .setMessage(String.format(getString(R.string.directory_empty_msg), root.getAbsolutePath()))
                    .setPositiveButton(R.string.dialog_button_Bummer, null)
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert).show();

            return;
        }

        Collections.sort(files);
        adapter.addAll(files);
    }
}