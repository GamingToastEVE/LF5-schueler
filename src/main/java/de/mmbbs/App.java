package de.mmbbs;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        String[] namen = {"Thomas","Frank","Simone","Oliver"};

        for (String name : namen) {
            System.out.println(name);            
        }
    }

    public static int add(int a, int b) {
        return a+b;
    }
}
