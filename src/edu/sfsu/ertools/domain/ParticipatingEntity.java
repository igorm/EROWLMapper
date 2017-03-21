package edu.sfsu.ertools.domain;

/**
 * @author igorm
 * 
 * Models an ER entity participating in a relationship
 *
 */
public class ParticipatingEntity {
	
	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Role name
	 */
	private String role;
	
	/**
	 * Min cardinality constraint
	 */
	private int min;
	
	/**
	 * Max cardinality constraint
	 */
	private int max;
	
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return
	 */
	public String getRole() {
		return role;
	}
	
	/**
	 * @param role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * @return
	 */
	public int getMin() {
		return min;
	}

	/**
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * @return
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max
	 */
	public void setMax(int max) {
		this.max = max;
	}
}
