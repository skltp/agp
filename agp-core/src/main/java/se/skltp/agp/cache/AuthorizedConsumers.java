/**
 * 
 */
package se.skltp.agp.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author muqkha
 *
 */
public class AuthorizedConsumers extends ArrayList<String> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public AuthorizedConsumers() {
		super();
	}
	
	public AuthorizedConsumers(Object data) {
		if (data != null) {
			addAllConsumers(data);
		}
	}
	
	public void addIfAbsent(String item) {
		if (item != null && !this.contains(item)) {
			this.add(item);
		}
	}

	private void addAllConsumers(Object data) {
		String[] consumers = data.toString().split("\\s*,\\s*");
		
		for (String consumer : consumers) {
			this.add(consumer);
		}
	}

	public void update(Object data) {
		String[] consumers = data.toString().split("\\s*,\\s*");
		
		for (String consumer : consumers) {
			if (this.contains(consumer)) {
				this.add(consumer);
			} else {
				this.remove(consumer);
			}
		}
	}
	
	
	public boolean contains(String senderId, String originalConsumerId) {
		return this.contains(senderId) || this.contains(originalConsumerId);
				
    }

}
