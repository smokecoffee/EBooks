package com.atson.commons.lang;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.atson.commons.annotations.NonNull;


/**
 * @author hibi
 * @author wy8h-hsmt
 *
 */
public final class FP {

    private FP() { noInstance(getClass()); }

    public static void noInstance() {
        throw new AssertionError("�C���X�^���X�͍��Ȃ�");
    }

    /**
     * �u�������v�̎|��\�����s����O��new���ĕԂ��B<br>
     * �e���v���[�g�Ɏg���A�������ł��邱�Ƃ�\��<br>
     * ��<br>
     * throw FP.notImplemented();
     * @return �u�������v�̎|��\���Anew���ꂽ���s����O
     */
    public static RuntimeException notImplemented() {
        return new UnsupportedOperationException("not implemented yet");
    }

    /**
     * AssertionError���X���[���A�N���X�̐��������Ȃ����Ƃ�\������<br>
     * ��
     *
     * <pre>
     * private HogeClass() { noInstance(getClass()); }
     * </pre>
     *
     * @param clazz �ΏۃN���X
     */
    public static void noInstance(final Class<?> clazz) {
        throw new AssertionError("�C���X�^���X�͍��Ȃ�: " + clazz);
    }

    /**
     * <p>
     * (Haskell)<br>
     * {@code data () = ()}<br>
     * ��Java����
     * </p>
     * <p>
     * <br>
     * ������Unit type<br>
     * Fun�̌^�p�����[�^���Ɏg��
     * </p>
     * <p>
     * �Q�l<br>
     * http://en.wikipedia.org/wiki/Unit_type
     * </p>
     * @author hibi
     * @author wy8h-hsmt (comment)
     *
     */
    public static final class Unit {
        private Unit() {/* hide constructor */}
        private static final Unit INSTANCE = new Unit();
    }
    /**
     * Unit�^�̒l
     */
    public static final Unit UNIT = Unit.INSTANCE;

    /**
     * ���𕶂ɂ������Ƃ��Ɏg��<br>
     * ��<br>
     * {@code FP.toUnit(i % 2 == 0 ? doHoge(i) : doFuga(i));}<br>
     * <br>
     * ���̌��ʂ��̂Ă����Ƃ��Ɏg��<br>
     * ���Ƃ���new�̕���p�������K�v�ȏꍇ�ɁAEclipse��Unused object allocation�x�����������̂Ɏg����
     *
     * @return {@link #UNIT}
     */
    @SuppressWarnings("unused")
    public static <A> Unit toUnit(final A a) {
        return FP.UNIT;
    }

    public static <A> A unsupported() {
        throw new UnsupportedOperationException("undefined");
    }

    /**
     * <pre>
     * ���s����O�X���[�����ɂ���B3�����Ŏg��
     * ��
     * public class C {
     *     public C(final String str) {}
     *     public C(final Integer val) {
     *         this(val != null ? val.toString() :
     *             FP.&lt;String&gt; throwRuntime(new NullPointerException("val is null")));
     *     }
     * }
     * </pre>
     * @param <A> �C�ӂ̌^
     * @param runtimeException �X���[�����O null�͋֎~
     * @return runtimeException���X���[����̂ŉ���return���Ȃ�
     */
    @NonNull
    public static <A> A throwRuntime(@NonNull final RuntimeException runtimeException) {
        throw runtimeException;
    }

    /**
     * Error�̃X���[�����ɂ���
     * @param error �X���[�����O null�͋֎~
     * @param <A> �C�ӂ̌^
     * @return error���X���[����̂ŉ���return���Ȃ�
     */
    public static <A> A throwError(final Error error) {
        throw error;
    }


    /**
     * TODO �e�X�g
     * ex���X���[����֐�
     * @param <B> ���ʂ̌^
     * @param <X> �X���[�����O�̌^
     * @param ex
     * @return ex���X���[����֐�
     */
    public static <B, X extends Exception> EFun<Object, B, X> throwExceptionFun(final X ex) {
        return new EFun<Object, B, X>() {

            @Override
            public B app(final Object a) throws X {
                throw ex;
            }

        };
    }

    /**
     *
     * @param exception �X���[�����O null�͋֎~
     * @param <A> �C�ӂ̌^
     * @param <E> �X���[�����O�܂��̓X�[�p�[�N���X�̌^
     * @return exception���X���[����̂ŉ���return���Ȃ�
     * @throws E exception��null�łȂ��ꍇ�A�������ŃX���[�����
     */
    public static <A, E extends Throwable> A throwException(final E exception)
            throws E {
        throw exception;
    }
    
    /**
    * exceptionSupplier������������O���X���[����B
    * @param exceptionSupplier ��O�𐶐�����֐�
    * @param <A> �C�ӂ̌^
    * @param <E> �X���[�����O�܂��̓X�[�p�[�N���X�̌^
    * @return �������ꂽ��O���X���[����̂ŉ���return���Ȃ�
    * @throws E �������ꂽ��O���������ŃX���[�����
    */
    public static <A, E extends Throwable> A throwLazy(
            final Supplier<E> exceptionSupplier) throws E {
        throw exceptionSupplier.get();
   }

    /**
     * <pre>
     * IllegalStateException���X���[����B
     * (�����`�F�b�N || die("�`�F�b�N���s")) �Ə��������Ƃ��Ɏg���B
     *
     * ��1
     * if (hoge != null || FP.die("hoge is null")) {
     *     // do something
     * }
     *
     * ��2
     * FP.toUnit(hoge != null || FP.die("hoge is null"));
     * </pre>
     *
     * @param msg �X���[�����O�ɓn������������
     * @throws IllegalStateException �˂ɃX���[����
     * @return ���� return���Ȃ�
     */
    public static boolean die(final String msg) {
        throw new IllegalStateException(msg);
    }

    @SuppressWarnings("unused")
    public static <A> A undefined(final Unit u) { return FP.<A>unsupported(); }

    public static <A> Fun<Unit,A> undefined() {
        return new Fun<Unit,A>() {
            @Override
            public A app(final Unit u) { return FP.<A>undefined(u); }
        };
    }


    /**
     * ��O���X���[���邩������Ȃ��֐��B�S��Java8�ɂȂ�����@FunctionalInterface �ɂ���
     * @author wy8h-hsmt
     *
     * @param <A> �����̌^
     * @param <B> ���ʂ̌^
     * @param <X> ��O�̌^
     */
    public interface EFun<A, B, X extends Exception> {
        B app(A a) throws X;
    }
    
    /**
     * ��O���X���[���邩������Ȃ��߂�l�̂Ȃ��֐��B�S��Java8�ɂȂ�����@FunctionalInterface �ɂ���
     * ������ java.util.function.Consumer ���
     * @author wy8h-hsmt
     *
     * @param <A> �����̌^
     * @param <X> ��O�̌^
     */
    public interface EConsumer<A, X extends Exception> {
        void accept(A a) throws X;
    }


    /**
     * EFun{@code <A, Unit, X>} �̊ȈՎ���
     * @author wy8h-hsmt
     *
     * @param <A> �����̌^
     * @param <X> ��O�̌^
     */
    public abstract static class EFunVoid<A, X extends Exception> implements EFun<A, Unit, X>{
        /**
         * �T�u�N���X�Ńu���b�N�̋L�q������B
         */
        public abstract void execute(A a) throws X;

        @Override
        public final Unit app(final A a) throws X {
            execute(a);
            return UNIT;
        }

    }

    /**
     * TODO test
     * EFun�̈����^�k���A�߂�l�^�g���A��O���b�v
     * @param <A> �ϊ�������^
     * @param <B> �ϊ���߂�l�^
     * @param <X> �ϊ����O�̌^
     * @param orig �ϊ�������EFun
     * @param wrap ��O�����b�v����X�^�ɕϊ�����֐�
     * @return EFun<A, B, X> �ɕϊ����ꂽorig
     */
    public static <A, B, X extends Exception> EFun<A, B, X> efun(
            final EFun<? super A, ? extends B, ?> orig,
            final Fun<Exception, X> wrap) {
        return new EFun<A, B, X>() {

            @Override
            public B app(final A a) throws X {
                try {
                    return orig.app(a);
                } catch (Exception e) {
                    throw wrap.app(e);
                }
            }

        };
    }

    /**
     * �֐��C���^�[�t�F�[�X�B�S��Java8�ɂȂ�����@FunctionalInterface �ɂ���
     * @author wy8h-hsmt (comment)
     *
     * @param <A> �����̌^
     * @param <B> ���ʂ̌^
     */
    public interface Fun<A,B> extends EFun<A, B, RuntimeException> {
        @Override B app(A a);
    }
    
    /**
     * �֐��C���^�[�t�F�[�X�B�S��Java8�ɂȂ�����@FunctionalInterface �ɂ���B
     * 
     * EConsumer ������Ȃ炱�������~�����̂Œǉ��B
     * 
     * @author af6h-sgym
     *
     * @param <A>
     *            �����̌^
     */
    public interface Consumer<A> extends EConsumer<A, RuntimeException> {
        @Override void accept(A a);
    }
    
    /**
     * java.util.function���
     * @author wy8h-hsmt
     *
     * @param <A> �߂�l�̌^
     */
    public interface Supplier<A> {
        A get();
    }

    /**
     * Fun<A, Unit> �̊ȈՎ���
     * 
     * @author wy8h-hsmt
     *
     * @param <A>
     *            �����̌^
     */
    public abstract static class FunVoid<A> implements Fun<A, Unit> {
        public abstract void execute(A a);

        @Override
        public final Unit app(final A a) {
            execute(a);
            return UNIT;
        }

    }

    /**
     * \() -> a
     * @see {@link #const1(Object)} �̌^�����
     * @param a
     */
    public static <A> Fun<Unit,A>fun0(final A a) {
        return new Fun<Unit,A>() {
            @Override
            public A app(final Unit u) { return a; }
        };
    }

    /**
     * ������Unit�̊֐� EFun<Unit, R, X> �̊ȈՎ���<br>
     *
     * @author wy8h-hsmt
     *
     * @param <R> execute�̌��ʂ̌^
     * @param <X> ��O�̌^
     */
    public abstract static class EBlock<R, X extends Exception> implements EFun<Unit, R, X> {
        /**
         * �T�u�N���X�Ńu���b�N�̋L�q������B
         * @throws X
         */
        public abstract R execute() throws X;

        @Override
        public final R app(final Unit u) throws X {
            return execute();
        }

    }

    /**
     * ������Unit�A�߂�l��Unit�̊֐� EFunVoid<Unit, X> �̊ȈՎ���
     *
     * @author wy8h-hsmt
     *
     * @param <X> ��O�̌^
     */
    public abstract static class EBlockVoid<X extends Exception> extends
            EFunVoid<Unit, X> {
        /**
         * �T�u�N���X�Ńu���b�N�̋L�q������B
         */
        public abstract void execute() throws X;

        @Override
        public final void execute(final Unit a) throws X {
            execute();
        }

    }

    /**
     * ������Unit�̊֐� Fun<Unit, R> �̊ȈՎ���<br>
     *
     * @author wy8h-hsmt
     *
     * @param <R> execute�̌��ʂ̌^
     */
    public abstract static class Block<R> implements Fun<Unit, R> {
        /**
         * �T�u�N���X�Ńu���b�N�̋L�q������B
         */
        public abstract R execute();

        @Override
        public final R app(final Unit u) {
            return execute();
        }

    }

