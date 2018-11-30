//package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;

public class NIOClient {
	private static Selector selector;
	private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer sBuffer = ByteBuffer.allocate(1024);
	private Charset charset = Charset.forName("UTF-8");
	private InetSocketAddress SERVER ;
	public NIOClient(int port){
		SERVER = new InetSocketAddress("localhost",port);
		try{
			init();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void init()throws IOException{
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(SERVER);
		while(true){
			selector.select();
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			for(SelectionKey sk:selectionKeys){
				handle(sk);
			}
			selectionKeys.clear();
		}
	}
	public void handle(SelectionKey sk){
		try{
			if(sk.isConnectable()){
				SocketChannel client = (SocketChannel)sk.channel();
				if(client.isConnectionPending()){
					client.finishConnect();
					selector = Selector.open();    
					client.configureBlocking(false);
				
					System.out.println("µÇÂ¼ÁÄÌìÊÒ");
					new Thread(){
						public void run(){
							while(true){
								try{
									sBuffer.clear();
									Scanner in = new Scanner(System.in);
									String text = in.nextLine();
									System.out.println(text);
									sBuffer.put(charset.encode(text));
									sBuffer.flip();
									client.write(sBuffer);
								}catch(IOException e){
									e.printStackTrace();
								}
							}
						}
					}.start();	
				}
				client.register(selector, SelectionKey.OP_READ);
			}else if(sk.isReadable()){
				System.out.println("---23");
				SocketChannel client = (SocketChannel)sk.channel();
				rBuffer.clear();
				int count = client.read(rBuffer);
				if(count>0){
					rBuffer.flip();
					String text = String.valueOf(charset.decode(rBuffer));
					System.out.println(text);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		new NIOClient(7777);
	}
}
