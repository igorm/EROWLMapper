package edu.sfsu.ertools.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igorm
 * 
 * Models an ER relationship
 *
 */
public class Relationship {
	
	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Description
	 */
	private String description;
	
	/**
	 * Participating entity objects
	 */
	private List<ParticipatingEntity> participatingEntities = new ArrayList<ParticipatingEntity>();
	
	/**
	 * Attribute objects
	 */
	private List<Attribute> attributes = new ArrayList<Attribute>();
	
	/**
	 * @param name
	 * @return
	 */
	public ParticipatingEntity getParticipatingEntity(String name) {
		for (ParticipatingEntity participatingEntity : participatingEntities) {
			if (participatingEntity.getName().equals(name))
				return participatingEntity;
		}
		return null;
	}
	
	/**
	 * @param attribute
	 */
	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
	}
	
	/**
	 * @param participatingEntity
	 */
	public void addParticipatingEntity(ParticipatingEntity participatingEntity) {
		participatingEntities.add(participatingEntity);
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
	public List<ParticipatingEntity> getParticipatingEntities() {
		return participatingEntities;
	}
	
	/**
	 * @return
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}
}