    /**
     * ������Unit, �߂�l��Unit�̊֐� FunVoid{@code <Unit>}�̊ȈՎ���
     * @author wy8h-hsmt
     *
     */
    public abstract static class BlockVoid extends FunVoid<Unit> {
        /**
         * �T�u�N���X�Ńu���b�N�̋L�q������B
         */
        public abstract void execute();

        @Override
        public final void execute(final Unit a) {
            execute();
        }

    }

    // base functions

    /**
     * Pair �łȂ̂ł��Â���� -- 2-tuple �R���X�g���N�^ -- (,) :: a -> b -> (a, b)
     */
    public static <A,B> Fun<A, Fun<B, Pair<A, B>>> pair() {
        return new Fun<A, Fun<B, Pair<A, B>>>() {
            @Override
            public Fun<B, Pair<A, B>> app(final A a) {
                return new Fun<B, Pair<A, B>>() {
                    @Override
                    public Pair<A, B> app(final B b) {
                        return Pair.of(a, b);
                    }
                };
            }
        };
    }

    /**
     * Pair �łȂ̂ł��Â���� -- Java���\�b�h�� 2-tuple �R���X�g���N�^
     */
    public static <A,B> Pair<A, B> pair(final A a, final B b) {
        return
            FP.<A,B, Pair<A, B>>
            app2(FP.<A,B>pair(),
                   a, b);
    }

    public static Fun<String,Integer> readInteger() {
        return new Fun<String,Integer>() {
            @Override
            public Integer app(final String str) { return Integer.parseInt(str); }
        };
    }

    public static Fun<String,Byte> readByte() {
        return new Fun<String,Byte>() {
            @Override
            public Byte app(final String str) { return Byte.parseByte(str); }
        };
    }

    public static Fun<String,Short> readShort() {
        return new Fun<String,Short>() {
            @Override
            public Short app(final String str) { return Short.parseShort(str); }
        };
    }

    public static Fun<String,Long> readLong() {
        return new Fun<String,Long>() {
            @Override
            public Long app(final String str) { return Long.parseLong(str); }
        };
    }

    public static Fun<String,Boolean> readBoolean() {
        return new Fun<String,Boolean>() {
            @Override
            public Boolean app(final String str) { return Boolean.parseBoolean(str); }
        };
    }

    public static Fun<String,Float> readFloat() {
        return new Fun<String,Float>() {
            @Override
            public Float app(final String str) { return Float.parseFloat(str); }
        };
    }

    public static Fun<String,Double> readDouble() {
        return new Fun<String,Double>() {
            @Override
            public Double app(final String str) { return Double.parseDouble(str); }
        };
    }

    // End -- base functions

    // Functions like Data.Function of Haskell

    /**
     * �P���֐� -- id :: a -> a
     */
    public static <A> Fun<A,A> id() {
        return new Fun<A,A>() {
            @Override
            public A app(final A a) {
                return a;
            }
        };
    }

    /**
     * const = \a _ -> a<br>
     * {@link #const_}�̌^�p�����[�^Object��
     */
    public static <A> Fun<A,Fun<Object,A>> const1() {
        return const_();
    }

