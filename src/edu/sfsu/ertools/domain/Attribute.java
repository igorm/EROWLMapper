package edu.sfsu.ertools.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 *
 * Models an ER entity/relationship/component attribute
 *
 */
public class Attribute {
	
	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Key
	 */
	private boolean key;
	
	/**
	 * Description
	 */
	private String description;
	
	/**
	 * Component attribute objects
	 */
	private List<Attribute> componentAttributes = new ArrayList<Attribute>();
	
	/**
	 * @return
	 */
	public boolean isComposite() {
		if (componentAttributes.isEmpty())
			return false;
		else 
			return true;
	}
	
	/**
	 * @param attribute
	 */
	public void addComponentAttribute(Attribute attribute) {
		componentAttributes.add(attribute);
	}
	
	/**
	 * @return
	 */
	public boolean isKey() {
		return key;
	}
	
	/**
	 * @param key
	 */
	public void setKey(boolean key) {
		this.key = key;
	}
	
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
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return
	 */
	public List<Attribute> getComponentAttributes() {
		return componentAttributes;
	}
}
