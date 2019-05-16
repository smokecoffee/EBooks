package com.atson.commons.lang;

public class Pair<A, B> extends FP.T2<A, B> {
    public static <A, B> Pair<A, B> of(final A a, final B b) {
        return new Pair<A, B>(a, b, false);
    }
    
    /**
     * @param t2 2-タプル
     * @return タプルのfstとsndをコピーしたPair
     */
    public static <A, B> Pair<A, B> of(final FP.T2<? extends A, ? extends B> t2) {
        return Pair.<A, B> of(t2.fst(), t2.snd());
    }

    public static Pair<String, String> str(final String a, final String b) {
        return of(a, b);
    }

    private final A a;
    private final B b;

    @Deprecated
    public Pair(final A a, final B b) {
        // 替わりに　Pair.of(a, b) して
        this(a, b, false);
    }

    protected Pair(final A a, final B b,
            @SuppressWarnings("unused") final boolean dummy) {
        this.a = a;
        this.b = b;
    }

    @Override
    public A fst() {
        return this.a;
    }

    // .jsp用
    public A getFst() {
        return fst();
    }

    @Override
    public B snd() {
        return this.b;
    }

    // .jsp用
    public B getSnd() {
        return snd();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((this.a == null) ? 0 : this.a.hashCode());
        result = 31 * result + ((this.b == null) ? 0 : this.b.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Pair<?, ?>)) {
            return false;
        }

        Pair<?, ?> p = (Pair<?, ?>) o;
        return (this.a == p.a || this.a != null && this.a.equals(p.a))
                && (this.b == p.b || this.b != null && this.b.equals(p.b));
    }

    @Override
    public String toString() {
        return "(" + this.a + "," + this.b + ")";
    }
}
