package com.wxl.utils;

import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by wuxingle on 2017/12/4.
 * java8集合
 */
public class CollectionAndMap8Test {

    private Random random = new Random(10);

    @Test
    public void testIterable() {
        List<String> list = getRandomList(random, 10);
        System.out.println(list);
        System.out.println("for each");
        list.forEach(System.out::print);
    }

    @Test
    public void testIterator() {
        List<String> list = getRandomList(random, 10);
        System.out.println(list);
        System.out.println("for each");
        list.iterator().forEachRemaining(System.out::print);
    }

    @Test
    public void testCollection() {
        List<String> list = getRandomList(random, 10);
        System.out.println(list);
        //删除m,v
        Predicate<String> predicate = (t) -> t.equals("v");
        list.removeIf(predicate.or((t) -> t.equals("m")));
        System.out.println(list);
    }

    /**
     * stream中间操作
     */
    @Test
    public void testIntermediateStream() {
        List<String> list = getRandomList(random, 10);
        list.add("c");
        System.out.println(list);

        //filter
        System.out.println("filter:");
        list.stream().filter((t) -> t.charAt(0) < 'm')
                .forEach(System.out::print);
        System.out.println();

        //map,把原来的集合映射成boolean集合
        System.out.println("map:");
        list.stream().map((t)->t.charAt(0) < 'm')
                .forEach(System.out::print);
        System.out.println();

        //flatmap一对多映射,把层级结构扁平化，就是将最底层元素抽出来放到一起，
        // 最终 output 的新 Stream 里面已经没有 List 了，都是直接的数字。
        System.out.println("flatmap:");
        Stream<List<Integer>> intStream = Stream.of(
                Arrays.asList(1,2),
                Arrays.asList(3,4,5),
                Arrays.asList(6,7,8,9));
        Stream<Integer> outStream = intStream
                .flatMap(List::stream);

        //distinct
        System.out.println("distinct:");
        list.stream().distinct()
                .forEach(System.out::print);
        System.out.println();

        //sorted
        System.out.println("sorted:");
        list.stream().sorted()
                .forEach(System.out::print);
        System.out.println();

        //sorted
        System.out.println("sorted:");
        list.stream().sorted((p1,p2) -> -p1.compareTo(p2))
                .forEach(System.out::print);
        System.out.println();

        //peek 对每个元素执行操作并返回一个新的 Stream
        System.out.println("peek:");
        list.stream().peek((t)->System.out.print(t+","))
                .filter((t)->t.charAt(0)<'m')
                .peek((t)->System.out.print(t+","))
                .forEach(System.out::print);
        System.out.println();

        //limit
        System.out.println("limit:");
        list.stream().limit(5)
                .forEach(System.out::print);
        System.out.println();

        //skip
        System.out.println("skip:");
        list.stream().skip(5)
                .forEach(System.out::print);
        System.out.println();

        //
    }

    /**
     * stream最终操作
     */
    @Test
    public void testTerminalStream(){
        List<String> list = getRandomList(random, 10);
        list.add("c");
        System.out.println(list);

        System.out.println(list.stream().reduce((s1,s2)->s1+s2+",").orElse("xx"));

        List<String> list2 = list.stream()
                .filter((t)->t.charAt(0)<'m')
                .collect(Collectors.toList());
        System.out.println(list2);

        System.out.println("min:"+list.stream().min(Comparator.naturalOrder()).get());
        System.out.println("max:"+list.stream().max(Comparator.naturalOrder()).get());

        System.out.println("count:"+list.stream().filter((t)->t.charAt(0)<'m').count());
    }

    /**
     * stream的ShortCircuiting
     */
    @Test
    public void testShortCircuiting(){
        List<String> list = getRandomList(random, 10);
        list.add("c");
        System.out.println(list);

        System.out.println(list.stream().anyMatch((t)->t.equals("c")));
        System.out.println(list.stream().allMatch((t)->t.charAt(0)>'a' && t.charAt(0)<'z'));
        System.out.println(list.stream().noneMatch((t)->t.charAt(0)<'a' || t.charAt(0)>'z'));
        System.out.println(list.stream().findFirst().get());
        System.out.println(list.stream().findAny().get());
    }

    /**
     * 自己生成stream
     */
    @Test
    public void testGenerator(){
        Random random = new Random();
        //这个一个无限的stream
        Supplier<Integer> supplier= random::nextInt;
        Stream.generate(supplier)
                .limit(10)
                .forEach(System.out::println);
        //或者
        IntStream.generate(random::nextInt)
                .limit(10)
                .forEach(System.out::println);

        //和reduce类似，不过是一元操作,这也是无限的stream
        Stream.iterate(0,(n)->n+3).limit(10).forEach(System.out::println);
    }


    private List<String> getRandomList(Random random, int len) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            list.add((char) (random.nextInt(26) + 'a') + "");
        }
        return list;
    }

}
