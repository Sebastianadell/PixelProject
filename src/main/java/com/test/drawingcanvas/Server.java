package com.test.drawingcanvas;

import javafx.beans.binding.ObjectExpression;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private Socket s = null;
    private ServerSocket ss = null;
    private ObjectInputStream in = null;
    //usage Server s = new Server(8080);
    private Color serverCanvasData[][];
    private final Object mutex = new Object(); // mutex for canvas data
    private List<ObjectOutputStream> clientOutputs = new ArrayList<ObjectOutputStream>();

    public Server(int port) throws IOException{
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
           // ss.bind(new java.net.InetSocketAddress(50000));
            System.out.println("Server started");
            System.out.println("Waiting for client..");
       }
        catch (IOException e) {
            System.out.println("ERROR STARTING SERVER");
            throw new RuntimeException(e);
        }
    }

    public void start(){
        new Thread(() -> {
            try{
                this.s = ss.accept();
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                synchronized (clientOutputs){
                    clientOutputs.add(out);
                }
                this.in = new ObjectInputStream(s.getInputStream());
                while(true){
                    Operation op = (Operation) in.readObject();
                    System.out.println("Server received operation at Row: " + op.row + "Col: " + op.col); // debug line
                    updateServerOperation(op);
                    broadcastToClients(op, out);
                }
            } catch(Exception e){
                System.out.println("ERROR STARTING SERVER");
            }
        }).start();
    }

    public void onClose() throws IOException {
        System.out.println("Closing Connection");
        this.s.close();
        this.in.close();

    }

    public void updateServerOperation(Operation op){
        synchronized (mutex) { // critical section here, we dont want multiple threads updating the data structure at the same time
            serverCanvasData[op.row][op.col] = op.getNext();
        }
    }

    public void initServerCanvas(Color[][] clientCanvas, int rows, int cols) {
        this.serverCanvasData = new Color[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                serverCanvasData[r][c] = clientCanvas[r][c]; // i think this would actually be hostCanvas since only the host can call this method
            }
        }
    }

    public void broadcastToClients(Operation op, ObjectOutputStream sender){
        synchronized (clientOutputs){
            for(ObjectOutputStream out : clientOutputs) {
                if (out != sender) {
                    try {
                        out.writeObject(op);
                        out.flush();
                    } catch (IOException e) {
                        System.out.println("ERROR BROADCASTING TO CLIENTS");
                    }
                }
            }
        }
    }

}