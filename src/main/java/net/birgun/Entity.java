package net.birgun;

import java.time.LocalDateTime;

public class Entity implements Comparable<Entity> {
    private LocalDateTime date;
    private int id;
    private String name;
    private String surName;
    private ActionType action;

    public Entity() {
	super();
    }

    public LocalDateTime getDate() {
	return date;
    }

    public void setDate(LocalDateTime date) {
	this.date = date;
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getSurName() {
	return surName;
    }

    public void setSurName(String surName) {
	this.surName = surName;
    }

    public ActionType getAction() {
	return action;
    }

    public void setAction(ActionType action) {
	this.action = action;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((action == null) ? 0 : action.hashCode());
	result = prime * result + ((date == null) ? 0 : date.hashCode());
	result = prime * result + id;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((surName == null) ? 0 : surName.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Entity other = (Entity) obj;
	if (action != other.action)
	    return false;
	if (date == null) {
	    if (other.date != null)
		return false;
	} else if (!date.equals(other.date))
	    return false;
	if (id != other.id)
	    return false;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (surName == null) {
	    if (other.surName != null)
		return false;
	} else if (!surName.equals(other.surName))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Entity [date=" + date + ", id=" + id + ", name=" + name + ", surName=" + surName + ", action=" + action
		+ "]";
    }

    @Override
    public int compareTo(Entity o) {
	int compareTo = this.date.compareTo(o.date);
	return compareTo;
    }

}
