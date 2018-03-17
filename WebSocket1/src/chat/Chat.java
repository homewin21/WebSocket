package chat;

import java.io.IOException;
import java.util.*;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class Chat {
	private boolean first = true;
	private String name;
	private Session session;
	private static final HashMap<String, Object> clientSet = new HashMap<String, Object>();

	/*
	 * �ͻ�������ʱ�����÷���2
	 */
	@OnOpen
	public void start(Session session) throws IOException {

		this.session = session;

	}

	/*
	 * �ͻ��˶Ͽ�ʱ�����÷���
	 */
	@OnClose
	public void end() {
		clientSet.remove(name, this);
		String message = String.format("��%s %s��", name, "�뿪�������ң�");
		// ������Ϣ
		broadcast(message);
	}

	/*
	 * �ͻ����յ���Ϣʱ�����÷���
	 */
	@OnMessage
	public void print(String msg) {
		if (first) {
			name = msg;
			clientSet.put(name, this);
			String message = String.format("��%s %s��", name, "�����������ң�");
			// ������Ϣ
			broadcast(message);
			first = false;
		} else {
			broadcast("��" + name + "��" + ":" + msg);
		}

	}

	// ���ͻ���ͨ�ų��ִ���ʱ�������÷���
	@OnError
	public void onError(Throwable t) throws Throwable {
		System.out.println("WebSocket����˴��� " + t);
	}

	public void broadcast(String msg) {
		// �������������������пͻ���
		Chat client = null;
		for (String nickname : clientSet.keySet()) {

			try {
				client = (Chat) clientSet.get(nickname);
				synchronized (client) {
					// ������Ϣ
					client.session.getBasicRemote().sendText(msg);
				}
			} catch (IOException e) {
				System.out.println("���������ͻ��� " + client + " ������Ϣ���ִ���");
				clientSet.remove(name, client);
				try {
					client.session.close();
				} catch (IOException e1) {
				}
				String message = String.format("��%s %s��", client.name, "�Ѿ����Ͽ������ӡ�");
				broadcast(message);
			}
		}
	}

}
