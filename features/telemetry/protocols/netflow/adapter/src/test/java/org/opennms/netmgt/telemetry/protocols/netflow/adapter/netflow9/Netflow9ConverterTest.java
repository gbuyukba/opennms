/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow9;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.protocols.netflow.adapter.Utils.buildAndSerialize;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetflowConverter;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Netflow9ConverterTest {

    private NetflowConverter nf9Converter = new NetflowConverter();

    @Test
    public void canParseNetflow9Flows() {
        // Generate flows from existing packet payloads
        List<Flow> flows = getFlowsForPayloadsInSession("/flows/netflow9_template.dat", "/flows/netflow9_records.dat");
        assertThat(flows, hasSize(5));
        // Verify a flow
        Flow flow = flows.get(4);
        assertThat(flow.getNetflowVersion(), is(Flow.NetflowVersion.V9));
        assertThat(flow.getSrcAddr(), equalTo("10.1.20.85"));
        assertThat(flow.getSrcAddrHostname(), equalTo(Optional.empty()));
        assertThat(flow.getSrcPort(), equalTo(137));
        assertThat(flow.getDstAddr(), equalTo("10.1.20.127"));
        assertThat(flow.getDstAddrHostname(), equalTo(Optional.empty()));
        assertThat(flow.getDstPort(), equalTo(137));
        assertThat(flow.getProtocol(), equalTo(17)); // UDP
        assertThat(flow.getBytes(), equalTo(156L));
        assertThat(flow.getInputSnmp(), equalTo(369098754));
        assertThat(flow.getOutputSnmp(), equalTo(0));
        assertThat(flow.getFirstSwitched(), equalTo(1524773519000L)); // Thu Apr 26 16:11:59 EDT 2018
        assertThat(flow.getLastSwitched(), equalTo(1524773527000L)); // Thu Apr 26 16:12:07 EDT 2018
        assertThat(flow.getPackets(), equalTo(2L));
        assertThat(flow.getDirection(), equalTo(Flow.Direction.INGRESS));
        assertThat(flow.getNextHop(), equalTo("0.0.0.0"));
        assertThat(flow.getNextHopHostname(), equalTo(Optional.empty()));
        assertThat(flow.getVlan(), nullValue());
    }

    private List<Flow> getFlowsForPayloadsInSession(String... resources) {
        final List<byte[]> payloads = new ArrayList<>(resources.length);
        for (String resource : resources) {
            URL resourceURL = getClass().getResource(resource);
            try {
                payloads.add(Files.readAllBytes(Paths.get(resourceURL.toURI())));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return getFlowsForPayloadsInSession(payloads);
    }

    private List<Flow> getFlowsForPayloadsInSession(List<byte[]> payloads) {
        final List<Flow> flows = new ArrayList<>();
        final Session session = new TcpSession(InetAddress.getLoopbackAddress());
        for (byte[] payload : payloads) {
            final ByteBuf buffer = Unpooled.wrappedBuffer(payload);
            final Header header;
            try {
                header = new Header(slice(buffer, Header.SIZE));
                final Packet packet = new Packet(session, header, buffer);
                packet.getRecords().forEach(rec -> {

                    byte[] message = new byte[0];
                    try {
                        message = buildAndSerialize(Protocol.NETFLOW9, rec);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        FlowMessage flowMessage = FlowMessage.parseFrom(message);
                        flows.addAll(nf9Converter.convert(flowMessage, Instant.now()));
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }

                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }
        return flows;
    }


}
