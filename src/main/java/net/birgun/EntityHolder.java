package net.birgun;

public class EntityHolder {
    private Entity first;
    private Entity last;

    public EntityHolder(Entity first, Entity last) {
	super();
	this.first = first;
	this.last = last;
    }

    public Entity getFirst() {
	return first;
    }

    public void setFirst(Entity first) {
	this.first = first;
    }

    public Entity getLast() {
	return last;
    }

    public void setLast(Entity last) {
	this.last = last;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((first == null) ? 0 : first.hashCode());
	result = prime * result + ((last == null) ? 0 : last.hashCode());
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
	EntityHolder other = (EntityHolder) obj;
	if (first == null) {
	    if (other.first != null)
		return false;
	} else if (!first.equals(other.first))
	    return false;
	if (last == null) {
	    if (other.last != null)
		return false;
	} else if (!last.equals(other.last))
	    return false;
	return true;
    }

}
