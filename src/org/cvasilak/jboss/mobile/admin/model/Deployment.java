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

public class Deployment {

    private String name;
    private String runtimeName;
    private boolean enabled;
    private String BYTES_VALUE;

    public Deployment() {
    }

    public Deployment(String name, String runtimeName, boolean enabled, String BYTES_VALUE) {
        this.name = name;
        this.runtimeName = runtimeName;
        this.enabled = enabled;
        this.BYTES_VALUE = BYTES_VALUE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBYTES_VALUE() {
        return BYTES_VALUE;
    }

    public void setBYTES_VALUE(String BYTES_VALUE) {
        this.BYTES_VALUE = BYTES_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Deployment that = (Deployment) o;

        if (enabled != that.enabled) return false;
        if (BYTES_VALUE != null ? !BYTES_VALUE.equals(that.BYTES_VALUE) : that.BYTES_VALUE != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (runtimeName != null ? !runtimeName.equals(that.runtimeName) : that.runtimeName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (runtimeName != null ? runtimeName.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (BYTES_VALUE != null ? BYTES_VALUE.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}