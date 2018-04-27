package net.data.technology.jraft;


public abstract class Tuple {

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.hashCode();
        return result;
    }

    abstract protected boolean elementEquals(Tuple tuple);

    @Override
    public boolean equals(Object object) {
        boolean rv = false;
        if (!(object instanceof Tuple))
            return false;
        if (object == this)
            return true;

        Tuple tuple = (Tuple) object;
        rv = elementEquals(tuple);

        return rv;
    }

    protected <T> boolean same(T a, T b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else if (a.equals(b)) {
            return true;
        }
        return false;
    }

}
