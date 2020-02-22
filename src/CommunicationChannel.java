import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {
	private BlockingQueue<Message> spaceExplorerChannel;
	private BlockingQueue<Message> headQuarterChannel;
	
	private Map<Thread, Queue<Message>> headQuartersMessages;

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
		this.spaceExplorerChannel = new LinkedBlockingQueue<Message>();
		this.headQuarterChannel = new LinkedBlockingQueue<Message>();
		this.headQuartersMessages = new HashMap<Thread, Queue<Message>>();
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 * @throws InterruptedException 
	 */
	public void putMessageSpaceExplorerChannel(Message message) {
		
		/**
		 * Synchronized queue - 2 Space Explorers can't put
		 * elements in this queue simultaneously.
		 */
		try {
			spaceExplorerChannel.put(message);
		} catch (InterruptedException e) {
			
		}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 * @throws InterruptedException 
	 */
	public Message getMessageSpaceExplorerChannel() {
		
		/**
		 * Synchronized queue - 2 Space Explorers can't get
		 * an element from this queue simultaneously.
		 */
		try {
			return spaceExplorerChannel.take();
		} catch (InterruptedException e) {
			
		}
		
		return null;
	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 * @throws InterruptedException 
	 */
	public void putMessageHeadQuarterChannel(Message message) {
		
		/* Get the HQ who is putting a message in the channel. */
		Thread headQuarter = Thread.currentThread();
		
		/* Add the HQ if the channel doesn't know it yet. */
		if (!headQuartersMessages.keySet().contains(headQuarter)) {
			Queue<Message> headQuarterMessages = new LinkedList<Message>();
			headQuartersMessages.put(headQuarter, headQuarterMessages);
		}
		
		/* Get current HQ's message queue. */
		Queue<Message> currentHQMessages = headQuartersMessages.get(headQuarter);
		
		/** 
		 * If the HQ sent an END, he is done sending messages.
		 * HQ's queue is cleared and all the messages are put in
		 * the main list from where space explorers can get messages.
		 */
		if (message.getData().equals(HeadQuarter.END)) {
			
			/**
			 * 2 HQs must not be able to put messages in the main
			 * queue simultaneously.
			 */
			synchronized (this) {				
				while (!currentHQMessages.isEmpty()) {
					try {
						headQuarterChannel.put(currentHQMessages.poll());
					} catch (InterruptedException e) {
						
					}
				}
			}
		} else {
			/**
			 * END message not received - the HQ is not done sending messages.
			 * Put the message in HQ's waiting list.
			 */
			currentHQMessages.add(message);
		}
		
		headQuartersMessages.put(headQuarter, currentHQMessages);
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 * @throws InterruptedException 
	 */
	public Message getMessageHeadQuarterChannel() {
		try {
			return headQuarterChannel.take();
		} catch (InterruptedException e) {
			
		}
		
		return null;
	}
}
