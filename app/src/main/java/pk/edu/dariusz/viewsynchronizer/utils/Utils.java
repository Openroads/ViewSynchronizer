package pk.edu.dariusz.viewsynchronizer.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Created by dariusz on 11/18/17.
 */

public class Utils {
    public static InetAddress getMyInetAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return addr;
                        } else {
                            if (!isIPv4) {
                                return addr;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return null;
    }
    public static String getMyIPAddress(boolean useIPv4) {
        InetAddress address = getMyInetAddress(useIPv4);
        if(address !=null) {
            String stringAddress = address.getHostAddress();
            if (useIPv4) {
                return stringAddress;
            } else {
                int delim = stringAddress.indexOf('%'); // drop ip6 zone suffix
                return delim < 0 ? stringAddress.toUpperCase() : stringAddress.substring(0, delim).toUpperCase();

            }
        }else
            return "";
    }
    public static String checkHostsInLANForServerIp() throws SocketException {

        //getting subnet address
         /*InetAddress address = getMyInetAddress(true);
        NetworkInterface byInetAddress = NetworkInterface.getByInetAddress(address);
        for(InterfaceAddress interfaceAddress : byInetAddress.getInterfaceAddresses()){
            interfaceAddress.getNetworkPrefixLength();
        }*/
        String[] split = getMyIPAddress(true).split("\\.");
        StringBuilder subnet;
        int timeout=100;
        for(int j=1;j<255;j++) {
            subnet= new StringBuilder(split[0]+"."+split[1]);
            subnet.append(".").append(j);
            for (int i = 1; i < 255; i++) {
                String host = subnet.toString()+"."+i;
                    if(isListeningOnPort(host,ViewSynchronizerConstants.APPLICATION_PORT))
                        return host;
                }
            }
            return "";
        }
    public static boolean isListeningOnPort(String host, int port)
    {
        Socket s = null;
        try
        {
            s = new Socket();
            s.connect(new InetSocketAddress(host, port),180);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if(s != null)
                try {s.close();}
                catch(Exception e){}
        }
    }

    public static boolean checkIfClientIsConnected(Socket socket) {
        try {
            socket.getOutputStream().write(0);
            return true;
        } catch (IOException e) {
            return false;
        }
        /* if(!socket.getInetAddress().isReachable(200)) {
                return false;
            }*/
    }
}
