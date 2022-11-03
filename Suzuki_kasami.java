import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Suzuki_kasami {

    public static void exit_criticalSection(site localSite, int siteNumber, int number_of_nodes, String[] ipAddresss, int[] port,
                              int no_of_sites) {

        localSite.LN[siteNumber - 1] = localSite.RN[siteNumber - 1];

        // Send updated LN array value to all sites
        String message = "ln," + siteNumber + "," + localSite.LN[siteNumber - 1];

        for (int i = 0; i < no_of_sites; i++) {

            if(i==siteNumber-1) {
                continue;
            }

            try {
                Socket socket = new Socket(ipAddresss[i], port[i]);

                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(message);
                bw.flush();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < number_of_nodes; i++) {
            if (localSite.RN[i] == localSite.LN[i] + 1) {
                if (!localSite.token_queue.contains(i + 1)) {
                    localSite.token_queue.add(i + 1);
                }
            }
        }

        if (localSite.token_queue.size() > 0) {
            localSite.sendToken(localSite.token_queue.poll());
        }

    }

    public static void main(String[] args) {

        // Read nodes.config file for nodes

        BufferedReader reader = null;
        File fFile = new File("");
        String cwd = fFile.getAbsolutePath();

        File nodes = new File(cwd + "\\nodes.config");
        try {
            reader = new BufferedReader(new FileReader(nodes));
            int number_of_nodes = 0;
            String nodeAddress = reader.readLine();
            ArrayList<String> node_table = new ArrayList<String>();

            while (nodeAddress != null) {
                node_table.add(nodeAddress);
                number_of_nodes++;
                nodeAddress = reader.readLine();

            }

            int[] siteNumber = new int[number_of_nodes];
            String[] ipAddresss = new String[number_of_nodes];
            int[] port = new int[number_of_nodes];

            String[] temporaryAddress = null;

            for (int counter = 0; counter < number_of_nodes; counter++) {

                temporaryAddress = node_table.get(counter).split(" ");
                siteNumber[counter] = Integer.parseInt(temporaryAddress[0]);
                ipAddresss[counter] = temporaryAddress[1];
                port[counter] = Integer.parseInt(temporaryAddress[2]);

            }



            // Preparing the site to enter Critical Section

            Scanner scan = new Scanner(System.in);

            int site_num = 0;

            int inFlag = 0;

            do {

                System.out.print("Enter site number for execution (1-" + number_of_nodes + "): ");
                site_num = Integer.parseInt(scan.nextLine());

                if (site_num >= 1 && site_num <= number_of_nodes) {
                    inFlag = 1;
                } else {
                    System.out.println("Please enter the correct site number i.e. from 1 to  " + number_of_nodes);
                }

            } while (inFlag == 0);

            int hasToken = 0;

            if (site_num == 1) {
                hasToken = 1;
            }

            site localSite = new site(number_of_nodes, site_num, hasToken, ipAddresss, port);

            listenToBroadcast listenBroadcast = new listenToBroadcast(localSite, port[site_num - 1]);

            listenBroadcast.start();

            String input_query = "";

            while (!input_query.equalsIgnoreCase("quit")) {
                System.out.println("Press ENTER to enter Critical Section: ");
                Scanner scan_query = new Scanner(System.in);
                input_query = scan_query.nextLine();
                if (localSite.token == 1) {

                    localSite.processingCS = 1;
                    System.out.println("Site has token. Executing Critical Section.");

                    Thread.sleep(15000);

                    localSite.processingCS = 0;

                    System.out.println("Exiting Critical Section.");

                    exit_criticalSection(localSite, site_num, number_of_nodes, ipAddresss, port, number_of_nodes);

                } else {

                    System.out.println("Requesting token");

                    localSite.reqCS();
                    System.out.println("Waiting for token....");

                    localSite.processingCS = 1;

                    while (localSite.token == 0) {
                        Thread.sleep(100);

                    }


                    System.out.println("Site has received token. Executing Critical Section.");

                    Thread.sleep(15000);

                    localSite.processingCS = 0;
                    System.out.println("Exiting Critical Section.");

                    exit_criticalSection(localSite, site_num, number_of_nodes, ipAddresss, port, number_of_nodes);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class site {

    String[] ipAddresss = null;
    int[] port = null;

    int number_of_sites = 0;
    int site_number = 0;
    int token = 0;
    int seq_number = 0;
    int processingCS = 0;
    Queue<Integer> token_queue = new LinkedList<>();
    int RN[];
    int LN[];

    site(int numberofsites, int siteNumber, int hasToken, String[] ipAddr, int[] portno) {
        this.number_of_sites = numberofsites;
        this.site_number = siteNumber;
        this.token = hasToken;

        this.ipAddresss = ipAddr;
        this.port = portno;

        RN = new int[number_of_sites];
        LN = new int[number_of_sites];
        for (int i = 0; i < numberofsites; i++) {
            RN[i] = 0;
            LN[i] = 0;
        }

    }

    void print() {
        System.out.println(number_of_sites + " " + site_number + " " + token);
    }

    void updateLN(int siteNumber, int value) {
        LN[siteNumber-1]=value;
    }

    void reqCS() {
        RN[site_number - 1]++;

        String message = "request," + site_number + "," + RN[site_number - 1];

        System.out.println("Broadcasting request to " + (number_of_sites - 1) + " sites.");

        for (int i = 0; i < number_of_sites; i++) {

            if (i != site_number - 1) {
                Socket socket = null;

                try {
                    socket = new Socket(ipAddresss[i], port[i]);
                    System.out.println(socket.getPort());
                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(message);
                    bw.flush();
                    os.close();
                    osw.close();
                    bw.close();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }

            }

        }

    }

    void processCSectionRequest(int site, int sn) {
        if (RN[site - 1] < sn) {
            RN[site - 1] = sn;
        }

        if (processingCS == 0 && token == 1) {
            sendToken(site);

        } else {
            token_queue.add(site);
        }

    }

    void sendToken(int site) {

        if (this.token == 1) {
            if (RN[site - 1] == LN[site - 1] + 1) {
                System.out.println("Sending token to site " + site);

                try {
                    Socket socket = new Socket(ipAddresss[site - 1], port[site - 1]);
                    String message = "token";
                    int tokenQueuelen=token_queue.size();
                    for(int i=0;i<tokenQueuelen;i++) {
                        message+=","+token_queue.poll();
                    }

                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(message);
                    bw.flush();
                    socket.close();
                    this.token = 0;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}

class listenToBroadcast extends Thread {

    int port = 0;
    site localSite = null;

    public listenToBroadcast(site thisSite, int port) {
        this.port = port;
        this.localSite = thisSite;
    }

    public void run() {

        try {
            ServerSocket serverSckt = new ServerSocket(port);
            while (true) {
                Socket socket = serverSckt.accept();
                new processRq(socket, localSite).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class processRq extends Thread {

    Socket lSocket = null;
    site localSite = null;

    public processRq(Socket lSocket, site localSitePass) {
        this.lSocket = lSocket;
        this.localSite = localSitePass;
    }

    public void run() {

        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(lSocket.getInputStream()));

            String command = "";
            String[] message = null;

            command = in.readLine();
            System.out.println(command);
            if (null != command) {
                if (command.charAt(0) == 'r') {

                    message = command.split(",");
                    localSite.processCSectionRequest(Integer.parseInt(message[1]), Integer.parseInt(message[2]));

                }

                if (command.charAt(0) == 't') {

                    message = command.split(",");
                    localSite.token_queue.clear();
                    int length=message.length;
                    for(int i=1;i<length;i++) {
                        localSite.token_queue.add(Integer.parseInt(message[i]));
                    }
                    localSite.token = 1;

                }

                if (command.charAt(0) == 'l') {

                    message = command.split(",");
                    localSite.updateLN(Integer.parseInt(message[1]), Integer.parseInt(message[2]));

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                in.close();
                // out.close();
                lSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}