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

package org.cvasilak.jboss.mobile.admin.model;

public abstract class ManagementModelBase implements Comparable<ManagementModelBase> {

    public enum Type {
        STRING,
        INT,
        LONG,
        BIG_DECIMAL,
        BIG_INTEGER,
        DOUBLE,
        BOOLEAN,
        PROPERTY,
        OBJECT,
        BYTES,
        LIST,
        UNDEFINED
    }

    protected String name;
    protected String descr;
    protected Type type;
    protected Type valueType;

    public ManagementModelBase() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getValueType() {
        return valueType;
    }

    public void setValueType(Type valueType) {
        this.valueType = valueType;
    }

    // Allow sort by name
    @Override
    public int compareTo(ManagementModelBase another) {
        return name.compareTo(another.name);
    }
}
