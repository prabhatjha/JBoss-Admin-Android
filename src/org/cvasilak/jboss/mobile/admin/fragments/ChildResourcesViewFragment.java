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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.util.IconTextRowAdapter;
import org.cvasilak.jboss.mobile.admin.util.ParametersMap;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class ChildResourcesViewFragment extends SherlockListFragment {

    private static final String TAG = ChildResourcesViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ArrayList<String> path;
    private String childTypeName;

    ArrayList<String> childResources;

    public static ChildResourcesViewFragment newInstance(ArrayList<String> path, String childTypeName) {
        ChildResourcesViewFragment f = new ChildResourcesViewFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("path", path);
        args.putString("childTypeName", childTypeName);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        if (getArguments() != null) {
            this.path = getArguments().getStringArrayList("path");
            this.childTypeName = getArguments().getString("childTypeName");
        }

        application = (JBossAdminApplication) getActivity().getApplication();

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
        String selection = (String) list.getAdapter().getItem(position);

        Fragment fragment = null;

        if (selection.equals(getString(R.string.undefined))) {
            // TODO: show generic operations
        } else {
            selection = childResources.get(position - 1);

            ArrayList<String> next = new ArrayList<String>();

            if (this.path != null)
                next.addAll(this.path);
            next.add(childTypeName);
            next.add(selection);

            fragment = ProfileViewFragment.newInstance(next);
        }

        // TODO: remove when all screens are implemented
        if (fragment == null)
            return;

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right, android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);

        transaction.addToBackStack(null)
                .replace(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                .commit();

    }

    public void refresh() {
        ParametersMap childParams = ParametersMap.newMap()
                .add("operation", "read-children-names")
                .add("address", this.path == null ? Arrays.asList("/") : this.path)
                .add("child-type", this.childTypeName);

        application.getOperationsManager().genericRequest(childParams, new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                childResources = new ArrayList<String>();

                // child resources
                JsonArray names = reply.getAsJsonArray();

                for (JsonElement elem : names) {
                    childResources.add(elem.getAsString());
                }

                // check if it has generic operations
                // an operation with name "add"
                ParametersMap operationsParams = ParametersMap.newMap()
                        .add("operation", "read-operation-names")
                        .add("address", Arrays.asList(childTypeName, "*"));

                application.getOperationsManager().genericRequest(operationsParams, new Callback() {
                    @Override
                    public void onSuccess(JsonElement reply) {
                        // children types
                        JsonArray genericOpsNames = reply.getAsJsonArray();

                        boolean hasGenericOps = false;
                        for (JsonElement elem : genericOpsNames) {
                            if (elem.getAsString().equals("add")) {
                                hasGenericOps = true;
                                break;
                            }
                        }

                        buildTable(hasGenericOps);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        buildTable(false);
                    }
                });
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


    private void buildTable(boolean hasGenericOps) {
        // build table
        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;

        if (childResources.size() != 0) {
            // Section: Child Resources
            sectionHeader = new TextView(getActivity());
            sectionHeader.setBackgroundColor(Color.DKGRAY);
            sectionHeader.setPadding(15, 10, 0, 10);
            sectionHeader.setText(R.string.child_resources);
            adapter.addView(sectionHeader);

            adapter.addAdapter(new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1, childResources));
        }

        if (hasGenericOps) {
            if (childResources.size() != 0) { // if the are previous items, add the splitter
                sectionHeader = new TextView(getActivity());
                sectionHeader.setBackgroundColor(Color.DKGRAY);
                sectionHeader.setPadding(15, 10, 0, 10);
                adapter.addView(sectionHeader);
            }

            adapter.addAdapter(new IconTextRowAdapter(getActivity(), Arrays.asList(getString(R.string.generic_operations)), R.drawable.ic_operations));
        }

        setListAdapter(adapter);
    }
}