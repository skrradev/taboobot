package kz.monsha.taboobot;


import kz.monsha.taboobot.config.YamlConfigReader;

public class Main {
    public static void main(String[] args) {
        YamlConfigReader configReader = new YamlConfigReader("application.yml");
        String botToken = (String) configReader.getValue("telegram.bot.token");
        String botUsername = (String) configReader.getValue("telegram.bot.username");

        System.out.println("Bot Token: " + botToken);
        System.out.println("Bot Username: " + botUsername);



    }
}