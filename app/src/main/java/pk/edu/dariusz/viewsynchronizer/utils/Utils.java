package pk.edu.dariusz.viewsynchronizer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;

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

    public static void copyBetweenStreams(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        LogUtil.logDebugToConsole("Read: "+buf.length + "bytes");
    }

    public static DATA_TYPE getDataType(String fileName){
        DATA_TYPE data_type;

        if(fileName.toLowerCase().endsWith("jpg") || fileName.toLowerCase().endsWith("png")){
            data_type= DATA_TYPE.IMG;
        }
        else if(fileName.endsWith("pdf")) {
            data_type = DATA_TYPE.PDF;
        } else {
            data_type = DATA_TYPE.OTHER;
        }

        return  data_type;
    }

    public static void removeDirectoryOrFile(File fileOrDirectory){
        if (fileOrDirectory.isDirectory())
        for (File child : fileOrDirectory.listFiles())
            removeDirectoryOrFile(child);

        fileOrDirectory.delete();
    }

    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

}
