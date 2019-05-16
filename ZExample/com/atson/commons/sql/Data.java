package com.atson.commons.sql;

import static java.lang.reflect.Array.newInstance;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;

import com.atson.commons.annotations.NonNull;
import com.atson.higherkind.typeclass.__;
import com.atson.commons.lang.FP;
import com.atson.commons.lang.FP.EFun;
import com.atson.commons.lang.FP.Fun;
import com.atson.commons.lang.FP.Eq;
import com.atson.commons.lang.FP.Unit;

final public class Data {

    /**
     * <p>
     * (Haskell)<br>
     * {@code Maybe a = Just a | Nothing }<br>
     * のJava実装<br>
     * <br>
     * 失敗するかもしれない計算の結果の型に使う
     * </p>
     *
     * 例
     * <pre>
     * Maybe{@code <Result>} mresult = ...
     * if(mresult.isJust()) {
     *   Result result = mresult.fromJust();
     *   System.out.println("success: result=" + result);
     * } else {
     *   System.out.println("fail");
     * }
     * </pre>
     * @author hibi
     * @author wy8h-hsmt
     *
     * @param <A>
     */
    static public abstract class Maybe<A> implements Iterable<A>, __<Maybe.Mu, A> {
        private Maybe() {
            // 継承を制限
        }

        // Defunctionalization type for higher kind polymorphism
        final public static class Mu {}

        /**
         * Functionaization down cast
         */
        public static <A> Maybe<A> fnize(__<Maybe.Mu, A> f) { return (Maybe<A>)f; }

        /**
         * Functionaization down cast for function result type
         */
        public static <A,B extends A,C>
            Fun<B, Maybe<C>> fnizef(final Fun<A, __<Maybe.Mu, C>> f) {
            return new Fun<B, Maybe<C>>()
                { @Override public Maybe<C> app(B b) { return fnize(f.app(b)); } };
        }

        /**
         * folding function of Maybe
         */
        public <B> B cata(final B dflt, final Fun<A,B> f) {
            return this.isNothing() ? dflt : f.app(this.fromJust());
        }

        /**
         * map function of Maybe
         */
        public <B> Maybe<B> map(final Fun<A, B> f) {
            return maybe
                (Data.<B>nothing(),
                 new Fun<A,Maybe<B>>()
                 { @Override public Maybe<B> app(A a) { return just(f.app(a)); } },
                 this
                 );
        }

        /**
         * @return thisがjustの場合、true
         */
        public final boolean isJust() { return (this instanceof Just); }
        /**
         * @return thisがnothingの場合、true
         */
        public final boolean isNothing() { return (this instanceof Nothing); }
        /**
         * @return thisがjust(a)の場合、aを返す。さもないと例外
         * @throws ClassCastException thisがisJustでない場合
         */
        public final A fromJust() { return ((Just<A>)this).a; }

        /**
         * thisがjust(a) ならば、aを返す。<br>
         * さもなくば、defaultValueを返す。
         * @param defaultValue デフォルト値
         */
        public final A getOrElse(final A defaultValue) {
            return isJust() ? fromJust() : defaultValue;
        }

        /**
         * thisがjust(a) ならば、aを返す。<br>
         * さもなくば、nullを返す。
         */
        public final A getOrNull() {
            return getOrElse(null);
        }

        /**
         * thisがjust(a)ならば、aを返す。<br>
         * さもなくば、fun.app(UNIT)を実行して結果を返す。
         * @param fun
         * @return thisがjust(a)ならば、a<br>
         * さもなくば、fun.app(UNIT)
         * @throws X fun.appが実行され、Xがスローされた場合
         */
        public final <X extends Exception> A getOrExecute(
                final EFun<? super Unit, A, X> fun) throws X {
            return isJust() ? fromJust() : fun.app(FP.UNIT);
        }

        /**
         * thisがjust(a) ならば、aだけを返すイテレータ<br>
         * thisがnothingならば、常にhasNext() がtrueを返すイテレータを返す。<br>
         * このイテレータはremoveをサポートしない。
         */
        @Override
        public Iterator<A> iterator() {
            return new Iterator<A>() {

                private boolean used = false;

                @Override
                public boolean hasNext() {
                    return isJust() && ! this.used;
                }

                @Override
                public A next() {
                    if(! isJust()) {
                        throw new UnsupportedOperationException("not just");
                    }
                    if(this.used) {
                        throw new UnsupportedOperationException("already used");
                    }
                    this.used = true;
                    return fromJust();
                }

                @Override
                public void remove() {
                    FP.unsupported();
                }
            };
        }


