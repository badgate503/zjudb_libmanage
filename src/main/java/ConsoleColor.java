public class ConsoleColor {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

    public static String colorize(String str, String color) {
        if (color.equals("red")) {
            return RED + str + RESET;
        } else if (color.equals("green")) {
            return GREEN + str + RESET;
        } else if (color.equals("yellow")) {
            return YELLOW + str + RESET;
        } else if (color.equals("blue")) {
            return BLUE + str + RESET;
        }
        return str;
    }

    public static void logs(String fName, String info, String state)
    {
        String color;
        if(state.equals("ERR"))
            color = "red";
        else if(state.equals("SUC"))
            color = "green";
        else if(state.equals("WRN"))
            color = "yellow";
        else
            color = "blue";
        String co = colorize(state, color);
        System.out.println("["+co+"] <"+fName+">: " + info);
    }
}
