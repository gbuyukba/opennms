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
package org.opennms.netmgt.dao;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.C3P0ConnectionFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.OnmsDistPoller;

public class DistPollerDaoTest extends BaseDaoTestCase {
    
    public DistPollerDaoTest() throws MarshalException, ValidationException, IOException, PropertyVetoException, SQLException {
        /*
         * Note: I'm using the opennms-database.xml file in target/classes/etc
         * so that it has been filtered first.
         */
        DataSourceFactory.setInstance(new C3P0ConnectionFactory("../opennms-daemon/target/classes/etc/opennms-database.xml"));
    }

    public void testBogus() {
        // do nothing... we're here so JUnit doesn't complain
    }
	
	public void FIXMEtestCreate() {
        OnmsDistPoller distPoller = new OnmsDistPoller("otherpoller", "192.168.7.7");   
        distPoller.setLastEventPull(new Date(1000000));
        getDistPollerDao().save(distPoller);
        
    }
    
    public void FIXMEtestGet() {
        assertNull(getDistPollerDao().get("otherpoller"));
        
        FIXMEtestCreate();
        
        OnmsDistPoller distPoller = getDistPollerDao().get("otherpoller");
        assertNotNull(distPoller);
        assertEquals(new Date(1000000), distPoller.getLastEventPull());
        
    }


}
