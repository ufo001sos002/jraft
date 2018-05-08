package net.data.technology.jraft.extensions;

public class Tuple4<E1, E2, E3, E4> extends Tuple {
    private E1 e1;
    private E2 e2;
    private E3 e3;
    private E4 e4;

    public Tuple4(E1 e1, E2 e2, E3 e3, E4 e4) {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
        this.e4 = e4;
    }

    public E1 _1() {
        return e1;
    }

    public E2 _2() {
        return e2;
    }

    public E3 _3() {
        return e3;
    }

    public E4 _4() {
        return e4;
    }

    @Override
    protected boolean elementEquals(Tuple tuple) {
        if (!(tuple instanceof Tuple4))
            return false;
        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) tuple;
        if (same(this.e1, tuple4.e1) && same(this.e2, tuple4.e2) && same(this.e3, tuple4.e3)
                && same(this.e4, tuple4.e4)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + (e1 == null ? 0 : e1.hashCode());
        result = result * 31 + (e2 == null ? 0 : e2.hashCode());
        result = result * 31 + (e3 == null ? 0 : e3.hashCode());
        result = result * 31 + (e4 == null ? 0 : e4.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{").append(e1 == null ? "[NULL]" : e1).append(":")
                .append(e2 == null ? "[NULL]" : e2).append(":").append(e3 == null ? "[NULL]" : e3)
                .append(":").append(e4 == null ? "[NULL]" : e4).append("}").toString();
    }

    /**
     * @return {@link #e1} 的值
     */
    public E1 getE1() {
        return e1;
    }

    /**
     * @param e1 根据 e1 设置 {@link #e1}的值
     */
    public void setE1(E1 e1) {
        this.e1 = e1;
    }

    /**
     * @return {@link #e2} 的值
     */
    public E2 getE2() {
        return e2;
    }

    /**
     * @param e2 根据 e2 设置 {@link #e2}的值
     */
    public void setE2(E2 e2) {
        this.e2 = e2;
    }

    /**
     * @return {@link #e3} 的值
     */
    public E3 getE3() {
        return e3;
    }

    /**
     * @param e3 根据 e3 设置 {@link #e3}的值
     */
    public void setE3(E3 e3) {
        this.e3 = e3;
    }

    /**
     * @return {@link #e4} 的值
     */
    public E4 getE4() {
        return e4;
    }

    /**
     * @param e4 根据 e4 设置 {@link #e4}的值
     */
    public void setE4(E4 e4) {
        this.e4 = e4;
    }

}
