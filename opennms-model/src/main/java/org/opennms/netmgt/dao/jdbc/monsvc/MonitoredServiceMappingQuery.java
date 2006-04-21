//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.springframework.jdbc.object.MappingSqlQuery;

public class MonitoredServiceMappingQuery extends MappingSqlQuery {

    public MonitoredServiceMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT ifservices.nodeid, ifservices.ipAddr, ifservices.ifIndex, ifservices.serviceId, ifservices.lastGood, ifservices.lastFail, ifservices.qualifier, ifservices.status, ifservices.source, ifservices.notify "+clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Integer nodeId = (Integer)rs.getObject("ifservices.nodeid");       //nodeID                  integer,        
        String ipAddr  = rs.getString("ifservices.ipAddr");                //ipAddr                  varchar(16) not null,
        Integer ifIndex = (Integer)rs.getObject("ifservices.ifIndex");     //ifIndex                 integer,
        Integer serviceId = (Integer)rs.getObject("ifservices.serviceId"); //serviceID               integer,
        
        MonitoredServiceId id = new MonitoredServiceId(nodeId, ipAddr, ifIndex, serviceId);
    	
        LazyMonitoredService svc = (LazyMonitoredService)Cache.obtain(OnmsMonitoredService.class, id);
        svc.setLoaded(true);
        svc.setLastGood(rs.getTimestamp("ifservices.lastGood"));           //lastGood                timestamp without time zone,
        svc.setLastFail(rs.getTimestamp("ifservices.lastFail"));           //lastFail                timestamp without time zone,
        svc.setQualifier(rs.getString("ifservices.qualifier"));            //qualifier               char(16),
        svc.setStatus(rs.getString("ifservices.status"));                  //status                  char(1),
        svc.setSource(rs.getString("ifservices.source"));                  //source                  char(1),
        svc.setNotify(rs.getString("ifservices.notify"));                  //notify                  char(1),
        svc.setDirty(false);
        return svc;
    }
    
    public OnmsMonitoredService findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsMonitoredService findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsMonitoredService findUnique(Object[] objs) {
        List nodes = execute(objs);
        if (nodes.size() > 0)
            return (OnmsMonitoredService) nodes.get(0);
        else
            return null;
    }
    
    public Set findSet() {
        return findSet((Object[])null);
    }
    
    public Set findSet(Object obj) {
        return findSet(new Object[] { obj });
    }
    
    public Set findSet(Object[] objs) {
        List nodes = execute(objs);
        Set results = new JdbcSet(nodes);
        return results;
    }
    
}