    /**
     * -- const :: a -> b -> a<br>
     * const�̓L�[���[�h�Ȃ̂Œ���
     */
    public static <A,B> Fun<A,Fun<B,A>> const_() {
        return new Fun<A,Fun<B,A>>() {
            @Override
            public Fun<B,A> app(final A a) {
                return new Fun<B,A>() {
                    @Override
                    public A app(final B b) {
                        return a;
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� const<br>
     * {@link #const_(Object)}�̌^�p�����[�^Object��
     */
    public static <A> Fun<Object,A> const1(final A a) {
        return FP.<A>const1().app(a);
    }

    /**
     * c
     * Java���\�b�h�� const
     */
    public static <A,B> Fun<B,A> const_(final A a) {
        return FP.<A,B>const_().app(a);
    }

    /**
     * �֐����ꉻ
     */
    public static <A, B>
        Fun<Fun<? super A,? extends B>, Fun<A, B>> specialize() {
        return new Fun<Fun<? super A,? extends B>, Fun<A, B>>() {
            @Override
            public Fun<A, B> app(final Fun<? super A,? extends B> gf) {
                return specialize(gf);
            }
        };
    }

    /**
     * Java���\�b�h�� �֐����ꉻ
     */
    public static <A, B>
        Fun<A, B> specialize(final Fun<? super A,? extends B> gf) {
        return new Fun<A, B>() {
            @Override
            public B app(final A a) { return gf.app(a); }
        };
    }

    /**
     * �֐����� -- (.) :: (b -> c) -> (a -> b) -> a -> c
     */
    public static <A, B, C>
        Fun<Fun<? super B,? extends C>, Fun<Fun<? super A,? extends B>, Fun<A,C>>> comp() {
        return
            new Fun<Fun<? super B,? extends C>, Fun<Fun<? super A,? extends B>, Fun<A,C>>>() {
            @Override
            public Fun<Fun<? super A,? extends B>, Fun<A,C>>
                app(final Fun<? super B,? extends C> f) {
                return new Fun<Fun<? super A,? extends B>, Fun<A,C>>() {
                    @Override
                    public Fun<A,C> app(final Fun<? super A,? extends B> g) {
                        return new Fun<A,C>() {
                            @Override
                            public C app(final A a) {
                                return f.app(g.app(a));
                            }
                        };
                    }
                };
            }
        };
    }

    public static <A, B, C>
        Fun<Fun<? super A,? extends B>, Fun<A,C>> comp(final Fun<? super B,? extends C> f) {
        return FP.<A,B,C>comp().app(f);
    }

    /**
     * Java���\�b�h�� �֐�����
     */
    public static <A,B,C> Fun<A,C> comp(final Fun<? super B,? extends C> f,
                                        final Fun<? super A,? extends B> g) {
        return FP.<A,B,C>comp().app(f).app(g);
    }

    /**
     * �֐������w���p�[
     */
    public static <A,B,C,D> Fun<A,D> comp3(final Fun<? super C,? extends D> f,
                                           final Fun<? super B,? extends C> g,
                                           final Fun<? super A,? extends B> h) {
        return FP.<A,C,D>comp().app(f)
            .app(FP.<A,B,C>comp().app(g).app(h));
    }

    public static <A,B,C,D,E> Fun<A,E> comp4(final Fun<? super D,? extends E> p,
                                             final Fun<? super C,? extends D> q,
                                             final Fun<? super B,? extends C> r,
                                             final Fun<? super A,? extends B> s) {
        return FP.<A,D,E>comp().app(p)
            .app(comp3(q, r, s));
    }

    public static <A,B,C,D,E,F> Fun<A,F> comp5(final Fun<? super E,? extends F> p,
                                               final Fun<? super D,? extends E> q,
                                               final Fun<? super C,? extends D> r,
                                               final Fun<? super B,? extends C> s,
                                               final Fun<? super A,? extends B> t) {
        return FP.<A,E,F>comp().app(p)
            .app(comp4(q, r, s, t));
    }

    /**
     * flip :: (a -> b -> c) -> b -> a -> c
     */
    public static <A,B,C>
        Fun<
        Fun<A, Fun<B, C>>,
        Fun<B, Fun<A, C>>> flip() {
        return new Fun<
            Fun<A, Fun<B, C>>,
            Fun<B, Fun<A, C>>>() {
            @Override
            public Fun<B, Fun<A, C>>
                app(final Fun<A, Fun<B, C>> f) {
                return new Fun<B, Fun<A, C>>() {
                    @Override
                    public Fun<A, C> app(final B b) {
                        return new Fun<A, C>() {
                            @Override
                            public C app(final A a) {
                                return f.app(a).app(b);
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� flip
     */
    public static <A,B,C>
        Fun<B, Fun<A, C>> flip(final Fun<A, Fun<B, C>> f) {
        return FP.<A,B,C>flip().app(f);
    }

    /**
     * �֐��K�p -- ($) :: (a -> b) -> a -> b
     */
    public static <A,B> Fun<Fun<A,B>, Fun<A,B>> app() {
        return id();
    }

    /**
     * Java���\�b�h�� �֐��K�p
     */
    public static <A,B> B app(final Fun<A,B> f, final A a) {
        return f.app(a);
    }

    /**
     * �֐��K�p�w���p�[
     */
    public static <A,B,C>
        C app2(final Fun<A, Fun<B, C>> f,
                 final A a, final B b) {
        return f.app(a).app(b);
    }

    /**
     * �֐��K�p�w���p�[
     */
    public static <A,B,C,D>
        D app3(final Fun<A, Fun<B, Fun<C, D>>> f,
                 final A a, final B b, final C c) {
        return FP.<A,B,Fun<C,D>>app2(f, a, b).app(c);
    }

    // -- fix -- not implemented

    /**
     * -- on :: (b -> b -> c) -> (a -> b) -> a -> a -> c
     */
    public static <A,B,C> Fun<
        Fun<B,Fun<B,C>>,
        Fun<Fun<? super A,B>, Fun<A,Fun<A,C>>>>
        on() {
        return new Fun<
            Fun<B,Fun<B,C>>,
            Fun<Fun<? super A,B>, Fun<A,Fun<A,C>>>>() {
            @Override
            public Fun<Fun<? super A,B>, Fun<A,Fun<A,C>>>
                app(final Fun<B,Fun<B,C>> cmp) {
                return new Fun<Fun<? super A,B>, Fun<A,Fun<A,C>>>(){
                    @Override
                    public Fun<A,Fun<A,C>> app(final Fun<? super A,B> conv) {
                        return new Fun<A,Fun<A,C>>() {
                            @Override
                            public Fun<A,C> app(final A x) {
                                return new Fun<A,C>() {
                                    @Override
                                    public C app(final A y) {
                                        return cmp
                                            .app(conv.app(x))
                                            .app(conv.app(y));
                                    }
                                };
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� on
     */
    public static <A,B,C> Fun<A,Fun<A,C>>
        on(final Fun<B,Fun<B,C>> cmp, final Fun<A,B> conv) {
        return FP.<A,B,C>on().app(cmp).app(conv);
    }

    /**
     * -- Eq����̎����グ
     * -- onEq :: (Eq b) -> (a -> b) -> (Eq a)
     */
    public static <A,B> Fun<
        Eq<B>,
        Fun<Fun<A,B>, Eq<A>>>
        onEq() {
        return new Fun<
            Eq<B>,
            Fun<Fun<A,B>, Eq<A>>>() {
            @Override
            public Fun<Fun<A,B>, Eq<A>> app(final Eq<B> eq) {
                return new Fun<Fun<A,B>, Eq<A>>() {
                    @Override
                    public Eq<A> app(final Fun<A,B> f) {
                        return defEq(on(eqFun(eq), f));
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� onEq
     */
    public static <A,B> Eq<A>
        onEq(final Eq<B> eq, final Fun<A,B> conv) {
        return FP.<A,B>onEq().app(eq).app(conv);
    }

    @Deprecated public static <A,B> Eq<A>
        on(final Eq<B> eq, final Fun<A,B> conv) {
        return onEq(eq, conv);
    }


    public static <A,B> Comparator<A>
        onComparator(final Comparator<B> cmp, final Fun<A,? extends B> conv) {
        return new Comparator<A>() {
            @Override
            public int compare(final A a1, final A a2) {
                return cmp.compare(conv.app(a1),
                                   conv.app(a2));
            }
        };
    }

    // End -- Functions like Data.Function of Haskell

    // Functions like Data.Tuple of Haskell

    /**
     * {@link #fst()} �̃��C���h�J�[�h��
     * @return \(a,_) -> a
     */
    public static <A> Fun<T2<? extends A, ?>, A> fst1() {
        return new Fun<T2<? extends A, ?>, A>() {
            @Override
            public A app(final T2<? extends A, ?> p) {
                return p.fst();
            }
        };
    }
    /**
     * -- fst :: (a, b) -> a
     */
    public static <A,B> Fun<T2<A,B>, A> fst() {
        return new Fun<T2<A,B>, A>() {
            @Override
            public A app(final T2<A,B> p) {
                return p.fst();
            }
        };
    }

    public static <A,B> Fun<Pair<A,B>, A> fst__() {
        return new Fun<Pair<A,B>, A>() {
            @Override
            public A app(final Pair<A,B> p) {
                return p.fst();
            }
        };
    }


    /**
     * {@link #snd()} �̃��C���h�J�[�h��
     * @return \(_,b) -> b
     */
    public static <B> Fun<T2<?, ? extends B>, B> snd1() {
        return new Fun<T2<?, ? extends B>, B>() {
            @Override
            public B app(final T2<?, ? extends B> p) {
                return p.snd();
            }
        };
    }

    /**
     * -- snd :: (a, b) -> b
     */
    public static <A,B> Fun<T2<A,B>, B> snd() {
        return new Fun<T2<A,B>, B>() {
            @Override
            public B app(final T2<A,B> p) {
                return p.snd();
            }
        };
    }

    public static <A,B> Fun<Pair<A,B>, B> snd__() {
        return new Fun<Pair<A,B>, B>() {
            @Override
            public B app(final Pair<A,B> p) {
                return p.snd();
            }
        };
    }

    /**
     * �J���[�� -- curry :: ((a, b) -> c) -> a -> b -> c
     */
    public static <A,B,C>
        Fun<
        Fun<T2<A,B>, C>,
        Fun<A, Fun<B, C>>> curry() {
        return new Fun<
            Fun<T2<A,B>, C>,
            Fun<A, Fun<B, C>>>() {
            @Override
            public Fun<A, Fun<B, C>>
                app(final Fun<T2<A,B>, C> f) {
                return new Fun<A, Fun<B, C>>() {
                    @Override
                    public Fun<B, C> app(final A a) {
                        return new Fun<B, C>() {
                            @Override
                            public C app(final B b) {
                                return f.app(FP.<A,B>t2(a, b));
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� �J���[��
     */
    public static <A,B,C> Fun<A, Fun<B, C>> curry(final Fun<T2<A,B>, C> f) {
        return FP.<A,B,C>curry().app(f);
    }

    /**
     * �J���[���t���Z -- uncurry :: (a -> b -> c) -> (a, b) -> c
     */
    public static <A,B,C>
        Fun<
        Fun<A, Fun<B, C>>,
        Fun<T2<? extends A, ? extends B>, C>> uncurry() {
        return new Fun<
            Fun<A, Fun<B, C>>,
            Fun<T2<? extends A, ? extends B>, C>>() {
            @Override
            public Fun<T2<? extends A, ? extends B>, C>
                app(final Fun<A, Fun<B, C>> f) {
                return new Fun<T2<? extends A, ? extends B>, C>() {
                    @Override
                    public C app(final T2<? extends A, ? extends B> p) {
                        return f.app(p.fst()).app(p.snd());
                    }
                };
            }
        };
    }

    /**
     * �J���[���t���Z -- uncurry :: (a -> b -> c) -> (a, b) -> c
     */
    public static <A,B,C>
        Fun<
        Fun<A, Fun<B, C>>,
        Fun<Pair<A,B>, C>> uncurry__() {
        return new Fun<
            Fun<A, Fun<B, C>>,
            Fun<Pair<A,B>, C>>() {
            @Override
            public Fun<Pair<A,B>, C>
                app(final Fun<A, Fun<B, C>> f) {
                return new Fun<Pair<A,B>, C>() {
                    @Override
                    public C app(final Pair<A,B> p) {
                        return f.app(p.fst()).app(p.snd());
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� �J���[���t���Z
     */
    public static <A,B,C> Fun<T2<? extends A, ? extends B>, C> uncurry(final Fun<A, Fun<B, C>> f) {
        return FP.<A,B,C>uncurry().app(f);
    }

    /**
     * Java���\�b�h�� �J���[���t���Z
     */
    public static <A,B,C> Fun<T2<? extends A, ? extends B>, C> uncurry_(final Fun<A, Fun<B, C>> f) {
        return FP.<A,B,C>uncurry(f);
    }

    /**
     * Java���\�b�h�� �J���[���t���Z
     */
    public static <A,B,C> Fun<Pair<A,B>, C> uncurry__(final Fun<A, Fun<B, C>> f) {
        return FP.<A,B,C>uncurry__().app(f);
    }

    /**
     * -- swap :: (a, b) -> (b, a)
     */
    public static <A,B> Fun<T2<A,B>,T2<B,A>> swap() {
        //return uncurry(flip(FP.<B,A>pair()));
        return new Fun<T2<A,B>,T2<B,A>>() {
            @Override
            public T2<B,A> app(final T2<A,B> ab) {
                return t2(ab.snd(), ab.fst());
            }
        };
    }

    /**
     * Java���\�b�h�� swap
     */
    public static <A,B> T2<B,A> swap(final T2<A,B> ab) {
        return FP.<A,B>swap().app(ab);
    }

    // End -- Functions like Data.Tuple of Haskell

    // Equality definition
    public interface Eq<T> {
        boolean eq(T o1, T o2);
    }

    public static <T> Eq<T>
        defEq(final Fun<T,Fun<T,Boolean>> p) {
        return new Eq<T>() {
            @Override
            public boolean eq(final T o1, final T o2) {
                return app2(p, o1, o2);
            }
        };
    }

    public static <T> Fun<T,Fun<T,Boolean>>
        eqFun(final Eq<T> eq) {
        return new Fun<T,Fun<T,Boolean>>() {
            @Override
            public Fun<T,Boolean> app(final T o1) {
                return new Fun<T,Boolean>() {
                    @Override
                    public Boolean app(final T o2) {
                        return eq.eq(o1, o2);
                    }
                };
            }
        };
    }


    public static <T> Eq<T>
        defEqOfJava() {
        return new Eq<T>() {
            @Override
            public boolean eq(final T o1, final T o2) {
                if (o1 != null) {
                    return o1.equals(o2);
                }
                if (o2 != null) {
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * �����񋓌^���m�̔�r������
     * @param <E> �񋓌^
     * @return E���m���r����{@link Eq}
     */
    public static <E extends Enum<E>> Eq<E> enumEq() {
        return defEqOfJava();
    }

    public static final Eq<Boolean>      boolEq      = defEqOfJava();
    public static final Eq<Byte>         byteEq      = defEqOfJava();
    public static final Eq<Character>    characterEq = defEqOfJava();
    public static final Eq<CharSequence> charsEq     = defEqOfJava();
    public static final Eq<Double>       doubleEq    = defEqOfJava();
    public static final Eq<Enum<?>>      enumEq      = defEqOfJava();
    public static final Eq<Float>        floatEq     = defEqOfJava();
    public static final Eq<Integer>      integerEq   = defEqOfJava();
    public static final Eq<Long>         longEq      = defEqOfJava();
    public static final Eq<Object>       objectEq    = defEqOfJava();
    public static final Eq<Short>        shortEq     = defEqOfJava();
    public static final Eq<String>       stringEq    = defEqOfJava();
    public static final Eq<Void>         voidEq      = defEqOfJava();

    public static final Eq<List<?>>       listEq       = defEqOfJava();
    public static final Eq<ArrayList<?>>  arrayListEq  = defEqOfJava();
    public static final Eq<LinkedList<?>> linkedListEq = defEqOfJava();

    // Against java.util.Comparator
    public static <T> Comparator<T>
        defComparator(final Fun<T,Fun<T,Integer>> cp) {
        return new Comparator<T>() {
            @Override
            public int compare(final T o1, final T o2) {
                return app2(cp, o1, o2);
            }
        };
    }

    public static <T> Fun<T,Fun<T,Integer>>
        ofComparator(final Comparator<T> cp) {
        return new Fun<T,Fun<T,Integer>>() {
            @Override
            public Fun<T,Integer> app(final T o1){
                return new Fun<T,Integer>() {
                    @Override
                    public Integer app(final T o2) {
                        return cp.compare(o1, o2);
                    }
                };
            }
        };
    }

    public static <T extends Comparable<T>> Comparator<T>
        comparableComparator() {
        return new Comparator<T>() {
            @Override
            public int compare(final T o1, final T o2) {
                if (o1 != null) {
                    return o1.compareTo(o2);
                }
                if (o2 != null) {
                    return ComparatorUtil.negateCompare(o2.compareTo(o1));
                }
                // o1 == null && o2 == null
                return 0;

            }
        };
    }

    public static <T extends Comparable<T>> Fun<T,Fun<T,Integer>>
        ofComparable() {
        return ofComparator(FP.<T>comparableComparator());
    }

    public static <A> boolean any(final FP.Fun<? super A, Boolean> pred,
            final Iterable<A> ite) {
        for (A a : ite) {
            if (pred.app(a)) {
                return true;
            }
        }
        return false;
    }

    public static <A> boolean all(final FP.Fun<? super A, Boolean> pred,
            final Iterable<A> ite) {
        for (A a : ite) {
            if (!pred.app(a)) {
                return false;
            }
        }
        return true;
    }

    /**
     * show :: Show a => a -> String
     */
    public static <A> Fun<A,String> show() {
        return new Fun<A,String>() {
            @Override
            public String app(final A a) { return a.toString(); }
        };
    }

    // Functions like Data.List of Haskell

    /**
     * ����ݍ��� -- foldl :: (a -> b -> a) -> a -> [b] -> a
     */
    public static <A, B>
        Fun<Fun<A, Fun<B, A>>, Fun<A, Fun<Iterable<B>, A>>>
        foldl() {
        return new Fun<Fun<A, Fun<B, A>>, Fun<A, Fun<Iterable<B>, A>>>() {
            @Override
            public Fun<A, Fun<Iterable<B>, A>>
                app(final Fun<A, Fun<B, A>> op) {
                return new Fun<A, Fun<Iterable<B>, A>>() {
                    @Override
                    public Fun<Iterable<B>, A> app(final A init) {
                        return new Fun<Iterable<B>, A>() {
                            @Override
                            public A app(final Iterable<B> list) {
                                A res = init;
                                for (B b : list) {
                                    res = op.app(res).app(b);
                                }
                                return res;
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Java���\�b�h�� ����ݍ���
     */
    public static <A, B> A foldl(final Fun<A, Fun<B, A>> op,
                                 final A init,
                                 final Iterable<B> list) {
        return FP.<A,B>foldl().app(op).app(init).app(list);
    }

    /**
     *  head :: [a] -> a
     */
    public static <A> Fun<Iterable<? extends A>, A> head() {
        return new Fun<Iterable<? extends A>, A>() {
            @Override
            public A app(final Iterable<? extends A> list) {
                return list.iterator().next();
            }
        };
    }

    public static <A> Fun<List<? extends A>, A> head_() {
        return downArgToList(FP.<A>head());
    }

    /**
     *  Java���\�b�h�� head
     */
    public static <A> A head(final Iterable<? extends A> list) {
        return FP.<A>head().app(list);
    }

    /**
     *  maximumBy :: (a -> a -> Ordering) -> [a] -> a
     */
    public static <A> A
        maximumBy(final Comparator<A> c, final Iterable<? extends A> ite) {
        boolean first = true;
        A max = null;
        for(A a : ite) {
            if (first) {
                first = false;
                max = a;
                continue;
            }
            if (c.compare(max, a) < 0) {
                max = a;
            }
        }
        return max;
    }

    /**
     *  minimumBy :: (a -> a -> Ordering) -> [a] -> a
     */
    public static <A> A
        minimumBy(final Comparator<A> c, final Iterable<? extends A> ite) {
        boolean first = true;
        A min = null;
        for(A a : ite) {
            if (first) {
                first = false;
                min = a;
                continue;
            }
            if (c.compare(min, a) > 0) {
                min = a;
            }
        }
        return min;
    }

    // End -- Functions like Data.List of Haskell

    public static <E> E[] array(final E... e) {
        return e;
    }

    /*
    public static <E,X> Fun<X,E[]> consArray() {
        return new Fun<X,E[]>() {
            public E[] app(X __) {
                return FP.<E>array();
            }
        };
    }
    */

    public static <E> LinkedList<E> linkedList() {
        return new LinkedList<E>();
    }

    public static <E> LinkedList<E> linkedList(final E... e) {
        return new LinkedList<E>(Arrays.asList(e));
    }

    /**
     * {@link LinkedList#LinkedList(Collection)} ��static���\�b�h��<br>
     * �������ۑ������̂ŁA{@code List<SubE>} ����{@code List<E>} �̕ϊ��Ɏg����
     *
     * @param collection
     * @return collection�̃C�e���[�^�̏����ŗv�f���R�s�[���ꂽArrayList
     */
    public static <E> LinkedList<E> linkedList(final Collection<? extends E> collection) {
        return new LinkedList<E>(collection);
    }

    public static <E,X> Fun<X,LinkedList<E>> consLinkedList() {
        return new Fun<X,LinkedList<E>>() {
            @Override
            public LinkedList<E> app(final X __) {
                return FP.<E>linkedList();
            }
        };
    }

    /**
     * {@link #consLinkedList()}�̌^�p�����[�^Object��
     */
    public static <E> Fun<Object,LinkedList<E>> consLinkedList1() {
        return new Fun<Object,LinkedList<E>>() {
            @Override
            public LinkedList<E> app(final Object __) {
                return FP.<E>linkedList();
            }
        };
    }

    public static <E> LinkedList<E> linkedListJ(final E e, final int n) {
        LinkedList<E> el = new LinkedList<E>();
        for (int i = 0; i < n; i++) {
            el.add(e);
        }
        return el;
    }

    @Deprecated
    public static <E> LinkedList<E> linkedEmpty() {
        return linkedList();
    }

    public static <E> ArrayList<E> arrayList() {
        return new ArrayList<E>();
    }

    public static <E> ArrayList<E> arrayList(final E... e) {
        return new ArrayList<E>(Arrays.asList(e));
    }

    /**
     * {@link ArrayList#ArrayList(Collection)} ��static���\�b�h��<br>
     * �������ۑ������̂ŁA{@code List<SubE>} ����{@code List<E>} �̕ϊ��Ɏg����
     *
     * @param collection
     * @return collection�̃C�e���[�^�̏����ŗv�f���R�s�[���ꂽArrayList
     */
    public static <E> ArrayList<E> arrayList(final Collection<? extends E> collection) {
        return new ArrayList<E>(collection);
    }

    public static <E> ArrayList<E> arrayList(final E e, final int n) {
        ArrayList<E> el = new ArrayList<E>();
        for (int i = 0; i < n; i++) {
            el.add(e);
        }
        return el;
    }

    @Deprecated
    public static <E> ArrayList<E> arrayEmpty() {
        return arrayList();
    }

    public static <E,X> Fun<X,ArrayList<E>> consArrayList() {
        return new Fun<X,ArrayList<E>>() {
            @Override
            public ArrayList<E> app(final X __) {
                return FP.<E>arrayList();
            }
        };
    }

    /**
     * {@link #consArrayList()}�̌^�p�����[�^Object��
     */
    public static <E> Fun<Object,ArrayList<E>> consArrayList1() {
        return new Fun<Object,ArrayList<E>>() {
            @Override
            public ArrayList<E> app(final Object __) {
                return FP.<E>arrayList();
            }
        };
    }

    /**
     * @param <E> �v�f�^ Comparable�p���K�{
     * @return ��� TreeSet
     */
    public static <E extends Comparable<? super E>> TreeSet<E> treeSet() {
        return new TreeSet<E>();
    }

    /**
     * @param es �v�f�̗�
     * @param <E> �v�f�^ Comparable�p���K�{
     * @return es�̗v�f����ꂽTreeSet
     */
    public static <E extends Comparable<? super E>> TreeSet<E> treeSet(final E... es) {
        return new TreeSet<E>(Arrays.asList(es));
    }

    public static <E> TreeSet<E> treeSet(final Comparator<E> comp) {
        return new TreeSet<E>(comp);
    }

    public static <E> TreeSet<E> treeSet(final Comparator<E> comp, final E... e) {
        TreeSet<E> s = new TreeSet<E>(comp);
        s.addAll(Arrays.asList(e));
        return s;
    }

    public static <E> Fun<Comparator<E>,TreeSet<E>> consTreeSet() {
        return new Fun<Comparator<E>,TreeSet<E>>() {
            @Override
            public TreeSet<E> app(final Comparator<E> comp) {
                return FP.<E>treeSet(comp);
            }
        };
    }

    public static <E> HashSet<E> hashSet() {
        return new HashSet<E>();
    }

    public static <E> HashSet<E> hashSet(final E... e) {
        return new HashSet<E>(Arrays.asList(e));
    }

    public static <E> HashSet<E> hashSet(final Collection<E> es) {
        return new HashSet<E>(es);
    }

    public static <E,X> Fun<X,HashSet<E>> consHashSet() {
        return new Fun<X,HashSet<E>>() {
            @Override
            public HashSet<E> app(final X __) {
                return FP.<E>hashSet();
            }
        };
    }

    /**
     * {@link #consHashSet()}�̌^�p�����[�^Object��
     */
    public static <E> Fun<Object,HashSet<E>> consHashSet1() {
        return new Fun<Object,HashSet<E>>() {
            @Override
            public HashSet<E> app(final Object __) {
                return FP.<E>hashSet();
            }
        };
    }

    public static <E> Fun<E,Boolean> elemOf(final E... es) {
        return new Fun<E,Boolean>() {
            final HashSet<E> tree = hashSet(es);
            @Override public Boolean app(final E e) {
                return this.tree.contains(e);
            }
        };
    }

    public static <E> LinkedHashSet<E> linkedHashSet() {
        return new LinkedHashSet<E>();
    }

    public static <E> LinkedHashSet<E> linkedHashSet(final E... e) {
        return new LinkedHashSet<E>(Arrays.asList(e));
    }

    public static <E,X> Fun<X,LinkedHashSet<E>> consLinkedHashSet() {
        return new Fun<X,LinkedHashSet<E>>() {
            @Override
            public LinkedHashSet<E> app(final X __) {
                return FP.<E>linkedHashSet();
            }
        };
    }

    /**
     * {@link #consLinkedHashSet()}�̌^�p�����[�^Object��
     */
    public static <E> Fun<Object,LinkedHashSet<E>> consLinkedHashSet1() {
        return new Fun<Object,LinkedHashSet<E>>() {
            @Override
            public LinkedHashSet<E> app(final Object __) {
                return FP.<E>linkedHashSet();
            }
        };
    }

    /**
     * HashMap��new���� ���_�ł���ꍇ�͌^�p�����[�^���ȗ��ł���
     *
     * @return new HashMap
     */
    public static <K, V> HashMap<K, V> hashMap() {
        return new HashMap<K, V>();
    }

    public static <K, V> HashMap<K, V> hashMap(final T2<K, V>... ps) {
        HashMap<K, V> n = new HashMap<K, V>();
        for (T2<K, V> p : ps) {
            n.put(p.fst(), p.snd());
        }
        return n;
    }

    public static <K, V> HashMap<K, V>
        hashMap(final Collection<? extends T2<? extends K, ? extends V>> ps) {
        HashMap<K, V> n = new HashMap<K, V>();
        for (T2<? extends K, ? extends V> p : ps) {
            n.put(p.fst(), p.snd());
        }
        return n;
    }

    /**
     * EnumSet.of(...) �ő�։\
     */
    @Deprecated
    public static <E extends Enum<E>> EnumSet<E> enumSet(final E... e) {
        return EnumSet.copyOf(Arrays.asList(e));
    }

    public static <B,A extends B> Fun<A,B> upCast() {
        return new Fun<A,B>() {
            @Override
            public B app(final A a) { return a; }
        };
    }

    /**
     *  �A��(Conclusion)��upcast
     */
    public static <A,B,C extends B> Fun<A,B> upCastC(final Fun<A,C> f) {
        return comp(FP.<B,C>upCast(), f);
    }

    public static <A,B,C extends B> Fun<Fun<? super A,? extends C>, Fun<A,B>> upCastC() {
        return FP.<A,C,B>comp(FP.<B,C>upCast());
    }

    /**
     *  �O��(Prerequisite) ��downcast���邱�Ƃɂ��upcast
     */
    public static <A, B extends A, C> Fun<B,C> upCastP(final Fun<A,C> f) {
        return comp(f, FP.<A,B>upCast());
    }

    public static <A, B extends A, C> Fun<Fun<? super A,? extends C>, Fun<B,C>>
        upCastP() {
        return Arr.<B,A,C>seq(FP.<A,B>upCast());
    }

    public static <E> Fun<List<E>,Iterable<E>> upFromList() {
        return FP.<Iterable<E>,List<E>>upCast();
    }

    public static <E,A> Fun<List<? extends E>,A>
        downArgToList(final Fun<Iterable<? extends E>,A> f) {
        return FP.<Iterable<? extends E>,List<? extends E>,A>upCastP(f);
    }


    public static <A, C extends Collection<A>> Fun<Fun<A, Boolean>, Fun<Iterable<? extends A>, C>>

            genFilter(final Fun<? super Comparator<A>, C> cons, final Comparator<A> comp) {
        return new Fun<Fun<A, Boolean>, Fun<Iterable<? extends A>, C>>() {
            @Override
            public Fun<Iterable<? extends A>, C> app(final Fun<A, Boolean> pred) {
                return new Fun<Iterable<? extends A>, C>() {
                    @Override
                    public C app(final Iterable<? extends A> ite) {
                        C rv = cons.app(comp);
                        for (A a : ite) {
                            if (pred.app(a)) {
                                rv.add(a);
                            }
                        }
                        return rv;
                    }
                };
            }
        };
    }

    public static <E> Comparator<E> noComparator() {
        return null;
    }

    public static
        <A, C extends Collection<A>>
        Fun<Fun<A, Boolean>,Fun<Iterable<? extends A>,C>>

        genFilter(final Fun<? super Comparator<A>,C> cons) {
        return genFilter(cons, FP.<A>noComparator());
    }

    // only for name space
    public static final class LinkedListJ { private LinkedListJ() {noInstance();}

        public static <E> Fun<LinkedList<E>,List<E>> upToList() {
            return FP.<List<E>,LinkedList<E>>upCast();
        }

        public static <E> Fun<LinkedList<E>,Iterable<E>> upToIterable() {
            return FP.<Iterable<E>,LinkedList<E>>upCast();
        }

        public static <T> LinkedList<List<T>>
            groupBy(final FP.Eq<T> eq,
                    final Iterable<? extends T> ite) {

            LinkedList<List<T>> rv =
                new LinkedList<List<T>>();

            Iterator<? extends T> it = ite.iterator();

            if (!it.hasNext()) {
                return rv;
            }

            T first = it.next();
            LinkedList<T> group = new LinkedList<T>();
            group.add(first);
            rv.add(group);

            for ( ; ; ) {
                if (!it.hasNext()) {
                    break;
                }
                T t = it.next();

                if (eq.eq(group.getLast(), t)) {
                    group.add(t);
                    continue;
                }

                group = new LinkedList<T>();
                group.add(t);
                rv.add(group);
            }

            return rv;
        }

        public static <T> Fun<Iterable<? extends T>, LinkedList<List<T>>>
            groupBy(final FP.Eq<T> eq) {
            return new Fun<Iterable<? extends T>, LinkedList<List<T>>>() {
                @Override
                public LinkedList<List<T>>
                    app(final Iterable<? extends T> list) {
                    return groupBy(eq, list);
                }
            };
        }

        public static <T> LinkedList<List<T>>
            groupBy(final Fun<T,Fun<T,Boolean>> eq,
                    final Iterable<? extends T> ite) {
            return groupBy(defEq(eq), ite);
        }

        public static <T> Fun<Iterable<? extends T>, LinkedList<List<T>>>
            groupBy(final Fun<T,Fun<T,Boolean>> eq) {
            return new Fun<Iterable<? extends T>, LinkedList<List<T>>>() {
                @Override
                public LinkedList<List<T>>
                    app(final Iterable<? extends T> list) {
                    return groupBy(eq, list);
                }
            };
        }

        public static <A>
            Fun<Fun<A, Boolean>,
            Fun<Iterable<? extends A>,LinkedList<A>>>
            filter() {
            return genFilter(FP.<A>consLinkedList1());
        }

        public static <A>
            Fun<Iterable<? extends A>,LinkedList<A>>
            filter(final Fun<A, Boolean> pred) {
            return LinkedListJ.<A>filter().app(pred);
        }

        public static <A> LinkedList<A>
            filter(final FP.Fun<A, Boolean> pred,
                   final Iterable<? extends A> ite) {
            return filter(pred).app(ite);
        }

        public static <A> LinkedList<A>
        takeWhile(final FP.Fun<A, Boolean> pred,
               final Iterable<? extends A> ite) {
            return FP.takeWhile(FP.<Unit, A> emptyLinkedListGen(), UNIT, pred, ite);
        }

        public static <A, B> LinkedList<B>
            map(final FP.Fun<A, B> fun,
                final Iterable<? extends A> ite) {
            LinkedList<B> rv = new LinkedList<B>();
            for (A a : ite) {
                rv.add(fun.app(a));
            }
            return rv;
        }

        public static <A, B> LinkedList<B>
        map(final FP.Fun<A, B> fun,
            final A[] arr) {
            return map(fun, Arrays.asList(arr));
        }

        public static <A, B> Fun<Iterable<A>, LinkedList<B>>
            map(final FP.Fun<A, B> fun) {
            return new Fun<Iterable<A>, LinkedList<B>>() {
                @Override
                public LinkedList<B> app(final Iterable<A> ite) {
                    return map(fun, ite);
                }
            };
        }

        public static <A, B> LinkedList<B>
            mapi(final FP.Fun<Pair<Integer,A>, B> fun,
                 final Iterable<? extends A> ite) {

            LinkedList<B> rv
                = new LinkedList<B>();
            int i = 0;
            for (A a : ite) {
                rv.add(fun.app(Pair.of(i++, a)));
            }
            return rv;
        }

        public static <A, B> LinkedList<Pair<Integer, B>>
            mapRI(final FP.Fun<A, B> fun,
                  final Iterable<? extends A> ite) {

            LinkedList<Pair<Integer, B>> rv
                = new LinkedList<Pair<Integer, B>>();
            int i = 0;
            for (A a : ite) {
                rv.add(Pair.of(i++, fun.app(a)));
            }
            return rv;
        }

        /**
         * �A�� -- (++) :: [a] -> [a] -> [a]
         */
        public static <A> Fun<Iterable<A>, Fun<Iterable<A>, LinkedList<A>>> append() {
            return new Fun<Iterable<A>, Fun<Iterable<A>, LinkedList<A>>>() {
                @Override
                public Fun<Iterable<A>, LinkedList<A>> app(final Iterable<A> xs) {
                    return new Fun<Iterable<A>, LinkedList<A>>() {
                        @Override
                        public LinkedList<A> app(final Iterable<A> ys) {
                            LinkedList<A> zs = new LinkedList<A>();
                            for (A x : xs) {
                                zs.add(x);
                            }
                            for (A y : ys) {
                                zs.add(y);
                            }
                            return zs;
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� �A��
         */
        public static <A> LinkedList<A> append(final Iterable<A> xs, final Iterable<A> ys) {
            return LinkedListJ.<A>append().app(xs).app(ys);
        }

        /**
         * (pre ++) :: [a] -> [a]
         */
        public static <A> Fun<Iterable<A>, LinkedList<A>>
            pre(final Iterable<A> pre) {
            return LinkedListJ.<A>append().app(pre);
        }

        /**
         * (++ post) :: [a] -> [a]
         */
        public static <A> Fun<Iterable<A>, LinkedList<A>>
            post(final Iterable<A> post) {
            return flip(LinkedListJ.<A>append()).app(post);
        }

        /**
         * zip :: [a] -> [b] -> [(a, b)]
         */
        public static <A,B>
            Fun<Iterable<A>, Fun<Iterable<B>, LinkedList<T2<A,B>>>>
            zip() {
            return new
                Fun<Iterable<A>, Fun<Iterable<B>, LinkedList<T2<A,B>>>>() {
                @Override
                public Fun<Iterable<B>, LinkedList<T2<A,B>>>
                    app(final Iterable<A> xs) {
                    return new Fun<Iterable<B>, LinkedList<T2<A,B>>>() {
                        @Override
                        public LinkedList<T2<A,B>> app(final Iterable<B> ys) {
                            final Iterator<A> xit = xs.iterator();
                            final Iterator<B> yit = ys.iterator();
                            final LinkedList<T2<A,B>> ps =
                                FP.linkedList();
                            while (xit.hasNext() &&
                                   yit.hasNext()) {
                                ps.add(t2(xit.next(), yit.next()));
                            }
                            return ps;
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� zip
         */
        public static <A,B> LinkedList<T2<A,B>>
            zip(final Iterable<A> xs, final Iterable<B> ys) {
            return LinkedListJ.<A,B>zip().app(xs).app(ys);
        }

        /*
        public static <A,B,AL extends List<A>,BL extends List<B>>
            Fun<Iterable<T2<A,B>>, T2<AL,BL>>
            unzipPrime() {
            return new
                Fun<Iterable<T2<A,B>>, T2<AL,BL>>() {
                public T2<AL,BL>
                    app(final Iterable<T2<A,B>> ps) {
                    Iterator<T2<A,B>> zit = ps.iterator();
                    AL xs = FP.<A>linkedList();
                    List<B> ys = FP.<B>linkedList();
                    while (zit.hasNext()) {
                        T2<A,B> p = zit.next();
                        xs.add(p.fst());
                        ys.add(p.snd());
                    }
                    return pair(xs, ys);
                }
            };
        }
        */

        /**
         * unzip :: [(a, b)] -> ([a], [b])
         */
        public static <A,B>
            Fun<Iterable<T2<A,B>>, T2<List<A>,List<B>>>
            unzip() {
            return new
                Fun<Iterable<T2<A,B>>, T2<List<A>,List<B>>>() {
                @Override
                public T2<List<A>,List<B>>
                    app(final Iterable<T2<A,B>> ps) {
                    Iterator<T2<A,B>> zit = ps.iterator();
                    LinkedList<A> xs = FP.<A>linkedList();
                    LinkedList<B> ys = FP.<B>linkedList();
                    while (zit.hasNext()) {
                        T2<A,B> p = zit.next();
                        xs.add(p.fst());
                        ys.add(p.snd());
                    }
                    return FP.<List<A>,List<B>>t2(xs, ys);
                }
            };
        }

        /**
         * Java���\�b�h�� unzip
         */
        public static <A,B> T2<List<A>,List<B>>
            unzip(final Iterable<T2<A,B>> ps) {
            return LinkedListJ.<A,B>unzip().app(ps);
        }


        // �p�ɂɌ����p�^�[���Ȃ̂Ŏ������Ă݂�
        /**
         *   \ eq ->  map (fst . head &&& map snd) . groupBy (on eq fst)
         *
         *   &&& -- Control.Arrow.&&&
         *   on  -- Data.Function.on
         */
        public static <A,B> LinkedList<T2<A,LinkedList<B>>>
            groupByFst(final Fun<A,Fun<A,Boolean>> eq,
                       final Iterable<T2<A,B>> ite) {

            return
                map(Arr.<Iterable<T2<A, B>>, A, LinkedList<B>> tee(FP.<Iterable<T2<A, B>>, T2<A, B>, A> comp(FP.<A> fst1(), FP.<T2<A,B>>head()),
                            map(FP.<A,B>snd())),
                    groupBy(FP.on(eq, FP.<A,B>fst()), ite));
        }

        /**
         *   \ eq ->  map (map fst &&& snd . head) . groupBy (on eq snd)
         *
         *   &&& -- Control.Arrow.&&&
         *   on  -- Data.Function.on
         */
        public static <A,B> LinkedList<T2<LinkedList<A>,B>>
            groupBySnd(final Fun<B,Fun<B,Boolean>> eq,
                       final Iterable<T2<A,B>> ite) {

            return
                map(Arr.<Iterable<T2<A, B>>, LinkedList<A>, B> tee(LinkedListJ.<T2<A, B>, A> map(FP.<A,B>fst()),
                            FP.<Iterable<T2<A, B>>, T2<A, B>, B> comp(FP.<B>snd1(), FP.<T2<A,B>>head())),
                    LinkedListJ.<T2<A, B>> groupBy(FP. <T2<A, B>, B, Boolean>on(eq, FP.<A,B>snd()), ite));
        }


        public static <A> LinkedList<? extends A>
            unsafeDropWhile(final Fun<A,Boolean> pred, final LinkedList<? extends A> lst) {
            while (!lst.isEmpty()) {
                if (pred.app(lst.getFirst())) {
                    lst.removeFirst();
                    continue;
                }
                break;
            }
            return lst;
        }

        public static <A> Fun<LinkedList<? extends A>,LinkedList<? extends A>>
            unsafeDropWhile(final Fun<A,Boolean> pred) {
            return new Fun<LinkedList<? extends A>,LinkedList<? extends A>>() {
                @Override
                public LinkedList<? extends A> app(final LinkedList<? extends A> lst) {
                    return unsafeDropWhile(pred, lst);
                }
            };
        }

        public static <A>
            Fun<Fun<A,Boolean>,Fun<LinkedList<? extends A>,LinkedList<? extends A>>>
            unsafeDropWhile() {
            return new Fun<Fun<A,Boolean>,Fun<LinkedList<? extends A>,LinkedList<? extends A>>>() {
                @Override
                public Fun<LinkedList<? extends A>,LinkedList<? extends A>>
                    app(final Fun<A,Boolean> pred) {
                    return unsafeDropWhile(pred);
                }
            };
        }

        public static <A> LinkedList<? extends A>
            dropWhile(final Fun<A,Boolean> pred, final A[] col) {
            return unsafeDropWhile(pred, linkedList(col));
        }

        public static <A> LinkedList<? extends A>
            dropWhile(final Fun<A,Boolean> pred, final Collection<? extends A> col) {
            return unsafeDropWhile(pred, linkedList(col));
        }

        public static <A> Fun<Collection<? extends A>,LinkedList<? extends A>>
            dropWhile(final Fun<A,Boolean> pred) {
            return new Fun<Collection<? extends A>,LinkedList<? extends A>>() {
                @Override
                public LinkedList<? extends A> app(final Collection<? extends A> col) {
                    return dropWhile(pred, col);
                }
            };
        }

        public static <A>
            Fun<Fun<A,Boolean>,Fun<Collection<? extends A>,LinkedList<? extends A>>>
            dropWhile() {
            return new Fun<Fun<A,Boolean>,Fun<Collection<? extends A>,LinkedList<? extends A>>>() {
                @Override
                public Fun<Collection<? extends A>,LinkedList<? extends A>>
                    app(final Fun<A,Boolean> pred) {
                    return dropWhile(pred);
                }
            };
        }
        // End of class LinkedListJ
    }

    /*
     * Lazy List �����̂� Eager List ���瓱�o���悤�Ƃ������A
     * Generics ���ƈ����t���� Type Constructor ���p�����^���ł��Ȃ��̂Ŗ���
     * �ȉ��̃R�[�h�� Lazy List �������ɎQ�l�ɂ���
     *

    public static final class List<A> {
        private Pair<A,List<A>> cell;

        private List(A head, List<A> tail) {
            this.cell = Pair.of(head,tail);
        }

        public A head() {
            return this.cell.fst();
        }

        public List<A> tail() {
            return this.cell.snd();
        }

        static <A> List<A> _cons(A head, List<A> tail) {
            return new List<A>(head, tail);
        }
    }

    public static final <A> List<A> nil() { return null; }

    public static final <A> List<A> cons(A head, List<A> tail) {
        return List._cons(head, tail);
    }

    public static final <A> Fun<A,Fun<List<A>,List<A>>>
        cons() {
        return new Fun<A,Fun<List<A>,List<A>>>() {
            public Fun<List<A>,List<A>> app(final A a) {
                return new Fun<List<A>,List<A>>() {
                    public List<A> app(List<A> as) {
                        return cons(a, as);
                    }
                };
            }
        };
    }

    public static final <A> boolean null_(List<A> xs) {
        return xs == nil();
    }

    public static final <A> Fun<List<A>,Boolean>
        null_() {
        return new Fun<List<A>,Boolean>() {
            public Boolean app(final List<A> xs) {
                return null_(xs);
            }
        };
    }

    public static final <A> A head(List<A> xs) {
        return xs.head();
    }

    public static final <A> Fun<List<A>,A>
        head() {
        return new Fun<List<A>,A>() {
            public A app(final List<A> xs) {
                return head(xs);
            }
        };
    }

    public static final <A> List<A> tail(List<A> xs) {
        return xs.tail();
    }

    public static final <A> Fun<List<A>,List<A>>
        tail() {
        return new Fun<List<A>,List<A>>() {
            public List<A> app(final List<A> xs) {
                return tail(xs);
            }
        };
    }
    */

    /*
    // TODO
    public static class ZList<A> {
    }

    */

    public static final class ArrayListJ { private ArrayListJ() {noInstance();}

        public static <E> Fun<ArrayList<E>,List<E>> upToList() {
            return FP.<List<E>,ArrayList<E>>upCast();
        }

        public static <E> Fun<ArrayList<E>,Iterable<E>> upToIterable() {
            return FP.<Iterable<E>,ArrayList<E>>upCast();
        }

        public static <T> ArrayList<List<T>>
            groupBy(final FP.Eq<T> eq,
                    final Iterable<? extends T> ite) {

            ArrayList<List<T>> rv =
                new ArrayList<List<T>>();

            Iterator<? extends T> it = ite.iterator();

            if (!it.hasNext()) {
                return rv;
            }

            T first = it.next();
            ArrayList<T> group = new ArrayList<T>();
            group.add(first);
            rv.add(group);

            for ( ; ; ) {
                if (!it.hasNext()) {
                    break;
                }
                T t = it.next();

                if (eq.eq(group.get(group.size() - 1), t)) {
                    group.add(t);
                    continue;
                }

                group = new ArrayList<T>();
                group.add(t);
                rv.add(group);
            }

            return rv;
        }

        public static <T> Fun<Iterable<? extends T>, ArrayList<List<T>>>
            groupBy(final FP.Eq<T> eq) {
            return new Fun<Iterable<? extends T>, ArrayList<List<T>>>() {
                @Override
                public ArrayList<List<T>> app(final Iterable<? extends T> list) {
                    return groupBy(eq, list);
                }
            };
        }

        public static <T> ArrayList<List<T>>
            groupBy(final Fun<T,Fun<T,Boolean>> eq,
                    final Iterable<? extends T> ite) {
            return groupBy(defEq(eq), ite);
        }

        public static <T> Fun<Iterable<? extends T>, ArrayList<List<T>>>
            groupBy(final Fun<T,Fun<T,Boolean>> eq) {
            return new Fun<Iterable<? extends T>, ArrayList<List<T>>>() {
                @Override
                public ArrayList<List<T>> app(final Iterable<? extends T> list) {
                    return groupBy(eq, list);
                }
            };
        }

        public static <A>
            Fun<Fun<A, Boolean>,
            Fun<Iterable<? extends A>,ArrayList<A>>>
            filter() {
            return genFilter(FP.<A>consArrayList1());
        }

        public static <A>
            Fun<Iterable<? extends A>,ArrayList<A>>
            filter(final Fun<A, Boolean> pred) {
            return ArrayListJ.<A>filter().app(pred);
        }

        public static <A> ArrayList<A>
            filter(final FP.Fun<A, Boolean> pred,
                   final Iterable<? extends A> ite) {
            return filter(pred).app(ite);
        }

        /**
         * @param n
         * @param ite
         * @return ite����n�擾���āAArrayList�ɉ���������<br>
         * n���񐳐����̏ꍇ ���ArrayList
         *
         */
        public static <A> ArrayList<A> take(final int n, final Iterable<A> ite) {
            return FP.take(FP.<A> emptyArrayListGen1(), UNIT, n, ite);
        }

        public static <A> ArrayList<A>
        takeWhile(final FP.Fun<A, Boolean> pred,
               final Iterable<? extends A> ite) {
            return FP.takeWhile(FP.<Unit, A> emptyArrayListGen(), FP.UNIT, pred, ite);
        }

        public static <A, B> ArrayList<B>
            map(final FP.Fun<A, B> fun,
                final Iterable<? extends A> ite) {
            ArrayList<B> rv = new ArrayList<B>();
            for (A a : ite) {
                rv.add(fun.app(a));
            }
            return rv;
        }

        public static <A, B> ArrayList<B> map(final FP.Fun<? super A, B> fun,
                final A[] arr) {
            return map(fun, Arrays.asList(arr));
        }

        public static <A, B> Fun<Iterable<A>, ArrayList<B>>
            map(final FP.Fun<A, B> fun) {
            return new Fun<Iterable<A>, ArrayList<B>>() {
                @Override
                public ArrayList<B> app(final Iterable<A> ite) {
                    return map(fun, ite);
                }
            };
        }

        public static <A, B> ArrayList<B>
            mapi(final FP.Fun<Pair<Integer,A>, B> fun,
                 final Iterable<? extends A> ite) {

            ArrayList<B> rv
                = new ArrayList<B>();
            int i = 0;
            for (A a : ite) {
                rv.add(fun.app(Pair.of(i++, a)));
            }
            return rv;
        }

        public static <A, B> ArrayList<Pair<Integer, B>>
            mapRI(final FP.Fun<A, B> fun,
                  final Iterable<? extends A> ite) {

            ArrayList<Pair<Integer, B>> rv
                = new ArrayList<Pair<Integer, B>>();
            int i = 0;
            for (A a : ite) {
                rv.add(Pair.of(i++, fun.app(a)));
            }
            return rv;
        }

        /**
         * �A�� -- (++) :: [a] -> [a] -> [a]
         */
        public static <A>
            Fun<Iterable<A>, Fun<Iterable<A>, ArrayList<A>>> append() {
            return new Fun<Iterable<A>, Fun<Iterable<A>, ArrayList<A>>>() {
                @Override
                public Fun<Iterable<A>, ArrayList<A>> app(final Iterable<A> xs) {
                    return new Fun<Iterable<A>, ArrayList<A>>() {
                        @Override
                        public ArrayList<A> app(final Iterable<A> ys) {
                            ArrayList<A> zs = new ArrayList<A>();
                            for (A x : xs) {
                                zs.add(x);
                            }
                            for (A y : ys) {
                                zs.add(y);
                            }
                            return zs;
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� �A��
         */
        public static <A> ArrayList<A> append(final Iterable<A> xs, final Iterable<A> ys) {
            return ArrayListJ.<A>append().app(xs).app(ys);
        }

        /**
         * (pre ++) :: [a] -> [a]
         */
        public static <A> Fun<Iterable<A>, ArrayList<A>>
            pre(final Iterable<A> pre) {
            return ArrayListJ.<A>append().app(pre);
        }

        /**
         * (++ post) :: [a] -> [a]
         */
        public static <A> Fun<Iterable<A>, ArrayList<A>>
            post(final Iterable<A> post) {
            return flip(ArrayListJ.<A>append()).app(post);
        }
        /**
         * zip :: [a] -> [b] -> [(a, b)]
         */
        public static <A,B>
            Fun<Iterable<A>, Fun<Iterable<B>, ArrayList<T2<A,B>>>>
            zip() {
            return new
                Fun<Iterable<A>, Fun<Iterable<B>, ArrayList<T2<A,B>>>>() {
                @Override
                public Fun<Iterable<B>, ArrayList<T2<A,B>>>
                    app(final Iterable<A> xs) {
                    return new Fun<Iterable<B>, ArrayList<T2<A,B>>>() {
                        @Override
                        public ArrayList<T2<A,B>> app(final Iterable<B> ys) {
                            final Iterator<A> xit = xs.iterator();
                            final Iterator<B> yit = ys.iterator();
                            final ArrayList<T2<A,B>> ps =
                                FP.arrayEmpty();
                            while (xit.hasNext() &&
                                   yit.hasNext()) {
                                ps.add(t2(xit.next(), yit.next()));
                            }
                            return ps;
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� zip
         */
        public static <A,B> ArrayList<T2<A,B>>
            zip(final Iterable<A> xs, final Iterable<B> ys) {
            return ArrayListJ.<A,B>zip().app(xs).app(ys);
        }

        /**
         * unzip :: [(a, b)] -> ([a], [b])
         */
        public static <A,B>
            Fun<Iterable<T2<A,B>>, T2<List<A>,List<B>>>
            unzip() {
            return new
                Fun<Iterable<T2<A,B>>, T2<List<A>,List<B>>>() {
                @Override
                public T2<List<A>,List<B>>
                    app(final Iterable<T2<A,B>> ps) {
                    Iterator<T2<A,B>> zit = ps.iterator();
                    ArrayList<A> xs = FP.<A>arrayEmpty();
                    ArrayList<B> ys = FP.<B>arrayEmpty();
                    while (zit.hasNext()) {
                        T2<A,B> p = zit.next();
                        xs.add(p.fst());
                        ys.add(p.snd());
                    }
                    return FP.<List<A>,List<B>>t2(xs, ys);
                }
            };
        }

        /**
         * Java���\�b�h�� unzip
         */
        public static <A,B> T2<List<A>,List<B>>
            unzip(final Iterable<T2<A,B>> ps) {
            return ArrayListJ.<A,B>unzip().app(ps);
        }


        // �p�ɂɌ����p�^�[���Ȃ̂Ŏ������Ă݂�
        /**
         *   \ eq ->  map (fst . head &&& map snd) . groupBy (on eq fst)
         *
         *   &&& -- Control.Arrow.&&&
         *   on  -- Data.Function.on
         */
        public static <A,B> ArrayList<T2<A,ArrayList<B>>>
            groupByFst(final Fun<A,Fun<A,Boolean>> eq,
                       final Iterable<T2<A,B>> ite) {

            return
                map(Arr.<Iterable<T2<A, B>>, A, ArrayList<B>> tee(FP.<Iterable<T2<A, B>>, T2<A, B>, A> comp(FP.<A,B>fst(), FP.<T2<A,B>>head()),
                            map(FP.<A,B>snd())),
                    groupBy(FP.on(eq, FP.<A,B>fst()), ite));
        }

        public static <A,B> ArrayList<T2<A,ArrayList<B>>>
            groupByFst(final Eq<A> eq,
                       final Iterable<T2<A,B>> ite) {
            return groupByFst(eqFun(eq), ite);
        }

        /**
         *   \ eq ->  map (map fst &&& snd . head) . groupBy (on eq snd)
         *
         *   &&& -- Control.Arrow.&&&
         *   on  -- Data.Function.on
         */
        public static <A,B> ArrayList<T2<ArrayList<A>,B>>
            groupBySnd(final Fun<B,Fun<B,Boolean>> eq,
                       final Iterable<T2<A,B>> ite) {

            return
                map(Arr.tee(map(FP.<A,B>fst()),
                            FP.<Iterable<T2<A, B>>, T2<A, B>, B> comp(FP.<A,B>snd(), FP.<T2<A,B>>head())),
                    groupBy(FP.on(eq, FP.<A,B>snd()), ite));
        }

        public static <A,B> ArrayList<T2<ArrayList<A>,B>>
            groupBySnd(final Eq<B> eq,
                       final Iterable<T2<A,B>> ite) {
            return groupBySnd(eqFun(eq), ite);
        }

        // End of class ArrayList
    }

    public static final class TreeSetJ { private TreeSetJ() {noInstance();}

        public static <E> Fun<TreeSet<E>,Iterable<E>> upToIterable() {
            return FP.<Iterable<E>,TreeSet<E>>upCast();
        }

        public static <A>
            Fun<Fun<A, Boolean>,
            Fun<Iterable<? extends A>,TreeSet<A>>>
            filter() {
            return genFilter(FP.<A>consTreeSet());
        }

        public static <A>
            Fun<Iterable<? extends A>,TreeSet<A>>
            filter(final Fun<A, Boolean> pred) {
            return TreeSetJ.<A>filter().app(pred);
        }

        public static <A> TreeSet<A>
        filter(final FP.Fun<A, Boolean> pred,
               final Iterable<? extends A> ite) {
            return filter(pred).app(ite);
        }

        public static <A, B> TreeSet<B>
            map(final FP.Fun<A, B> fun,
                final Iterable<? extends A> ite) {
            TreeSet<B> rv = new TreeSet<B>();
            for (A a : ite) {
                rv.add(fun.app(a));
            }
            return rv;
        }

        public static <A, B> TreeSet<B>
        map(final FP.Fun<A, B> fun,
            final A[] arr) {
            return map(fun, Arrays.asList(arr));
        }

        public static <A, B> Fun<Iterable<A>, TreeSet<B>>
            map(final FP.Fun<A, B> fun) {
            return new Fun<Iterable<A>, TreeSet<B>>() {
                @Override
                public TreeSet<B> app(final Iterable<A> ite) {
                    return map(fun, ite);
                }
            };
        }
    }

    public static final class StringJ { private StringJ() {noInstance();}

        public static Fun<Object, Fun<Object, String>> unsafeAppend() {
            return new Fun<Object, Fun<Object, String>>() {
                @Override
                public Fun<Object, String> app(final Object o1) {
                    return new Fun<Object, String>() {
                        @Override
                        public String app(final Object o2) {
                            return "" + o1 + o2;
                        }
                    };
                }
            };
        }

        public static <A,B> Fun<A, Fun<B, String>> append() {
            return new Fun<A, Fun<B, String>>() {
                @Override
                public Fun<B, String> app(final A a) {
                    return new Fun<B, String>() {
                        @Override
                        public String app(final B b) {
                            return "" + a + b;
                        }
                    };
                }
            };
        }

        public static <A,B> Fun<B, String> pre(final A pre) {
            return StringJ.<A,B>append().app(pre);
        }

        public static <A,B> Fun<A, String> post(final B post) {
            return flip(StringJ.<A,B>append()).app(post);
        }
    }

    abstract public static class ZList<E> {
        // lazy list �p�ɗ\��
    }
    // lazy list �p�ɗ\��
    public static <E> ZList<E> list(@SuppressWarnings("unused") final E... e) {
        return null;
    }

    // Functions like Control.Arrow of Haskell

    public static final class Arr { private Arr() {noInstance();}

        /**
         * (<<<) :: cat b c -> cat a b -> cat a c
         *       :: (b -> c) -> (a -> b) -> a -> c
         */
        public static <A,B,C> Fun<Fun<? super B,? extends C>, Fun<Fun<? super A,? extends B>, Fun<A,C>>>
            revseq() { return FP.<A,B,C>comp(); }

        public static <A,B,C> Fun<Fun<? super A,? extends B>, Fun<A,C>>
            revseq(final Fun<? super B,? extends C> f) { return FP.<A,B,C>comp(f); }

        /**
         * Java���\�b�h�� (<<<)
         */
        public static <A,B,C> Fun<A,C> revseq(final Fun<? super B,? extends C> f,
                                              final Fun<? super A,? extends B> g) {
            return Arr.<A,B,C>revseq().app(f).app(g);
        }

        /**
         * (>>>) ::  cat a b -> cat b c -> cat a c
         *       ::  (a -> b) -> (b -> c) -> a -> c
         */
        public static <A,B,C> Fun<Fun<? super A,? extends B>, Fun<Fun<? super B,? extends C>, Fun<A,C>>>
            seq() { return FP.flip(Arr.<A,B,C>revseq()); }

        public static <A,B,C> Fun<Fun<? super B,? extends C>, Fun<A,C>>
            seq(final Fun<? super A,? extends B> f) { return Arr.<A,B,C>seq().app(f); }

        /**
         * Java���\�b�h�� (>>>)
         */
        public static <A,B,C> Fun<A,C>
            seq(final Fun<? super A,? extends B> f, final Fun<? super B,? extends C> g) {
            return Arr.<A,B,C>seq(f).app(g);
        }

        /**
         * first :: a b c -> a (b, d) (c, d)
         *       :: (b -> c) -> (b, d) -> (c, d)
         */
        public static <B,C,D> Fun<Fun<B,C>, Fun<T2<B,D>, T2<C,D>>>
            first() {
            return new Fun<Fun<B,C>, Fun<T2<B,D>, T2<C,D>>>() {
                @Override
                public Fun<T2<B,D>, T2<C,D>> app(final Fun<B,C> f) {
                    return new Fun<T2<B,D>, T2<C,D>>() {
                        @Override
                        public T2<C,D> app(final T2<B,D> p) {
                            return t2(f.app(p.fst()), p.snd());
                        }
                    };
                }
            };
        }

        public static <B,C,D> Fun<Fun<B,C>, Fun<Pair<B,D>, Pair<C,D>>>
            first_() {
            return new Fun<Fun<B,C>, Fun<Pair<B,D>, Pair<C,D>>>() {
                @Override
                public Fun<Pair<B,D>, Pair<C,D>> app(final Fun<B,C> f) {
                    return new Fun<Pair<B,D>, Pair<C,D>>() {
                        @Override
                        public Pair<C,D> app(final Pair<B,D> p) {
                            return pair(f.app(p.fst()), p.snd());
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� first
         */
        public static <B,C,D> Fun<T2<B,D>, T2<C,D>>
            first(final Fun<B,C> f) {
            return Arr.<B,C,D>first().app(f);
        }

        public static <B,C,D> Fun<Pair<B,D>, Pair<C,D>>
            first_(final Fun<B,C> f) {
            return Arr.<B,C,D>first_().app(f);
        }

        /**
         * second :: a b c -> a (d, b) (d, c)
         *        :: (b -> c) -> (d, b) -> (d, c)
         */
        public static <B,C,D> Fun<Fun<B,C>, Fun<T2<D,B>, T2<D,C>>>
            second() {
            return new Fun<Fun<B,C>, Fun<T2<D,B>, T2<D,C>>>() {
                @Override
                public Fun<T2<D,B>, T2<D,C>> app(final Fun<B,C> f) {
                    return new Fun<T2<D,B>, T2<D,C>>() {
                        @Override
                        public T2<D,C> app(final T2<D,B> p) {
                            return t2(p.fst(), f.app(p.snd()));
                        }
                    };
                }
            };
        }

        public static <B,C,D> Fun<Fun<B,C>, Fun<Pair<D,B>, Pair<D,C>>>
            second_() {
            return new Fun<Fun<B,C>, Fun<Pair<D,B>, Pair<D,C>>>() {
                @Override
                public Fun<Pair<D,B>, Pair<D,C>> app(final Fun<B,C> f) {
                    return new Fun<Pair<D,B>, Pair<D,C>>() {
                        @Override
                        public Pair<D,C> app(final Pair<D,B> p) {
                            return pair(p.fst(), f.app(p.snd()));
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� second
         */
        public static <B,C,D> Fun<T2<D,B>, T2<D,C>>
            second(final Fun<B,C> f) {
            return Arr.<B,C,D>second().app(f);
        }

        public static <B,C,D> Fun<Pair<D,B>, Pair<D,C>>
            second_(final Fun<B,C> f) {
            return Arr.<B,C,D>second_().app(f);
        }

        /**
         * (***) :: a b c -> a b' c' -> a (b, b') (c, c')
         *       :: (b -> c) -> (b' -> c') -> (b, b') -> (c, c')
         */
        public static <B,C,B_,C_>
            Fun<Fun<B,C>, Fun<Fun<B_,C_>, Fun<T2<B,B_>, T2<C,C_>>>>
            pai() {
            return new
                Fun<Fun<B,C>, Fun<Fun<B_,C_>, Fun<T2<B,B_>, T2<C,C_>>>>() {
                @Override
                public Fun<Fun<B_,C_>, Fun<T2<B,B_>, T2<C,C_>>>
                    app(final Fun<B,C> f) {
                    return new Fun<Fun<B_,C_>, Fun<T2<B,B_>, T2<C,C_>>>() {
                        @Override
                        public Fun<T2<B,B_>, T2<C,C_>>
                            app(final Fun<B_,C_> g) {
                            // f :: b -> c,   first  f :: (b, b') -> (c, b')
                            // g :: b' -> c', second g :: (c, b') -> (c, c')
                            return FP.comp(Arr.<B_,C_,C>second(g),
                                           Arr.<B,C,B_>first(f));
                        }
                    };
                }
            };
        }

        public static <B,C,B_,C_>
            Fun<Fun<B,C>, Fun<Fun<B_,C_>, Fun<Pair<B,B_>, Pair<C,C_>>>>
            pai_() {
            return new
                Fun<Fun<B,C>, Fun<Fun<B_,C_>, Fun<Pair<B,B_>, Pair<C,C_>>>>() {
                @Override
                public Fun<Fun<B_,C_>, Fun<Pair<B,B_>, Pair<C,C_>>>
                    app(final Fun<B,C> f) {
                    return new Fun<Fun<B_,C_>, Fun<Pair<B,B_>, Pair<C,C_>>>() {
                        @Override
                        public Fun<Pair<B,B_>, Pair<C,C_>>
                            app(final Fun<B_,C_> g) {
                            // f :: b -> c,   first  f :: (b, b') -> (c, b')
                            // g :: b' -> c', second g :: (c, b') -> (c, c')
                            return FP.comp(Arr.<B_,C_,C>second_(g),
                                           Arr.<B,C,B_>first_(f));
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� (***)
         */
        public static <B,C,B_,C_>
            Fun<T2<B,B_>, T2<C,C_>> pai(final Fun<B,C> f, final Fun<B_,C_> g) {
            return Arr.<B,C,B_,C_>pai().app(f).app(g);
        }

        public static <B,C,B_,C_>
            Fun<Pair<B,B_>, Pair<C,C_>> pai_(final Fun<B,C> f, final Fun<B_,C_> g) {
            return Arr.<B,C,B_,C_>pai_().app(f).app(g);
        }

        /**
         * (&&&) :: a b c -> a b c' -> a b (c, c')
         *       :: (b -> c) -> (b -> c') -> b -> (c, c')
         */
        public static <B,C,C_>
            Fun<Fun<B,C>, Fun<Fun<B,C_>, Fun<B, T2<C, C_>>>>
            tee() {
            return new
                Fun<Fun<B,C>, Fun<Fun<B,C_>, Fun<B, T2<C, C_>>>>() {
                @Override
                public Fun<Fun<B,C_>, Fun<B, T2<C, C_>>>
                    app(final Fun<B,C> f) {
                    return new Fun<Fun<B,C_>, Fun<B, T2<C, C_>>>() {
                        @Override
                        public Fun<B, T2<C, C_>> app(final Fun<B,C_> g) {
                            return comp(pai(f, g),
                                        new Fun<B, T2<B,B>>() {
                                            @Override
                                            public T2<B,B> app(final B b) {
                                                return t2(b,b);
                                            }
                                        });
                        }
                    };
                }
            };
        }

        public static <B,C,C_>
            Fun<Fun<B,C>, Fun<Fun<B,C_>, Fun<B, Pair<C, C_>>>>
            tee_() {
            return new
                Fun<Fun<B,C>, Fun<Fun<B,C_>, Fun<B, Pair<C, C_>>>>() {
                @Override
                public Fun<Fun<B,C_>, Fun<B, Pair<C, C_>>>
                    app(final Fun<B,C> f) {
                    return new Fun<Fun<B,C_>, Fun<B, Pair<C, C_>>>() {
                        @Override
                        public Fun<B, Pair<C, C_>> app(final Fun<B,C_> g) {
                            return comp(pai_(f, g),
                                        new Fun<B, Pair<B,B>>() {
                                            @Override
                                            public Pair<B,B> app(final B b) {
                                                return pair(b,b);
                                            }
                                        });
                        }
                    };
                }
            };
        }

        /**
         * Java���\�b�h�� (&&&)
         */
        public static <B,C,C_>
            Fun<B, T2<C, C_>> tee(final Fun<B,C> f, final Fun<B,C_> g) {
            return Arr.<B,C,C_>tee().app(f).app(g);
        }

        public static <B,C,C_>
            Fun<B, Pair<C, C_>> tee_(final Fun<B,C> f, final Fun<B,C_> g) {
            return Arr.<B,C,C_>tee_().app(f).app(g);
        }
    }

    // End -- Functions like Control.Arrow of Haskell

    public static <A> Fun<PrintStream,Fun<A,Unit>> print() {
        return new Fun<PrintStream,Fun<A,Unit>>() {
            @Override
            public Fun<A,Unit> app(final PrintStream stream) {
                return new Fun<A,Unit>() {
                    @Override
                    public Unit app(final A a) {
                        stream.print(a);
                        return UNIT;
                    }
                };
            }
        };
    }

    public static <A> Fun<A,Unit> outPrint() {
        return FP.<A>print().app(System.out);
    }

    public static <A> void outPrint(final A a) {
        outPrint().app(a);
    }

    public static <A> Fun<A,Unit> errPrint() {
        return FP.<A>print().app(System.err);
    }

    public static <A> void errPrint(final A a) {
        errPrint().app(a);
    }

    public static <A> Fun<PrintStream,Fun<A,Unit>> println() {
        return new Fun<PrintStream,Fun<A,Unit>>() {
            @Override
            public Fun<A,Unit> app(final PrintStream stream) {
                return new Fun<A,Unit>() {
                    @Override
                    public Unit app(final A a) {
                        stream.println(a);
                        return UNIT;
                    }
                };
            }
        };
    }

    public static <A> Fun<A,Unit> outPrintln() {
        return FP.<A>println().app(System.out);
    }

    public static <A> void outPrintln(final A a) {
        outPrintln().app(a);
    }

    public static <A> Fun<A,Unit> errPrintln() {
        return FP.<A>println().app(System.err);
    }

    public static <A> void errPrintln(final A a) {
        errPrintln().app(a);
    }

    public static abstract class T2<A,B> {
        abstract public A fst();
        abstract public B snd();

        public static <A,B> T2<A,B> of(final A a, final B b) {
            return Pair.of(a, b);
        }

        public static <A, B> T2<A, B> upCast(final T2<? extends A, ? extends B> p){
            return T2.<A, B> of(p.fst(), p.snd());
        }
    }

    /**
     * 2-tuple �R���X�g���N�^ -- (,) :: a -> b -> (a, b)
     */
    public static <A,B> Fun<A, Fun<B, T2<A, B>>> t2() {
        return new Fun<A, Fun<B, T2<A, B>>>() {
            @Override
            public Fun<B, T2<A, B>> app(final A a) {
                return t2(a);
                // return new Fun<B, T2<A, B>>() {
                //     public T2<A, B> app(final B b) {
                //         return T2.of(a, b);
                //     }
                // };
            }
        };
    }

    public static <A,B> Fun<B, T2<A, B>> t2(final A a) {
        return new Fun<B, T2<A, B>>() {
            @Override
            public T2<A, B> app(final B b) {
                return T2.of(a, b);
            }
        };
    }

    /**
     * Java���\�b�h�� 2-tuple �R���X�g���N�^
     */
    public static <A,B> T2<A,B> t2(final A a, final B b) {
        return T2.of(a, b);
    }

    public static <A,B> Eq<T2<A,B>>
        t2Eq(final Eq<A> eqA, final Eq<B> eqB) {
        return new Eq<T2<A,B>>() {
            @Override
            public boolean eq(final T2<A,B> o1, final T2<A,B> o2) {
                return eqA.eq(o1.fst(), o2.fst()) && eqB.eq(o1.snd(), o2.snd());
            }
        };
    }

    @Deprecated
    public static <A,B> Fun<T2<A,B>,Pair<A,B>> pairOfT2() {
        return new Fun<T2<A,B>,Pair<A,B>>() {
            @Override
            public Pair<A,B> app(final T2<A,B> t2) {
                return Pair.of(t2.fst(), t2.snd());
            }
        };
    }

    @Deprecated
    public static <A,B> Pair<A,B> pairOfT2(final T2<A,B> t2) {
        return FP.<A,B>pairOfT2().app(t2);
    }

    public static final class T3<A,B,C> {
        A a; B b; C c;
        private T3(final A a, final B b , final C c) {
            this.a = a; this.b = b; this.c = c;
        }
        public static <A,B,C> T3<A,B,C> of(final A a, final B b , final C c) {
            return new T3<A,B,C>(a, b, c);
        }

        public A fst() { return this.a; }
        public B snd() { return this.b; }
        public C trd() { return this.c; }
    }

    public static <A,B,C> T3<A,B,C> t3(final A a, final B b , final C c) {
        return T3.of(a, b, c);
    }

    public static <A, B, C> Eq<T3<A, B, C>> t3Eq(final Eq<A> eqA,
            final Eq<B> eqB, final Eq<C> eqC) {
        return new Eq<T3<A, B, C>>() {
            @Override
            public boolean eq(final T3<A, B, C> o1, final T3<A, B, C> o2) {
                return eqA.eq(o1.fst(), o2.fst()) && eqB.eq(o1.snd(), o2.snd())
                        && eqC.eq(o1.trd(), o2.trd());
            }
        };
    }

    public static final class T4<A,B,C,D> {
        A a; B b; C c; D d;
        private T4(final A a, final B b , final C c, final D d) {
            this.a = a; this.b = b; this.c = c; this.d = d;
        }
        public static <A,B,C,D> T4<A,B,C,D> of(final A a, final B b , final C c, final D d) {
            return new T4<A,B,C,D>(a, b, c, d);
        }

        public A fst() { return this.a; }
        public B snd() { return this.b; }
        public C trd() { return this.c; }
        public D fth() { return this.d; }
    }

    public static <A,B,C,D> T4<A,B,C,D> t4(final A a, final B b , final C c, final D d) {
        return T4.of(a, b, c, d);
    }

    public static final Fun<String,Fun<String,Boolean>>
        STRING_EQ = new Fun<String,Fun<String,Boolean>>() {
        @Override
        public Fun<String,Boolean> app(final String s1) {
            return new Fun<String,Boolean>() {
                @Override
                public Boolean app(final String s2) {
                    return StringUtils.equals(s1, s2);
                }
            };
        }
    };

    public static <A, E> Fun<A, ArrayList<E>> emptyArrayListGen() {
        return new Fun<A, ArrayList<E>>() {

            @Override
            public ArrayList<E> app(final A a) {
                // �����͖���
                return arrayList();
            }
        };
    }

    public static <E> Fun<Object, ArrayList<E>> emptyArrayListGen1() {
        return new Fun<Object, ArrayList<E>>() {

            @Override
            public ArrayList<E> app(final Object __) {
                // �����͖���
                return arrayList();
            }
        };
    }


    public static <A, E> Fun<A, LinkedList<E>> emptyLinkedListGen() {
        return new Fun<A, LinkedList<E>>() {

            @Override
            public LinkedList<E> app(final A a) {
                // �����͖���
                return linkedList();
            }
        };
    }

    /**
     * @param colGen �R���N�V�������쐬����֐�
     * @param init colGen�ɗ^���鏉���l �s�v�Ȃ� UNIT��OK
     * @param pred �q��
     * @param ite �R���N�V�����𒊏o������Iterable
     * @return ite����pred��true��Ԃ����̂����o���āAcolGen�ō��ꂽ�R���N�V�����ɉ���������
     */
    protected static <A, E, C extends Collection<E>> C
    takeWhile(final Fun<A, C> colGen, final A init, final Fun<E, Boolean> pred,
           final Iterable<? extends E> ite) {
        C rv = colGen.app(init);
        for (E a : ite) {
            if (pred.app(a)) {
                rv.add(a);
            } else {
                break;
            }
        }
        return rv;
    }

    /**
     * @param colGen
     *            �R���N�V�������쐬����֐�
     * @param init
     *            colGen�ɗ^���鏉���l �s�v�Ȃ� UNIT��OK
     * @param n
     *            ��
     * @param ite
     *            �R���N�V�����𒊏o������Iterable
     * @return ite����n�擾���āAcolGen�ō��ꂽ�R���N�V�����ɉ���������<br>
     * n���񐳐����̏ꍇ colGen�ō��ꂽ�R���N�V������Ԃ��B
     */
    public static <A, E, C extends Collection<E>> C take(
            final Fun<? super A, C> colGen, final A init, final int n,
            final Iterable<? extends E> ite) {
        int count = 0;
        C result = colGen.app(init);
        for (E a : ite) {
            if (count < n) {
                result.add(a);
                count++;
            } else {
                break;
            }
        }
        return result;
    }
}
