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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import org.cvasilak.jboss.mobile.admin.util.commonsware.MergeAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileViewFragment extends SherlockListFragment {

    private static final String TAG = ProfileViewFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "@onActivityCreated()");

        Map<String, List<String>> table = new HashMap<String, List<String>>();

        table.put("FOO", Arrays.asList("BOO", "KOO"));
        table.put("TOO", Arrays.asList("EEEEE", "BBBBB", "CCCCC", "DDDD"));
        table.put("KOO", Arrays.asList("WWWW", "PPPP"));

        MergeAdapter adapter = new MergeAdapter();

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        for (Map.Entry<String, List<String>> entry : table.entrySet()) {
            // add section header
            TextView section = new TextView(getActivity());
            section.setBackgroundColor(Color.DKGRAY);
            section.setPadding(15, 10, 0, 10);
            section.setText(entry.getKey());
            adapter.addView(section);

            // add section data
            adapter.addAdapter(new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    entry.getValue()));
        }

        setListAdapter(adapter);
    }


    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction
                .setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
                .replace(android.R.id.content, new ConfigurationViewFragment())
                .addToBackStack(null)
                .commit();

        Log.d(TAG, String.valueOf(position));
    }
}