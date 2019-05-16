package com.atson.commons.lang;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.NullComparator;

import com.atson.commons.lang.FP.Fun;

public final class ComparatorUtil {
    private ComparatorUtil() {
        FP.noInstance();
    }
    
    /**
     * {@link Comparator#compare(Object, Object)} や{@link Comparable#compareTo(Object)}の比較結果を反転する。
     * @param compare
     * @return compare が0なら0、負なら正、正なら負のint
     */
    public static int negateCompare(final int compare) {
        // use Integer.compare(0, compare) from Java7
        return Integer.valueOf(0).compareTo(compare);
    }
    
//    /**
//     * org.apache.commons.collections.ComparatorUtils.naturalComparator() に型付け
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> Comparator<T> naturalComparator() {
//        return ComparatorUtils.naturalComparator();
//    }
    
    /**
     * 比較対象をそれぞれfunしてからcompする新たなComparatorを返す。
     * @param comp
     * @param fun
     * @return Fun<A, B> fun,
     */
    public static <A, B> Comparator<A> cons(final Comparator<B> comp, final Fun<A, B> fun) {
        return new Comparator<A>(){

            @Override
            public int compare(final A o1, final A o2) {
                return comp.compare(fun.app(o1), fun.app(o2));
            }
            
        };
    }
    
    /**
     * BeanComparatorのGenerics対応、staticメソッド用意、nullプロパティ許可 (デフォルトではnullはnon-nullより大きい)
     * @author wy8h-hsmt
     *
     * @param <T> Beanの型
     */
    public static class BeanComparatorWrapper<T> implements Comparator<T>, Serializable {

        private BeanComparator beanComparator;
        
        public static <T> BeanComparatorWrapper<T> beanComparatorW(final String property) {
            return new BeanComparatorWrapper<T>(property);
        }
        
        public static <T> BeanComparatorWrapper<T> beanComparatorW(final String property, final Comparator<?> comparator) {
            return new BeanComparatorWrapper<T>(property, comparator);
        }
        
        public static <T> BeanComparatorWrapper<T> beanComparatorW(final String property, final boolean nullsAreHigh) {
            return new BeanComparatorWrapper<T>(property, nullsAreHigh);
        }
        
        public static <T> BeanComparatorWrapper<T> beanComparatorW(final String property, final Comparator<?> comparator, final boolean nullsAreHigh) {
            return new BeanComparatorWrapper<T>(property, comparator, nullsAreHigh);
        }
        
        public BeanComparatorWrapper(final String property) {
            this.beanComparator = new BeanComparator(property, new NullComparator());
        }
        
        public BeanComparatorWrapper(final String property, final Comparator<?> comparator) {
            this.beanComparator = new BeanComparator(property, new NullComparator(comparator));
        }

        public BeanComparatorWrapper(final String property, final boolean nullsAreHigh) {
            this.beanComparator = new BeanComparator(property, new NullComparator(nullsAreHigh));
        }
        
        public BeanComparatorWrapper(final String property, final Comparator<?> comparator, final boolean nullsAreHigh) {
            this.beanComparator = new BeanComparator(property, new NullComparator(comparator, nullsAreHigh));
        }
        
        @Override
        public int compare(final T o1, final T o2) {
            return this.beanComparator.compare(o1, o2);
        }

        public String getProperty() {
            return this.beanComparator.getProperty();
        }

        public void setProperty(final String property) {
            this.beanComparator.setProperty(property);
        }
        
    }
    
    /**
     * @param property
     * @return propertyプロパティによりTを比較する{@link BeanComparatorWrapper}
     */
    public static <T> BeanComparatorWrapper<T> beanProperty(final String property) {
        return BeanComparatorWrapper.beanComparatorW(property);
    }
    
    /**
     * @param property
     * @return propertyプロパティを取得し、その値をcompで比較することによりTを比較する{@link BeanComparatorWrapper}
     */
    public static <T> BeanComparatorWrapper<T> beanProperty(final String property, final Comparator<?> comp) {
        return BeanComparatorWrapper.beanComparatorW(property, comp);
    }

    /**
     * @param <T>
     * @param property
     * @param nullsAreHigh trueの場合nullは最大、falseの場合nullは最小
     * @return propertyプロパティによりTを比較する{@link BeanComparatorWrapper}
     */
    public static <T> BeanComparatorWrapper<T> beanProperty(final String property, final boolean nullsAreHigh) {
        return BeanComparatorWrapper.beanComparatorW(property, nullsAreHigh);
    }
    
    /**
     * @param <T>
     * @param property
     * @param comp
     * @param nullsAreHigh trueの場合nullは最大、falseの場合nullは最小
     * @return propertyプロパティを取得し、その値をcompで比較することによりTを比較する{@link BeanComparatorWrapper}
     */
    public static <T> BeanComparatorWrapper<T> beanProperty(final String property, final Comparator<?> comp, final boolean nullsAreHigh) {
        return BeanComparatorWrapper.beanComparatorW(property, comp, nullsAreHigh);
    }

    
    /**
     * @return 空の{@link ComparatorChainWrapper}
     */
    public static <T> ComparatorChainWrapper<T> newChain() {
        return ComparatorChainWrapper.<T> newInstance();
    }
    
    /**
     * ComparatorChainWrapperのGenerics対応<br>
     * 流れるようなインターフェース
     * @author wy8h-hsmt
     *
     * @param <T>
     */
    public static class ComparatorChainWrapper<T> implements Comparator<T>, Serializable  {
        
        public ComparatorChainWrapper() {
            this.comparatorChain = new ComparatorChain();
        }
        
        public static <T> ComparatorChainWrapper<T> newInstance() {
            return new ComparatorChainWrapper<T>();
        }
        
        public ComparatorChainWrapper<T> append(final Comparator<? super T> comparator) {
            addComparator(comparator);
            return this;
        }

        public ComparatorChainWrapper<T> append(final Comparator<? super T> comparator, final boolean reverse) {
            addComparator(comparator, reverse);
            return this;
        }
        
        private ComparatorChain comparatorChain;

        private void addComparator(final Comparator<? super T> comparator, final boolean reverse) {
            this.comparatorChain.addComparator(comparator, reverse);
        }

        private void addComparator(final Comparator<? super T> comparator) {
            this.comparatorChain.addComparator(comparator);
        }

        @Override
        public int compare(final T o1, final T o2)
                throws UnsupportedOperationException {
            return this.comparatorChain.compare(o1, o2);
        }

        public int size() {
            return this.comparatorChain.size();
        }

    }
}
