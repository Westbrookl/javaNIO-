//package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NIOServer {
	private int port = 8888;
	private Charset charset = Charset.forName("UTF-8");
	private Map<String,SocketChannel> clientMap = new HashMap();
	private ByteBuffer sBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
	private static Selector selector ;
	private static int count = 0;
	public NIOServer(int port){
		this.port = port;
		try{
			init();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void init() throws IOException{
		
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务器启动端口为： "+ port);
	}
	public void listen() throws IOException{
		while(true){
			selector.select();
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			for(SelectionKey sk:selectionKeys){
				handle(sk);
			}
			selectionKeys.clear();
		}
	}
	public void handle(SelectionKey sk) {
		try{
			if(sk.isAcceptable()){
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) sk.channel();
//				ServerSocket serverSocket = serverSocketChannel.socket();
				SocketChannel client = serverSocketChannel.accept();
				client.configureBlocking(false);
				client.register(selector,SelectionKey.OP_READ);
				System.out.println(getClinetName(client)+" 连接成功");
				clientMap.put(getClinetName(client), client);
		
			}else if(sk.isReadable()){
				SocketChannel client = (SocketChannel)sk.channel();
				rBuffer.clear();
				int bytes = client.read(rBuffer);
				if(bytes>0){
					rBuffer.flip();
					String text = String.valueOf(charset.decode(rBuffer));
					System.out.println(getClinetName(client)+": "+text);
					dispatch(client,text);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public String getClinetName(SocketChannel sk){
		count++;
		Socket socket = sk.socket();
		return "["+socket.getInetAddress().toString().substring(1)+": "+count+" ]";
	}
	public void dispatch(SocketChannel sk,String text) throws IOException{
//		Map.Entry<String, SocketChannel> map = (Entry<String, SocketChannel>) clientMap.entrySet();
		if(!clientMap.isEmpty()){
			for(Map.Entry<String, SocketChannel> map :clientMap.entrySet()){		
				SocketChannel temp = map.getValue();
				if(!sk.equals(temp)){
//					System.out.println("-----1");
					sBuffer.clear();
					sBuffer.put(charset.encode(getClinetName(sk)+":"+text));
					sBuffer.flip();
					temp.write(sBuffer);
				}
			}
		}
		
	}
	public static void main(String[] args) throws IOException{
		NIOServer server = new NIOServer(7777);
		server.listen();
	}
}
