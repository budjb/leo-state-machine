package leo.demo;

import java.util.Arrays;

public class Application {
    public static void main(String[] args) {
        System.out.println("List of parsed tokens:");
        System.out.println("----------------------");
        Arrays.stream(new ArgumentTokenizer().tokenize("foo bar")).forEach(System.out::println);
    }
}
