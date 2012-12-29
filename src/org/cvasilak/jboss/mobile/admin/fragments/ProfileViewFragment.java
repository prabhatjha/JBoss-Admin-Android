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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.model.Attribute;
import org.cvasilak.jboss.mobile.admin.model.ChildType;
import org.cvasilak.jboss.mobile.admin.net.Callback;
import org.cvasilak.jboss.mobile.admin.util.IconTextRowAdapter;
import org.cvasilak.jboss.mobile.admin.util.ParametersMap;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProfileViewFragment extends SherlockListFragment {

    private static final String TAG = ProfileViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    ArrayList<Attribute> attributes;
    ArrayList<ChildType> childTypes;

    private ArrayList<String> path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();

        MergeAdapter adapter = new MergeAdapter();

        TextView sectionHeader;

        // Section: Attributes
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText(R.string.attributes);
        adapter.addView(sectionHeader);

        attributes = new ArrayList<Attribute>();
        adapter.addAdapter(new AttributeAdapter(attributes));

        //Section ChildTypes
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        sectionHeader.setText(R.string.child_types);
        adapter.addView(sectionHeader);

        childTypes = new ArrayList<ChildType>();
        adapter.addAdapter(new ChildTypeAdapter(childTypes));

        // Section Operations
        sectionHeader = new TextView(getActivity());
        sectionHeader.setBackgroundColor(Color.DKGRAY);
        sectionHeader.setPadding(15, 10, 0, 10);
        adapter.addView(sectionHeader);

        adapter.addAdapter(new IconTextRowAdapter(getActivity(), Arrays.asList("Operations"), R.drawable.ic_operations));

        setListAdapter(adapter);

        // inform runtime that we have action buttons
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
        MergeAdapter adapter = ((MergeAdapter) list.getAdapter());

        String selection;

        if (position - 1 <= attributes.size()) {
            selection = attributes.get(position - 1).getName();
        } else if (position - 2 <= (attributes.size() + childTypes.size())) {
            selection = childTypes.get(position - 2 - attributes.size()).getName();
        } else {
            Log.d(TAG, "operation");
        }
    }

    public void refresh() {
        progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.queryingServer));

        ParametersMap step1 = ParametersMap.newMap()
                .add("operation", "read-resource")
                .add("address", this.path == null ? Arrays.asList("/") : this.path);

        ParametersMap step2 = ParametersMap.newMap()
                .add("operation", "read-children-types")
                .add("address", this.path == null ? Arrays.asList("/") : this.path);

        ParametersMap params = ParametersMap.newMap()
                .add("operation", "composite")
                .add("steps", Arrays.asList(step1, step2));

        application.getOperationsManager().genericRequest(params, new Callback() {
            @Override
            public void onSuccess(JsonElement reply) {
                progress.dismiss();

                // clear existing data
                attributes.clear();
                childTypes.clear();

                JsonObject jsonObj = reply.getAsJsonObject();

                // attributes
                JsonObject attrs = jsonObj.getAsJsonObject("step-1").getAsJsonObject("result");
                // children types
                JsonArray childs = jsonObj.getAsJsonObject("step-2").getAsJsonArray("result");

                // iterate attributes
                for (Map.Entry<String, JsonElement> e : attrs.entrySet()) {
                    String name = e.getKey();

                    boolean found = false;
                    // check if it exists in child types
                    for (JsonElement elem : childs) {
                        if (elem.getAsString().equals(name)) {
                            found = true;
                        }
                    }

                    if (!found) { // its an attribute
                        Attribute attr = new Attribute();

                        attr.setName(name);
                        attr.setValue(e.getValue());

                        attributes.add(attr);

                    } else { // its a child type
                        ChildType type = new ChildType();
                        type.setName(name);
                        type.setValue(e.getValue());

                        childTypes.add(type);
                    }
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
        }

        );
    }

    class AttributeAdapter extends ArrayAdapter<Attribute> {
        AttributeAdapter(List<Attribute> attributes) {
            super(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, attributes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem row = (TwoLineListItem) super.getView(position, convertView, parent);

            Attribute attr = getItem(position);

            row.getText1().setText(attr.getName());

            if (attr.getValue() != null)
                row.getText2().setText(attr.getValue().toString());
            else
                row.getText2().setText("");

            return (row);
        }
    }

    class ChildTypeAdapter extends ArrayAdapter<ChildType> {
        ChildTypeAdapter(List<ChildType> childTypes) {
            super(getActivity(), R.layout.icon_text_row, childTypes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ChildTypeHolder holder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = inflater.inflate(R.layout.icon_text_row, parent, false);
                holder = new ChildTypeHolder(row);
                row.setTag(holder);

            } else {
                holder = (ChildTypeHolder) row.getTag();
            }

            holder.populateFrom(getItem(position));

            return (row);
        }
    }

    static class ChildTypeHolder {
        ImageView icon = null;
        TextView name = null;

        ChildTypeHolder(View row) {
            this.icon = (ImageView) row.findViewById(R.id.row_icon);
            this.name = (TextView) row.findViewById(R.id.row_name);
        }

        void populateFrom(ChildType childType) {
            name.setText(childType.getName());
            icon.setImageResource(R.drawable.ic_folder);
        }
    }
}