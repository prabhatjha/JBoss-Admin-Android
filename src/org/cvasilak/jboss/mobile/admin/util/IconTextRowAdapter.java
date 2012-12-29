package org.cvasilak.jboss.mobile.admin.util;

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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.cvasilak.jboss.mobile.admin.R;

import java.util.List;

// TODO
// Refactor to a generic class that users
// will pass an object that will have
// icon, text1, text2 getters (objects will implement the interface)
public class IconTextRowAdapter extends ArrayAdapter<String> {

    private int iconId;

    public IconTextRowAdapter(Context context, List<String> list, int iconId) {
        super(context, R.layout.icon_text_row, list);

        this.iconId = iconId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHolder holder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            row = inflater.inflate(R.layout.icon_text_row, parent, false);
            holder = new RowHolder(row);
            row.setTag(holder);

        } else {
            holder = (RowHolder) row.getTag();
        }

        holder.populate(getItem(position));

        return (row);
    }

    class RowHolder {
        ImageView icon = null;
        TextView name = null;

        RowHolder(View row) {
            this.icon = (ImageView) row.findViewById(R.id.row_icon);
            this.name = (TextView) row.findViewById(R.id.row_name);
        }

        void populate(String value) {
            name.setText(value);
            icon.setImageResource(iconId);
        }
    }

}