        /**
         * {@code Maybe<? extends T>} を{@code Maybe<T>} に変換する。
         * @param ma Maybe<? extends T> 型の何か
         * @return maがjust(a) なら just(a) を、nothingならnothing() を返す。
         */
        @NonNull
        public static <T> Maybe<T> up(@NonNull final Maybe<? extends T> ma) {
            if (ma.isJust()) { return Data.<T>just(ma.fromJust()); }
            else             { return nothing(); }
        }

        /**
         * nullをnothingに、non-nullをjustに変換
         * @param a
         * @return a != nullの場合just(a)を返す。<br>
         * a == nullの場合nothing()を返す。
         */
        @NonNull
        public static <A> Maybe<A> fromNullable(final A a) {
            return a != null ? just(a) : Data.<A> nothing();
        }

        /**
         * nullでないとわかっているaをとり、just(a)を返す
         * @param a
         * @return just(a)
         * @throws NullPointerException aがnullの場合
         */
        @NonNull
        public static <A> Maybe<A> fromNonNull(final A a) {
            return a != null ? just(a)
                    : FP.<Maybe<A>> throwRuntime(new NullPointerException(
                            "unexpected null"));
        }
    }

    /**
     * maがjustかどうか判定する。
     * @param ma
     * @return maがjustならばtrue、maがnothingであるかnullならばfalse
     */
    public static <A> boolean isJust(final Maybe<A> ma) {
        return (ma instanceof Just);
    }

    private static final class Just<A> extends Maybe<A> {
        final A a;

        Just(final A a) {
            this.a = a;
        }
    }

    /**
     * maがnothingかどうか判定する。
     * @param ma
     * @return maがnothingならばtrue、maがjustであるかnullならばfalse
     */
    public static <A> boolean isNothing(final Maybe<A> ma) {
        return (ma instanceof Nothing);
    }

    /**
     * @param a 結果の値
     * @return justで包んだa
     */
    @NonNull
    public static <A> Maybe<A> just(final A a) { return new Just<A>(a); }

    public static <A> Fun<A,Maybe<A>> just() {
        return new Fun<A,Maybe<A>>() {
            @Override
            public Maybe<A> app(final A a) { return just(a); }
        };
    }

    private static final class Nothing<A> extends Maybe<A> {
        Nothing() {}
    }

    /**
     * @return nothing
     */
    @NonNull
    public static <A> Maybe<A> nothing() { return new Nothing<A>(); }

    /**
     * Data.Maybe Functions
     * @throws NullPointerException mayがnullの場合
     */
    public static <A> A fromJust(@NonNull final Maybe<A> may) { return may.fromJust(); }

    public static <A> Fun<Maybe<A>,A> fromJust() {
        return new Fun<Maybe<A>,A>() {
            @Override
            public A app(final Maybe<A> may) { return fromJust(may); }
        };
    }

    public static <A> A fromMaybe(final A dflt, final Maybe<A> may) {
        return isJust(may) ? fromJust(may) : dflt;
    }
    public static <A> Fun<Maybe<A>,A> fromMaybe(final A dflt) {
        return new Fun<Maybe<A>,A>() {
            @Override
            public A app(final Maybe<A> may) { return fromMaybe(dflt, may); }
        };
    }
    public static <A> Fun<A,Fun<Maybe<A>,A>> fromMaybe() {
        return new Fun<A,Fun<Maybe<A>,A>>() {
            @Override
            public Fun<Maybe<A>,A> app(final A dflt) { return fromMaybe(dflt); }
        };
    }

    public static <A> Maybe<A> listToMaybe(final Iterable<A> list) {
        final Iterator<A> it = list.iterator();
        return it.hasNext()
            ? just(it.next()) : Data.<A>nothing();
    }

    public static <A, B> B maybe(final B dflt, final Fun<A,B> f, Maybe<A> ma)
    { return ma.cata(dflt, f); }

    /**
     * find :: (a -> Bool) -> [a] -> Maybe a
     */
    public static <A> Maybe<A>
        find(final Fun<A,Boolean> pred, final Iterable<? extends A> ite) {
        for (A a : ite) {
            if (pred.app(a)) { return just(a); }
        }
        return nothing();
    }

