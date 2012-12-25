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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import org.cvasilak.jboss.mobile.admin.R;

public class JMSTypeSelectorViewFragment extends SherlockListFragment {

    private static final String TAG = JMSTypeSelectorViewFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1, new String[]{"Queues", "Topics"});

        setListAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        String value = (String) list.getItemAtPosition(position);

        Fragment fragment = null;

        if (value.equals("Queues")) {
            fragment = new JMSQueuesViewController();
        } else if (value.equals("Topics")) {
            fragment = new JMSTopicsViewController();
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();

    }
}