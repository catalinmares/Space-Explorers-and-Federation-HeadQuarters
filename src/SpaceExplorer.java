import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.nio.charset.StandardCharsets;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {
	private Integer hashCount;
	public static Set<Integer> discovered;
	private CommunicationChannel channel;
	
	public static String SYNC = "SYNC"; // for Threads synchronization
	
	/**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */
	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount = hashCount;
		SpaceExplorer.discovered = discovered;
		this.channel = channel;
	}

	@Override
	public void run() {
		Message fromHQMessage1;
		Message fromHQMessage2;
		Message toHQMessage;
		
		Integer parentSolarSystem;
		Integer currentSolarSystem;
		
		String frequency;
		String decodedFrequency;
		
		boolean explore = false;
		
		/**
		 * Each Space Explorer runs until it receives 
		 * an EXIT message from an HQ. 
		 */
		while (true) {
			synchronized (SYNC) {
				
				/* Receive 1st message from an HQ. */
				fromHQMessage1 = null;
				
				while (fromHQMessage1 == null) {
					fromHQMessage1 = channel.getMessageHeadQuarterChannel();
				}
				
				/* If EXIT, thread must end its execution. */
				if (fromHQMessage1.getData().equals(HeadQuarter.EXIT)) {
					break;
				}
				
				/* Receive 2nd message from an HQ. */
				fromHQMessage2 = null;
				
				while (fromHQMessage2 == null) {
					fromHQMessage2 = channel.getMessageHeadQuarterChannel();
				}
			}
			
			/* Extract data from the 2 messages. */
			parentSolarSystem = fromHQMessage1.getCurrentSolarSystem();
			currentSolarSystem = fromHQMessage2.getCurrentSolarSystem();
			frequency = fromHQMessage2.getData();
			
			/* Check if current solar system is already discovered. */
			synchronized (discovered) {
				if (!discovered.contains(currentSolarSystem)) {
					discovered.add(currentSolarSystem);
					explore = true;
				}
			}
			
			/** 
			 * If the current system is undiscovered,
			 * the Space Explorer must explore it. 
			 */
			if (explore) {
				
				/* Decode current system's frequency. */
				decodedFrequency = encryptMultipleTimes(frequency, hashCount);
				
				/* Send decoded frequency to an HQ. */
				toHQMessage = new Message(parentSolarSystem, currentSolarSystem, decodedFrequency);
				channel.putMessageSpaceExplorerChannel(toHQMessage);
				
				explore = false;
			}
		}
	}
	
	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