    public static <A> Fun<Iterable<? extends A>, Maybe<A>>
        find(final Fun<A,Boolean> pred) {
        return new Fun<Iterable<? extends A>, Maybe<A>>() {
            @Override
            public Maybe<A> app(final Iterable<? extends A> ite) {
                return find(pred, ite);
            }
        };
    }

    public static <A> Fun<Fun<A,Boolean>, Fun<Iterable<? extends A>, Maybe<A>>>
      find() {
        return new Fun<Fun<A,Boolean>, Fun<Iterable<? extends A>, Maybe<A>>>() {
            @Override
            public Fun<Iterable<? extends A>, Maybe<A>> app(final Fun<A,Boolean> pred) {
                return find(pred);
            }
        };
    }

    public static final <A> Eq<Maybe<A>>  maybeEq(final Eq<A> eqA) {
        return  new Eq<Maybe<A>>() {
            @Override public boolean
                eq(Maybe<A> a1, Maybe<A> a2) {
                if (a1 == null && a2 == null) { return true; }
                if (a1 == null) { return false; }
                if (a2 == null) { return false; }
                if (a1.isNothing() && a2.isNothing()) { return true; }

                if (a1.isJust() && a2.isJust()) {
                    return eqA.eq(a1.fromJust(), a2.fromJust());
                }

                return false;
            }
        };
    }

    /* Monoid class and instances like Haskell */
    static public abstract class Monoid<A> {
        abstract public A mempty();
        abstract public A mappend(A x, A y);
        public A mconcat(final Iterable<A> xs) {
            A r = mempty();
            for (A x : xs) { r = mappend(r, x); }
            return r;
        }
    }

    static public final Monoid<String> stringMonoid = new Monoid<String>() {
        @Override
        public String mempty() { return ""; }
        @Override
        public String mappend(final String x, final String y) { return x + y; }
        @Override
        public String mconcat(final Iterable<String> xs) {
            StringBuffer b = new StringBuffer();
            for (String x : xs) { b.append(x); }
            return b.toString();
        }
    };

    static public <E> Monoid<E[]> arrayMonoid(final Class<E> ec) {
        return new Monoid<E[]>() {
            @Override
            @SuppressWarnings("unchecked") public E[]
                mempty() { return (E[])newInstance(ec, 0); }
            @Override
            public E[] mappend(final E[] x, final E[] y) {
                ArrayList<E> b = new ArrayList<E>();
                for (E e : x) { b.add(e); }
                for (E e : y) { b.add(e); }
                return b.toArray(x);
            }
            @Override
            public E[] mconcat(final Iterable<E[]> xs) {
                ArrayList<E> b = new ArrayList<E>();
                for (E[] x : xs) { for (E e : x) { b.add(e); } }
                return b.toArray(mempty());
            }
        };
    }

    static public <E> Monoid<ArrayList<E>> arrayListMonoid() {
        return new Monoid<ArrayList<E>>() {
            @Override
            public ArrayList<E> mempty()  { return new ArrayList<E>(); }
            @Override
            public ArrayList<E> mappend(final ArrayList<E> x, final ArrayList<E> y) {
                ArrayList<E> r = new ArrayList<E>();
                for (E e : x) { r.add(e); }
                for (E e : y) { r.add(e); }
                return r;
            }
            @Override
            public ArrayList<E> mconcat(final Iterable<ArrayList<E>> xs) {
                ArrayList<E> r = new ArrayList<E>();
                for (ArrayList<E> x : xs) { for (E e : x) { r.add(e); } }
                return r;
            }
        };
    }

    static public <E> Monoid<LinkedList<E>> linkedListMonoid() {
        return new Monoid<LinkedList<E>>() {
            @Override
            public LinkedList<E> mempty()  { return new LinkedList<E>(); }
            @Override
            public LinkedList<E> mappend(final LinkedList<E> x, final LinkedList<E> y) {
                LinkedList<E> r = new LinkedList<E>();
                for (E e : x) { r.add(e); }
                for (E e : y) { r.add(e); }
                return r;
            }
            @Override
            public LinkedList<E> mconcat(final Iterable<LinkedList<E>> xs) {
                LinkedList<E> r = new LinkedList<E>();
                for (LinkedList<E> x : xs) { for (E e : x) { r.add(e); } }
                return r;
            }
        };
    }
}
