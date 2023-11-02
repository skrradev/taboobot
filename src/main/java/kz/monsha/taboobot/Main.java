package kz.monsha.taboobot;


import kz.monsha.taboobot.config.MongoDBConfig;
import kz.monsha.taboobot.config.YamlConfigReader;

public class Main {
    public static void main(String[] args) {
        YamlConfigReader configReader = new YamlConfigReader("application.yml");
        String botToken =  configReader.getValue("telegram.bot.token");
        String botUsername =  configReader.getValue("telegram.bot.username");

        System.out.println("Bot Token: " + botToken);
        System.out.println("Bot Username: " + botUsername);


        MongoDBConfig mongoDBConfig = new MongoDBConfig(configReader);


    }
}