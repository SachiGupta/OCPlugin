package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.SubnetType;
import net.juniper.contrail.api.types.VirtualNetwork;
import net.juniper.contrail.api.types.VnSubnetsType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronSubnet;
import org.opendaylight.controller.networkconfig.neutron.NeutronSubnet_IPAllocationPool;

/**
 * Test Class for Subnet.
 */
public class SubnetHandlerTest {
    SubnetHandler subnetHandler;
    VirtualNetwork mockedVirtualNetwork = mock(VirtualNetwork.class);
    SubnetHandler mockedSubnetHandler = mock(SubnetHandler.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);

    @Before
    public void beforeTest() {
        subnetHandler = new SubnetHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedVirtualNetwork);
        assertNotNull(mockedSubnetHandler);
    }

    @After
    public void afterTest() {
        subnetHandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Subnet */
    public NeutronSubnet defaultSubnetObject() {
        NeutronSubnet subnet = new NeutronSubnet();
        List<NeutronSubnet_IPAllocationPool> allocationPools = new ArrayList<NeutronSubnet_IPAllocationPool>();
        NeutronSubnet_IPAllocationPool neutronSubnet_IPAllocationPool = new NeutronSubnet_IPAllocationPool();
        subnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        subnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        subnet.setCidr("10.0.0.0/24");
        subnet.setGatewayIP("10.0.0.254");
        neutronSubnet_IPAllocationPool.setPoolStart("10.0.0.1");
        neutronSubnet_IPAllocationPool.setPoolEnd("10.0.0.254");
        allocationPools.add(neutronSubnet_IPAllocationPool);
        subnet.setAllocationPools(allocationPools);
        return subnet;
    }

    /* dummy params for Neutron Delta Subnet */
    public NeutronSubnet defaultDeltaSubnet() {
        NeutronSubnet subnet = new NeutronSubnet();
        subnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        subnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        subnet.setCidr("10.0.0.0/24");
        subnet.setGatewayIP("10.0.0.254");
        return subnet;
    }

    /* Test method to check if neutron subnet is null */
    @Test
    public void testCanCreateSubnetNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, subnetHandler.canCreateSubnet(null));
    }

    /* Test method to check if neutron subnet is null */
    @Test
    public void testCanCreateSubnetCidrNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet subnet = new NeutronSubnet();
        subnet.setCidr(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, subnetHandler.canCreateSubnet(subnet));
    }

    /* Test method to check if Gateway Ip is invalid */
    @Test
    public void testCanCreateSubnetInvalidGatewayIp() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        neutronSubnet.setGatewayIP("20.0.0.250");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, subnetHandler.canCreateSubnet(neutronSubnet));
    }

    /* Test method to check if virtual network is null */

    @Test
    public void testCanCreateSubnetVirtualNetworkNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, subnetHandler.canCreateSubnet(neutronSubnet));
    }

    /* Test method to check if subnet can be created with IpamRefs null */
    @Test
    public void testCanCreateSubnet() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, subnetHandler.canCreateSubnet(neutronSubnet));
    }

    /* Test method to check if subnet creation returns Internal Server Error */
    @Test
    public void testCanCreateSubnetException() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, subnetHandler.canCreateSubnet(neutronSubnet));
    }

    /* Test method to check if subnet already exists */
    @Test
    public void testCanCreateSubnetExists() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference<VnSubnetsType> ref = new ObjectReference<>();
        List<ObjectReference<VnSubnetsType>> ipamRefs = new ArrayList<ObjectReference<VnSubnetsType>>();
        List<VnSubnetsType.IpamSubnetType> subnets = new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
            subnetType.setSubnet(type);
            subnetType.getSubnet().setIpPrefix("10.0.0.0");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, subnetHandler.canCreateSubnet(neutronSubnet));
    }

    /* Test method to check ipPrefix */
    @Test
    public void testGetIpPrefix() {
        SubnetHandler.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        String cidr = "10.0.0.0/24";
        String[] ipPrefix = cidr.split("/");
        assertArrayEquals(ipPrefix, subnetHandler.getIpPrefix(neutronSubnet));
    }

    /* Test method to check if ipPrefix is valid */
    @Test(expected = IllegalArgumentException.class)
    public void testGetIpPrefixInvalid() {
        SubnetHandler.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = new NeutronSubnet();
        neutronSubnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        neutronSubnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        neutronSubnet.setCidr("10.0.0.0");
        subnetHandler.getIpPrefix(neutronSubnet);
    }

    /* Test method to check if neutron subnets are null for update */
    @Test
    public void testcanUpdateSubnetNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, subnetHandler.canUpdateSubnet(null, null));
    }

    /* Test method to check if updated subnet gateway ip is empty */
    @Test
    public void testcanUpdateSubnetGatewayEmpty() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        NeutronSubnet deltaSubnet = new NeutronSubnet();
        deltaSubnet.setGatewayIP("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, subnetHandler.canUpdateSubnet(deltaSubnet, neutronSubnet));
    }

    /* Test method to check if gateway ip is valide for update */
    @Test
    public void testcanUpdateSubnetInvalidGateway() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        NeutronSubnet deltaSubnet = defaultDeltaSubnet();
        deltaSubnet.setGatewayIP("20.0.0.200");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, subnetHandler.canUpdateSubnet(deltaSubnet, neutronSubnet));
    }

    /* Test method to check if subnets do not exist */
    @Test
    public void testcanUpdateSubnetNotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        NeutronSubnet deltaSubnet = defaultDeltaSubnet();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, subnetHandler.canUpdateSubnet(deltaSubnet, neutronSubnet));
    }

    /*
     * Test method to check if subnet already exists and update subnet
     * successfully
     */
    @Test
    public void testcanUpdateSubnetOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        NeutronSubnet deltaSubnet = defaultDeltaSubnet();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference<VnSubnetsType> ref = new ObjectReference<>();
        List<ObjectReference<VnSubnetsType>> ipamRefs = new ArrayList<ObjectReference<VnSubnetsType>>();
        List<VnSubnetsType.IpamSubnetType> subnets = new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
            subnetType.setSubnet(type);
            subnetType.setSubnetUuid("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
            subnetType.getSubnet().setIpPrefix("10.0.0.0");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, subnetHandler.canUpdateSubnet(deltaSubnet, neutronSubnet));
    }

    /* Test method to check if subnet already exists and update subnet fails */
    @Test
    public void testcanUpdateSubnetFail() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        NeutronSubnet deltaSubnet = defaultDeltaSubnet();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference<VnSubnetsType> ref = new ObjectReference<>();
        List<ObjectReference<VnSubnetsType>> ipamRefs = new ArrayList<ObjectReference<VnSubnetsType>>();
        List<VnSubnetsType.IpamSubnetType> subnets = new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
            subnetType.setSubnet(type);
            subnetType.setSubnetUuid("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
            subnetType.getSubnet().setIpPrefix("10.0.0.0");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, subnetHandler.canUpdateSubnet(deltaSubnet, neutronSubnet));
    }

    /* Tets method to check if a subnet deletion terminate with Internal_Error */
    @Test
    public void testCanDeleteSubnet() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, subnetHandler.canDeleteSubnet(neutronSubnet));

    }

    /*
     * Tets method to check if a subnet is deleted from the network when
     * vnSubnetsType.getIpamSubnets() is not null
     */
    @Test
    public void testCanDeleteSubnetTrue() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        List<ObjectReference<VnSubnetsType>> ipamRefs = mockedVirtualNetwork.getNetworkIpam();
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference<VnSubnetsType> ref = new ObjectReference<>();
        List<VnSubnetsType.IpamSubnetType> subnets = new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
            subnetType.setSubnet(type);
            subnetType.getSubnet().setIpPrefix("10.0.0.0");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        VnSubnetsType.IpamSubnetType subnetType1 = new VnSubnetsType.IpamSubnetType();
        SubnetType type1 = new SubnetType();
        for (int i = 0; i < 1; i++) {
            subnetType1.setSubnet(type1);
            subnetType1.getSubnet().setIpPrefix("10.0.1.0");
            subnetType1.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType1);
            vnSubnetType.addIpamSubnets(subnetType1);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, subnetHandler.canDeleteSubnet(neutronSubnet));
    }

    /*
     * Test method to check if a subnet is deleted from the network when
     * vnSubnetsType.getIpamSubnets() is null
     */
    @Test
    public void testCanDeleteSubnetNullGetIpamSubnets() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        List<ObjectReference<VnSubnetsType>> ipamRefs = mockedVirtualNetwork.getNetworkIpam();
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference<VnSubnetsType> ref = new ObjectReference<>();
        List<VnSubnetsType.IpamSubnetType> subnets = new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
            subnetType.setSubnet(type);
            subnetType.getSubnet().setIpPrefix("10.0.0.0");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, subnetHandler.canDeleteSubnet(neutronSubnet));
    }
}